package com.minhnam.microlmssaas.modules.tenant.service;

import com.minhnam.microlmssaas.modules.tenant.entity.Tenant;
import com.minhnam.microlmssaas.modules.tenant.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TenantResolverService {
    private static final Logger log = LoggerFactory.getLogger(TenantResolverService.class);
    private static final String TENANT_CACHE_PREFIX = "tenant_domain:";
    
    @Value("${tenant.config.base-domain:microlms.com}")
    private String baseDomain;
    
    private final TenantRepository tenantRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public TenantResolverService(TenantRepository tenantRepository, RedisTemplate<String, String> redisTemplate) {
        this.tenantRepository = tenantRepository;
        this.redisTemplate = redisTemplate;
    }

    public String resolveSchema(String headerTenantId, String serverName) {
        // 1. Ưu tiên Header (API/Mobile/Postman)
        if (headerTenantId != null && !headerTenantId.isEmpty()) {
            return headerTenantId;
        }

        // 2. Phân giải từ Subdomain: {tenant}.{baseDomain}
        // Ví dụ Local: uit.localhost -> trích xuất 'uit'
        if (serverName != null && serverName.endsWith("." + baseDomain)) {
            String tenantIdFromDomain = serverName.substring(0, serverName.indexOf("."));
            return getCachedSchemaName(tenantIdFromDomain);
        }

        // 3. Mặc định dùng public
        return "public";
    }

    private String getCachedSchemaName(String tenantId) {
        String cacheKey = TENANT_CACHE_PREFIX + tenantId;
        
        // Kiểm tra trong Redis
        String schemaName = redisTemplate.opsForValue().get(cacheKey);
        
        if (schemaName != null) {
            return schemaName;
        }

        log.info("Redis cache miss for tenant: {}. Searching in database...", tenantId);
        
        // Nếu không có trong Redis, tìm trong DB
        return tenantRepository.findByTenantId(tenantId)
                .map(tenant -> {
                    String sName = tenant.getSchemaName();
                    // Lưu lại vào Redis với thời gian hết hạn (VD: 1 giờ)
                    redisTemplate.opsForValue().set(cacheKey, sName, 1, TimeUnit.HOURS);
                    return sName;
                })
                .orElse("public");
    }
}

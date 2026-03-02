package com.minhnam.microlmssaas.modules.tenant.service;

import com.minhnam.microlmssaas.modules.tenant.dto.TenantCreateRequest;
import com.minhnam.microlmssaas.modules.tenant.entity.Tenant;
import com.minhnam.microlmssaas.modules.tenant.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.redis.core.RedisTemplate;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantSchemaService tenantSchemaService;
    private final RedisTemplate<String, String> redisTemplate;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    private static final String TENANT_CACHE_PREFIX = "tenant_domain:";

    public TenantService(TenantRepository tenantRepository, 
                         TenantSchemaService tenantSchemaService,
                         RedisTemplate<String, String> redisTemplate,
                         org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.tenantRepository = tenantRepository;
        this.tenantSchemaService = tenantSchemaService;
        this.redisTemplate = redisTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    public Tenant getTenantById(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found with id: " + id));
    }

    @Transactional
    public Tenant updateTenant(UUID id, com.minhnam.microlmssaas.modules.tenant.dto.TenantUpdateRequest request) {
        Tenant tenant = getTenantById(id);
        
        if (request.displayName() != null) {
            tenant.setDisplayName(request.displayName());
        }
        if (request.isActive() != null) {
            tenant.setIsActive(request.isActive());
        }
        
        Tenant updatedTenant = tenantRepository.save(tenant);
        
        // Clear cache to ensure the resolver picks up latest changes (especially if we added domain logic later)
        redisTemplate.delete(TENANT_CACHE_PREFIX + tenant.getTenantId());
        
        return updatedTenant;
    }

    @Transactional
    public void deleteTenant(UUID id) {
        Tenant tenant = getTenantById(id);
        String schemaName = tenant.getSchemaName();
        
        // 1. Delete Master Data
        tenantRepository.delete(tenant);
        
        // 2. Clear Cache
        redisTemplate.delete(TENANT_CACHE_PREFIX + tenant.getTenantId());
        
        // 3. Physical Delete: Drop the schema and all its contents
        System.out.println(">>> Physically dropping schema: " + schemaName);
        jdbcTemplate.execute(String.format("DROP SCHEMA IF EXISTS \"%s\" CASCADE", schemaName));
    }

    @Transactional
    public Tenant createTenant(TenantCreateRequest request) {
        String schemaName = "tenant_" + request.tenantId().replace("-", "_");
        
        // 1. Lưu Master Data vào Public Schema (Nhanh)
        Tenant tenant = new Tenant();
        tenant.setTenantId(request.tenantId());
        tenant.setSchemaName(schemaName);
        tenant.setDisplayName(request.displayName());
        tenant.setIsActive(true);
        tenant.setCreatedAt(LocalDateTime.now());
        
        tenant = tenantRepository.save(tenant);
        
        // 2. Gọi Service xử lý Async thực sự (Không bị block vì khác Bean)
        tenantSchemaService.initTenantSchema(schemaName, request.adminEmail(), request.adminFullName());
        
        return tenant;
    }
}

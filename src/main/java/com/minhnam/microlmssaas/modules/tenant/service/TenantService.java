package com.minhnam.microlmssaas.modules.tenant.service;

import com.minhnam.microlmssaas.modules.tenant.dto.TenantCreateRequest;
import com.minhnam.microlmssaas.modules.tenant.entity.Tenant;
import com.minhnam.microlmssaas.modules.tenant.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantSchemaService tenantSchemaService;

    public TenantService(TenantRepository tenantRepository, TenantSchemaService tenantSchemaService) {
        this.tenantRepository = tenantRepository;
        this.tenantSchemaService = tenantSchemaService;
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

package com.minhnam.microlmssaas.modules.tenant.controller;

import com.minhnam.microlmssaas.modules.tenant.dto.TenantCreateRequest;
import com.minhnam.microlmssaas.modules.tenant.entity.Tenant;
import com.minhnam.microlmssaas.modules.tenant.service.TenantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    public ResponseEntity<Tenant> createTenant(@RequestBody TenantCreateRequest request) {
        Tenant tenant = tenantService.createTenant(request);
        return ResponseEntity.ok(tenant);
    }
}

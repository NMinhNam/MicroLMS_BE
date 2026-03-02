package com.minhnam.microlmssaas.modules.tenant.dto;

public record TenantCreateRequest(
    String tenantId,
    String displayName,
    String adminEmail,
    String adminFullName
) {}

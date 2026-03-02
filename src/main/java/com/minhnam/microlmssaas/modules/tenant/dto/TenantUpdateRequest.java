package com.minhnam.microlmssaas.modules.tenant.dto;

public record TenantUpdateRequest(
    String displayName,
    Boolean isActive
) {}

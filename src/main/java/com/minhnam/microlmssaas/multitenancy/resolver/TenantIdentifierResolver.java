package com.minhnam.microlmssaas.multitenancy.resolver;

import com.minhnam.microlmssaas.multitenancy.context.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {
    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenantId();
        return Objects.requireNonNullElse(tenantId, "public");
    }

    @Override
    public boolean validateExistingCurrentSessions() { return true; }
}

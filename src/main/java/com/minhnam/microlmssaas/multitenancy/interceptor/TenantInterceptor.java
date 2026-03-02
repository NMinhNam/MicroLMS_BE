package com.minhnam.microlmssaas.multitenancy.interceptor;

import com.minhnam.microlmssaas.multitenancy.context.TenantContext;
import com.minhnam.microlmssaas.modules.tenant.service.TenantResolverService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {
    private final TenantResolverService tenantResolverService;

    public TenantInterceptor(TenantResolverService tenantResolverService) {
        this.tenantResolverService = tenantResolverService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String headerTenantId = request.getHeader("X-Tenant-ID");
        String serverName = request.getServerName();
        
        String resolvedSchema = tenantResolverService.resolveSchema(headerTenantId, serverName);
        
        System.out.println(">>> Resolved Tenant Schema: " + resolvedSchema + " (Header: " + headerTenantId + ", Domain: " + serverName + ")");
        
        if ("public".equals(resolvedSchema) && (headerTenantId == null || headerTenantId.isEmpty())) {
            // Có thể cấu hình để chặn nếu là production
        }
        
        TenantContext.setTenantId(resolvedSchema);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear(); // Quan trọng: Tránh rò rỉ dữ liệu giữa các luồng (Thread)
    }
}

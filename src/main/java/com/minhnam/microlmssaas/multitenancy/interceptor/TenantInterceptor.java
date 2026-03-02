package com.minhnam.microlmssaas.multitenancy.interceptor;

import com.minhnam.microlmssaas.multitenancy.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantId = request.getHeader("X-Tenant-ID");
        if (tenantId == null || tenantId.isEmpty()) {
            throw new RuntimeException("Missing X-Tenant-ID header");
        }
        TenantContext.setTenantId(tenantId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear(); // Quan trọng: Tránh rò rỉ dữ liệu giữa các luồng (Thread)
    }
}

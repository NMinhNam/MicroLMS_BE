package com.minhnam.microlmssaas.modules.tenant.service;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
public class TenantSchemaService {
    private static final Logger log = LoggerFactory.getLogger(TenantSchemaService.class);
    
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public TenantSchemaService(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Async
    public void initTenantSchema(String schemaName, String adminEmail, String adminFullName) {
        long startTime = System.currentTimeMillis();
        try {
            log.info(">>> [SCHEDULER] Starting background initialization for: {}", schemaName);
            
            // 1. Tạo Physical Schema
            long stepStart = System.currentTimeMillis();
            String createSchemaSql = String.format("CREATE SCHEMA IF NOT EXISTS \"%s\"", schemaName);
            jdbcTemplate.execute(createSchemaSql);
            log.info("Step 1/3: Create Schema [{}] took {}ms", schemaName, (System.currentTimeMillis() - stepStart));

            // 2. Khởi tạo cấu trúc bảng (Migration dùng Flyway)
            stepStart = System.currentTimeMillis();
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(schemaName)
                    .locations("db/migration/tenants") // Bỏ prefix classpath: để flyway tự quét rộng hơn
                    .baselineOnMigrate(true)
                    .validateOnMigrate(false)
                    .load();
            
            log.info("Flyway identified {} scripts for schema {}", flyway.info().all().length, schemaName);
            int migrationsCount = flyway.migrate().migrationsExecuted;
            log.info("Step 2/3: Flyway Migration ({} scripts executed) took {}ms", migrationsCount, (System.currentTimeMillis() - stepStart));

            // 3. Tạo tài khoản Admin mặc định
            stepStart = System.currentTimeMillis();
            String insertAdminSql = String.format(
                "INSERT INTO \"%s\".users (email, full_name, role) VALUES (?, ?, 'ADMIN') ON CONFLICT (email) DO NOTHING", 
                schemaName
            );
            jdbcTemplate.update(insertAdminSql, adminEmail, adminFullName);
            log.info("Step 3/3: Insert Admin for [{}] took {}ms", schemaName, (System.currentTimeMillis() - stepStart));

            log.info("<<< [SUCCESS] Initialization for {} finished in {}ms", schemaName, (System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            log.error("!!! [FAILED] Initialization for {}: {}", schemaName, e.getMessage(), e);
        }
    }
}

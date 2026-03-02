# Agentic Coding Guidelines - MicroLMS_BE

This document provides essential information for AI agents operating in the MicroLMS_BE repository. Adhere to these standards to ensure consistency and quality.

## 1. Build and Test Commands

The project uses Maven as the build tool and Java 25.

- **Build Project**: `mvn clean compile`
- **Full Install**: `mvn clean install`
- **Run All Tests**: `mvn test`
- **Run a Single Test Class**: `mvn test -Dtest=ClassName`
- **Run a Single Test Method**: `mvn test -Dtest=ClassName#methodName`
- **Run Spring Boot**: `mvn spring-boot:run`

## 2. Project Architecture

The project follows a modular Spring Boot structure:
- `com.minhnam.microlmssaas.modules.<module_name>`: Contains domain-specific logic.
    - `controller`: REST Endpoints.
    - `entity`: JPA Entities.
    - `repository`: Spring Data JPA Repositories.
    - `service`: (Recommended) Business logic.
- `com.minhnam.microlmssaas.multitenancy`: Core multi-tenancy implementation using schema-based isolation.
- `com.minhnam.microlmssaas.config`: Global configuration classes.

## 3. Code Style & Conventions

### 3.1 Naming Conventions
- **Classes**: `PascalCase` (e.g., `CourseController`, `TenantInterceptor`).
- **Methods & Variables**: `camelCase` (e.g., `getAll()`, `tenantId`).
- **Endpoints**: `kebab-case` and pluralized (e.g., `/api/courses`, `/api/user-profiles`).
- **Database Tables**: Pluralized `snake_case` (e.g., `courses`).

### 3.2 Imports
- Use explicit imports; avoid wildcard imports (e.g., `import java.util.*`).
- Group imports: `java.*`, `jakarta.*`, `org.springframework.*`, then local project packages.
- Clean up unused imports before finalizing changes.

### 3.3 Types and Data Handling
- Use **Lombok** to reduce boilerplate:
    - `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor` for Entities/DTOs.
    - `@RequiredArgsConstructor` for constructor-based dependency injection.
    - `@Slf4j` for logging.
- Use **UUID** for primary keys (`java.util.UUID`).
- Use **Java 25** features where appropriate (e.g., Records for DTOs).

### 3.4 Error Handling
- The project uses a global exception handler located in `com.minhnam.microlmssaas.config.exception.GlobalExceptionHandler`.
- **Guideline**: When throwing exceptions for client errors (like missing headers), use meaningful messages that the `GlobalExceptionHandler` can translate into appropriate HTTP status codes.
- Current handled cases:
    - Missing `X-Tenant-ID` header -> `400 Bad Request`.

### 3.5 Multi-tenancy
- The system uses schema-based multi-tenancy.
- Every request should include an `X-Tenant-ID` header.
- The `TenantInterceptor` resolves this header and sets it in `TenantContext`.
- Ensure `TenantContext.clear()` is called after request completion (handled by interceptor).

## 4. Documentation

- **OpenAPI**: The project uses SpringDoc. Access Swagger UI at `/swagger-ui/index.html` when the app is running.
- **Comments**: Focus on "why" rather than "what". Use Javadoc for complex public methods.

## 5. Security & Safety

- **Database**: Do not commit sensitive credentials. Use environment variables or local `application.yaml` (ensure it's in `.gitignore`).
- **Secrets**: Never hardcode API keys or passwords.

## 6. Git Protocol

- Create concise, descriptive commit messages.
- Ensure the project compiles and tests pass before committing.

---
*Note: This file is intended for AI agents. If you are a human, feel free to update these guidelines as the project evolves.*

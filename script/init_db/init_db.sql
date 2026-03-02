-- Tạo bảng quản lý Tenant
CREATE TABLE public.tenants
(
    id           UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    tenant_id    VARCHAR(50) UNIQUE NOT NULL, -- Ví dụ: 'trungtam_anhngu_a'
    schema_name  VARCHAR(50) UNIQUE NOT NULL, -- Ví dụ: 'schema_trungtam_a'
    display_name VARCHAR(255),
    domain_url   VARCHAR(255),
    is_active    BOOLEAN                  DEFAULT TRUE,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Thêm một vài tenant mẫu để test
INSERT INTO public.tenants (tenant_id, schema_name, display_name)
VALUES ('tenant_1', 'schema_tenant_1', 'Trung Tâm Tiếng Anh A'),
       ('tenant_2', 'schema_tenant_2', 'Học Viện Kỹ Năng B');

CREATE DATABASE elearning_platform;

-- Đảm bảo đang ở schema public
SET search_path TO public;

CREATE TABLE tenants
(
    id           UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    tenant_id    VARCHAR(50) UNIQUE NOT NULL, -- Mã định danh (ví dụ: trungtam-a)
    schema_name  VARCHAR(50) UNIQUE NOT NULL, -- Tên schema vật lý (ví dụ: schema_a)
    display_name VARCHAR(255)       NOT NULL,
    is_active    BOOLEAN                  DEFAULT TRUE,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE SCHEMA tenant_template;

-- Tạo bảng User trong schema mẫu
CREATE TABLE tenant_english_center.users
(
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email     VARCHAR(255) UNIQUE NOT NULL,
    full_name VARCHAR(255),
    role      VARCHAR(20) CHECK (role IN ('ADMIN', 'TEACHER', 'STUDENT'))
);

-- Tạo bảng Course trong schema mẫu
CREATE TABLE tenant_template.courses
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP        DEFAULT NOW()
);

INSERT INTO public.tenants (tenant_id, schema_name, display_name)
VALUES ('english-center', 'tenant_english_center', 'Trung Tâm Tiếng Anh');

CREATE SCHEMA tenant_english_center;

-- Thêm user cho Tenant A
INSERT INTO tenant_template.users (email, full_name) VALUES ('admin@template.com', 'Admin Template');

-- Thêm user cho Tenant English Center
INSERT INTO tenant_english_center.users (email, full_name) VALUES ('boss@english.com', 'English Boss');

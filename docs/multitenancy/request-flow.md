# Luồng xử lý Request (Tenant Resolution)

Tài liệu này giải thích cách hệ thống xác định một Request thuộc về khách hàng (Tenant) nào và cách nó chuyển đổi dữ liệu tương ứng.

## 1. Cơ chế Phân giải (Resolution Strategy)
Hệ thống sử dụng `TenantResolverService` để thực hiện việc xác định Tenant theo thứ tự ưu tiên:

### Bước 1: Kiểm tra HTTP Header
Hệ thống tìm kiếm header `X-Tenant-ID`.
*   **Mục đích**: Dành cho Mobile App, Postman, hoặc các tích hợp hệ thống (S2S).
*   **Giá trị**: Có thể truyền trực tiếp `tenantId` (ví dụ: `uit`) hoặc `schemaName` (ví dụ: `tenant_uit`).

### Bước 2: Kiểm tra Domain/Subdomain
Nếu không có Header, hệ thống trích xuất subdomain từ URL.
*   **Cấu trúc**: `{tenant}.{base-domain}`.
*   **Ví dụ**: Truy cập `fpt.microlms.com` -> trích xuất được `fpt`.
*   **Cấu hình**: `base-domain` được định nghĩa trong `application.yaml`.

## 2. Tối ưu hóa với Redis Cache
Việc tra cứu Database cho mỗi Request là một thao tác đắt đỏ. Hệ thống sử dụng Redis để lưu trữ mapping:
*   **Key**: `tenant_domain:{tenantId}` (ví dụ: `tenant_domain:fpt`).
*   **Value**: `schemaName` (ví dụ: `tenant_fpt`).
*   **TTL**: 1 giờ (có thể cấu hình).

**Luồng dữ liệu**:
1. Request đến -> `TenantResolverService` kiểm tra Redis.
2. Nếu Redis có dữ liệu -> Trả về ngay.
3. Nếu Redis trống (Cache Miss) -> Query DB schema `public` -> Lưu vào Redis -> Trả về.

## 3. Quá trình Switch Schema (Hibernate)
Sau khi `TenantInterceptor` xác định được `schemaName` và lưu vào `TenantContext`, Hibernate sẽ thực hiện các bước sau:

1. `TenantIdentifierResolver` lấy ID từ `TenantContext`.
2. `PostgresSchemaMultiTenantConnectionProvider` mượn một Connection từ Pool.
3. Thực thi SQL: `SET search_path TO "schemaName"`.
4. Thực thi các câu lệnh nghiệp vụ (SELECT/INSERT/UPDATE...).
5. Trước khi trả Connection về Pool, thực thi: `SET search_path TO public` để tránh rò rỉ dữ liệu cho các request sau.

## 4. Hướng dẫn Test Subdomain ở Local
Do không có DNS thật ở local, bạn có thể dùng domain `localhost`:
*   URL: `http://uit.localhost:9999/api/courses`.
*   Cấu hình `base-domain` trong `application.yaml` phải là `localhost`.
*   Trình duyệt sẽ tự động hiểu `*.localhost` trỏ về `127.0.0.1`.

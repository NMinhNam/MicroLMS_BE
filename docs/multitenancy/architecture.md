# Kiến trúc Multi-tenancy (Schema-based)

Dự án MicroLMS_BE sử dụng mô hình **Schema-based Isolation** để triển khai Multi-tenancy. Đây là sự cân bằng giữa hiệu năng và khả năng cô lập dữ liệu.

## 1. Các mô hình cô lập dữ liệu
*   **Database-based**: Mỗi Tenant một DB riêng. (An toàn nhất nhưng tốn tài nguyên nhất).
*   **Schema-based (Dự án này)**: Các Tenant dùng chung 1 DB nhưng mỗi người có 1 Schema riêng trong PostgreSQL.
*   **Column-based**: Dùng chung bảng, phân biệt bằng cột `tenant_id`. (Dễ scale nhưng khó cô lập dữ liệu hoàn toàn).

## 2. Cấu trúc Schema trong PostgreSQL
Hệ thống chia làm 2 loại Schema chính:

### 2.1 Schema `public` (Master Schema)
Lưu trữ thông tin quản lý toàn hệ thống, không chứa dữ liệu kinh doanh của khách hàng.
*   **Bảng `tenants`**: Chứa danh sách các tenant, tên schema tương ứng, domain định danh và trạng thái.
*   **Dữ liệu dùng chung**: Các danh mục quốc gia, tiền tệ... (nếu có).

### 2.2 Schema `tenant_xxx` (Tenant-specific Schema)
Mỗi khách hàng khi đăng ký sẽ được cấp một Schema riêng (ví dụ: `tenant_fpt`, `tenant_uit`).
*   **Cô lập**: Tenant A không thể nhìn thấy dữ liệu của Tenant B.
*   **Dữ liệu**: Chứa các bảng `users`, `courses`, `lessons`...
*   **Đồng nhất**: Cấu trúc bảng của mọi Tenant là giống hệt nhau nhờ Flyway Migration.

## 3. Các thành phần Core
*   **`TenantContext`**: Sử dụng `ThreadLocal` để lưu trữ thông tin Tenant hiện tại trong suốt vòng đời của một Request.
*   **`TenantInterceptor`**: Bộ lọc trích xuất Tenant từ Request (Header/Domain).
*   **`PostgresSchemaMultiTenantConnectionProvider`**: Thành phần của Hibernate giúp thực hiện lệnh `SET search_path TO "schemaName"` trước khi thực thi SQL.
*   **`TenantIdentifierResolver`**: Giúp Hibernate biết cần lấy ID Tenant từ đâu (trích xuất từ `TenantContext`).

## 4. Ưu điểm của kiến trúc này
1.  **Bảo mật**: Dữ liệu được cô lập ở mức vật lý (Schema).
2.  **Dễ bảo trì**: Backup/Restore từng khách hàng dễ dàng bằng cách backup schema.
3.  **Performance**: Tốt hơn mô hình DB-per-tenant vì dùng chung Connection Pool.

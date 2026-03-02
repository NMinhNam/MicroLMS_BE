# Hướng dẫn Phát triển & Vận hành

Dành cho lập trình viên làm việc trên dự án MicroLMS_BE để phát triển thêm tính năng mà không phá vỡ cấu trúc Multi-tenancy.

## 1. Cách thêm bảng/cột mới (Database Migration)
Khi bạn cần thêm một bảng mới (ví dụ: `lessons`) cho tất cả khách hàng:

1.  Tạo file migration mới tại: `src/main/resources/db/migration/tenants/`.
2.  Đặt tên đúng chuẩn: `V2__create_lessons_table.sql`.
3.  Viết code SQL (không cần quan tâm schema, vì Flyway sẽ tự apply vào schema đích).
4.  Khi khởi động lại hoặc khi tạo Tenant mới, Flyway sẽ tự động chạy script này.
5.  **Lưu ý**: Đối với các Tenant đã tồn tại, bạn cần gọi một API hoặc script để trigger Flyway migrate cho tất cả schema cũ.

## 2. Thêm Module nghiệp vụ mới
Khi tạo module mới (ví dụ: `Lesson`):
1.  Sử dụng cấu trúc: `com.minhnam.microlmssaas.modules.lesson`.
2.  Entity/Repository không cần cấu hình gì đặc biệt cho Multi-tenancy (Hibernate đã lo phần này).
3.  **Quan trọng**: Luôn đảm bảo Request gửi lên có Header `X-Tenant-ID` hoặc dùng Subdomain đúng để Hibernate biết cần lưu dữ liệu vào đâu.

## 3. Cấu hình Môi trường (Environment)

### 3.1 Local Development
*   `base-domain`: `localhost`.
*   Truy cập: `http://{tenant}.localhost:9999`.
*   Redis: Cần cài đặt Redis local (Cổng 6379).

### 3.2 Production
*   `base-domain`: `microlms.com`.
*   Wildcard DNS: Cấu hình `*.microlms.com` trỏ về IP của Server.
*   Redis: Sử dụng Managed Redis (AWS ElastiCache, Azure Redis) để đảm bảo HA.

## 4. Xử lý sự cố (Troubleshooting)

### 4.1 Lỗi "relation xxx does not exist"
*   **Nguyên nhân**: Request vào sai schema (thường là vào `public`) hoặc Flyway chưa chạy script tạo bảng cho schema đó.
*   **Kiểm tra**: Xem log `>>> Resolved Tenant Schema` để biết hệ thống đang chọn schema nào.

### 4.2 Lỗi treo khi tạo Tenant
*   **Nguyên nhân**: Lock database hoặc nghẽn connection pool.
*   **Kiểm tra**: Log của `TenantSchemaService`. Đảm bảo Redis đang hoạt động bình thường.

### 4.3 Lỗi Cache không cập nhật
*   Nếu bạn đổi tên schema thủ công trong DB, hãy nhớ xóa key tương ứng trong Redis: `DEL tenant_domain:{tenantId}`.

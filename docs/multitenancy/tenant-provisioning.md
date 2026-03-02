# Quy trình Khởi tạo Tenant (Provisioning)

Tài liệu này mô tả quy trình "Dựng" một môi trường hoàn chỉnh cho khách hàng mới ngay khi họ đăng ký.

## 1. Luồng xử lý Bất đồng bộ (@Async)
Việc khởi tạo một Database Schema là tác vụ nặng nề. Do đó, hệ thống chia làm 2 giai đoạn:
1.  **Giai đoạn Đồng bộ (Sync)**: Lưu thông tin cơ bản vào bảng `tenants` ở schema `public`. Trả về kết quả ngay cho User để họ không phải chờ.
2.  **Giai đoạn Bất đồng bộ (Async)**: Thực thi ngầm việc tạo schema, tạo bảng và dữ liệu mẫu.

## 2. Chi tiết 5 bước khởi tạo (The Senior Flow)

### Bước 1: Lưu Master Data
Lưu thông tin định danh vào `public.tenants`. Bước này quan trọng để các request sau này có thể "ánh xạ" domain về schema.

### Bước 2: Tạo Physical Schema
Thực thi lệnh SQL: `CREATE SCHEMA IF NOT EXISTS "tenant_xxx"`. Tên schema được bao bởi dấu ngoặc kép để hỗ trợ các ký tự đặc biệt.

### Bước 3: Migration (Flyway Programmatic)
Thay vì dùng Flyway tự động khi khởi động app, chúng ta khởi tạo Flyway bằng code:
*   `locations`: Trỏ đến `db/migration/tenants`.
*   `schemas`: Chỉ định đúng schema vừa tạo.
*   **Kết quả**: Toàn bộ cấu trúc bảng (`users`, `courses`...) được tạo ra trong schema mới.

### Bước 4: Tạo tài khoản Admin mặc định
Tự động chèn 1 bản ghi vào bảng `users` trong schema mới. Sử dụng lệnh `INSERT ... ON CONFLICT DO NOTHING` để đảm bảo an toàn nếu quá trình bị chạy lại.

### Bước 5: Cache Warming
Thông tin mapping được lưu vào Redis để các request đầu tiên của khách hàng không bị "Cache miss" (giảm độ trễ).

## 3. Các lưu ý quan trọng
*   **Tách Bean**: Phương thức `@Async` phải nằm ở một Class riêng (`TenantSchemaService`) để Spring Proxy có thể hoạt động. Nếu gọi trong cùng 1 Class, nó sẽ chạy đồng bộ (Synchronous) và gây treo API.
*   **Idempotency**: Quy trình khởi tạo được thiết kế để có thể chạy lại nhiều lần mà không gây lỗi (Dùng `IF NOT EXISTS` và `ON CONFLICT`).
*   **Monitoring**: Theo dõi thời gian thực thi qua Logs để phát hiện sự cố (ví dụ: Flyway quét script quá chậm).

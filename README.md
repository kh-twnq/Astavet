# AstaVet Store

MVP website bán hàng dành cho AstaVet với storefront Next.js, backend Java Spring Boot, PostgreSQL và thanh toán COD.

## Tính năng

- Xem danh sách và chi tiết sản phẩm.
- Giỏ hàng lưu trên trình duyệt.
- Đặt hàng COD bằng tên, số điện thoại và địa chỉ.
- Server tự xác minh giá, tồn kho và chống tạo đơn trùng.
- Quản trị sản phẩm, tồn kho và trạng thái đơn hàng.
- Quản trị nhiều hình ảnh, nhiều biến thể và phân trang đơn hàng.
- Session admin, CSRF, CORS giới hạn origin và rate-limit cơ bản.

## Yêu cầu môi trường

- Java 21.
- Node.js 20.17 trở lên.
- PostgreSQL 15 trở lên, hoặc Docker Compose.

## Chạy PostgreSQL

Sao chép `.env.example` thành `.env`, thay toàn bộ mật khẩu mặc định, sau đó chạy:

```bash
docker compose up -d postgres
```

Nếu không dùng Docker, tạo database và user khớp các biến `DATABASE_*` trong `.env`.

## Chạy backend

Nạp các biến trong `.env` vào shell, sau đó:

```bash
cd backend
./gradlew bootRun
```

Backend chạy tại `http://localhost:8080`. Swagger UI nằm tại `http://localhost:8080/swagger-ui.html`.

Tài khoản quản trị được tạo ở lần khởi động đầu tiên từ `ADMIN_EMAIL` và `ADMIN_PASSWORD`. Thay đổi biến môi trường sau đó không tự đổi mật khẩu trong database.

## Chạy frontend

Sao chép `frontend/.env.local.example` thành `frontend/.env.local`, sau đó:

```bash
cd frontend
npm install
npm run dev
```

Storefront chạy tại `http://localhost:3000`; trang quản trị tại `http://localhost:3000/admin/login`.

## Kiểm tra

Repo tích hợp ECC cho Codex và Java/Spring Boot. Xem [hướng dẫn ECC](docs/ecc/README.md)
để biết nguồn, skills và các giới hạn kiểm tra. Chạy từ thư mục gốc:

```bash
bash scripts/verify.sh ecc
bash scripts/verify.sh backend
# Hoặc kiểm tra cả backend và frontend
bash scripts/verify.sh all
```

```bash
cd backend
./gradlew test

# Integration test PostgreSQL (dùng database test riêng)
ASTAVET_TEST_DATABASE_URL=jdbc:postgresql://localhost:5432/astavet_integration_test \
ASTAVET_TEST_DATABASE_USERNAME=postgres \
ASTAVET_TEST_DATABASE_PASSWORD=postgres \
./gradlew test --tests '*IntegrationTest'

cd ../frontend
npm test
npm run lint
npm run build
npm audit
```

## Quy tắc đơn hàng

Luồng trạng thái hợp lệ:

```text
NEW → CONFIRMED → PACKING → SHIPPING → DELIVERED
  └──────────────→ CANCELLED
SHIPPING ─────────→ RETURNED
DELIVERED ─────────→ RETURNED
```

Tồn kho được giữ ngay khi đơn được tạo. Giá và tổng tiền trong trình duyệt chỉ mang tính hiển thị; backend luôn tính lại từ database trong transaction.
Đơn hủy trước khi giao hoàn tồn kho đúng một lần. COD chỉ được xác nhận đã thu sau khi giao thành công;
hoàn tiền chỉ được xác nhận sau khi đơn chuyển sang `RETURNED`. Mọi thay đổi thanh toán đều lưu người thực hiện và thời điểm.

## Cấu hình sản xuất

- Bật `SESSION_COOKIE_SECURE=true` và chỉ phục vụ qua HTTPS.
- Đặt frontend/backend sau cùng một reverse proxy và cấu hình đúng `FRONTEND_ORIGIN`.
- Dùng mật khẩu database/admin mạnh qua secret manager, không commit file `.env`.
- Thay rate-limit trong bộ nhớ bằng Redis hoặc gateway rate-limit khi chạy nhiều backend instance.
- Thiết lập backup PostgreSQL, giám sát lỗi và chính sách lưu dữ liệu khách hàng.

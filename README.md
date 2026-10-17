# 📚 Website quản lý chuỗi cửa hàng sách cũ

Đề tài 14 – Đồ án cuối kỳ.

**Công nghệ:** Spring Boot 4 · Thymeleaf · Bootstrap 5 · Spring Data JPA · SQL Server · Spring Security + JWT · WebSocket (STOMP) · Cloudinary

## 👥 Thành viên & phân công

| Thành viên | Vai trò phụ trách |
|---|---|
| TV1 – ... | Guest + User (trang chủ, tìm kiếm/lọc, giỏ hàng, thanh toán COD/VNPAY/MOMO, đơn hàng, đánh giá) |
| TV2 – ... | Vendor + Shipper + WebSocket (shop, sản phẩm, đơn hàng theo trạng thái, khuyến mãi, doanh thu, chat/thông báo realtime) |
| TV3 – ... (Trưởng nhóm) | Admin + Manager + Bảo mật (đăng ký OTP Email, JWT, BCrypt, Cloudinary, quản lý user/shop/chiết khấu/khuyến mãi/vận chuyển) |

## 🏗️ Cấu trúc 3 tầng

```
src/main/java/com/bookstore/bookstore/
 ├─ config/        Cấu hình (Security, WebSocket, Cloudinary)
 ├─ security/      JWT filter, JwtService, UserDetailsService
 ├─ entity/        JPA entities
 ├─ enums/         Enum dùng chung (OrderStatus, RoleName)
 ├─ repository/    Data Access Layer
 ├─ service/       Business Layer (interface)
 │   └─ impl/      Business Layer (implementation)
 ├─ controller/    Presentation Layer
 ├─ dto/  mapper/  exception/  util/
src/main/resources/
 ├─ templates/     fragments, guest, user, vendor, shipper, manager, admin, auth, error
 ├─ static/        css, js, images
 ├─ application.properties                 (cấu hình chung – có commit)
 └─ application-local.properties.example   (file mẫu cấu hình bí mật)
```

## ⚙️ Hướng dẫn cài đặt

1. Cài **JDK 21+**, **SQL Server** + SSMS, **Git**, IntelliJ IDEA.
2. Clone & chuyển sang nhánh `develop`:
   ```bash
   git clone https://github.com/hoangducvo1806-ui/bookstore-chain-springboot.git
   cd bookstore-chain-springboot
   git checkout develop
   ```
3. Tạo database trong SSMS:
   ```sql
   CREATE DATABASE bookstore_db;
   ```
4. Copy `src/main/resources/application-local.properties.example` thành
   `src/main/resources/application-local.properties` và điền mật khẩu SQL Server, Gmail, Cloudinary của máy bạn.
   > File này nằm trong `.gitignore`, **không bao giờ commit**.
5. Chạy:
   ```bash
   ./mvnw spring-boot:run
   ```
   Mở http://localhost:8080

## 🌿 Quy trình Git

- `main`: bản ổn định để nộp · `develop`: nhánh tích hợp · `feature/<tv>-<chuc-nang>`: nhánh làm việc
- Luôn tạo nhánh từ `develop`, làm xong tạo **Pull Request vào `develop`**, cần 1 người review.
- Commit message: `feat(...)`, `fix(...)`, `ui(...)`, `refactor(...)`, `docs(...)`
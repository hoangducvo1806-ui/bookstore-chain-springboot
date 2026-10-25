-- =========================================================================================
-- ĐỒ ÁN MÔN HỌC: XÂY DỰNG WEBSITE QUẢN LÝ CHUỖI CỬA HÀNG SÁCH CŨ (ĐỀ TÀI 14)
-- HỆ QUẢN TRỊ CSDL: MICROSOFT SQL SERVER 2019 / 2022
-- NGÀY KHỞI TẠO: 2026-10-25
-- =========================================================================================

IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = N'bookstore_db')
BEGIN
    CREATE DATABASE bookstore_db COLLATE Latin1_General_100_CI_AS_SC_UTF8;
END
GO

USE bookstore_db;
GO

-- 1. BẢNG ROLES (VAI TRÒ TRONG HỆ THỐNG)
IF OBJECT_ID(N'dbo.roles', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.roles (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(50) NOT NULL UNIQUE,
        description NVARCHAR(255) NULL
    );
END
GO

-- 2. BẢNG USERS (NGƯỜI DÙNG: ADMIN, MANAGER, VENDOR, SHIPPER, KHÁCH HÀNG)
IF OBJECT_ID(N'dbo.users', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.users (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        email NVARCHAR(150) NOT NULL UNIQUE,
        password NVARCHAR(255) NOT NULL,
        full_name NVARCHAR(150) NOT NULL,
        phone NVARCHAR(20) NULL,
        avatar_url NVARCHAR(500) NULL,
        enabled BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
    );
END
GO

-- 3. BẢNG LIÊN KẾT USER_ROLES (N - N)
IF OBJECT_ID(N'dbo.user_roles', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.user_roles (
        user_id BIGINT NOT NULL,
        role_id BIGINT NOT NULL,
        PRIMARY KEY (user_id, role_id),
        CONSTRAINT FK_user_roles_user FOREIGN KEY (user_id) REFERENCES dbo.users(id) ON DELETE CASCADE,
        CONSTRAINT FK_user_roles_role FOREIGN KEY (role_id) REFERENCES dbo.roles(id) ON DELETE CASCADE
    );
END
GO

-- 4. BẢNG ADDRESSES (SỔ ĐỊA CHỈ NHẬN HÀNG CỦA KHÁCH HÀNG)
IF OBJECT_ID(N'dbo.addresses', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.addresses (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        recipient_name NVARCHAR(100) NOT NULL,
        phone NVARCHAR(20) NOT NULL,
        street_address NVARCHAR(255) NOT NULL,
        ward NVARCHAR(100) NOT NULL,
        district NVARCHAR(100) NOT NULL,
        province NVARCHAR(100) NOT NULL,
        is_default BIT NOT NULL DEFAULT 0,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_addresses_user FOREIGN KEY (user_id) REFERENCES dbo.users(id) ON DELETE CASCADE
    );
END
GO

-- 5. BẢNG SHOPS (CHI NHÁNH CỬA HÀNG SÁCH CŨ TRONG CHUỖI)
IF OBJECT_ID(N'dbo.shops', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.shops (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        owner_id BIGINT NOT NULL UNIQUE,
        name NVARCHAR(200) NOT NULL,
        description NVARCHAR(1000) NULL,
        address NVARCHAR(255) NOT NULL,
        phone NVARCHAR(20) NOT NULL,
        avatar_url NVARCHAR(500) NULL,
        active BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_shops_owner FOREIGN KEY (owner_id) REFERENCES dbo.users(id)
    );
END
GO

-- 6. BẢNG CATEGORIES (THỂ LOẠI / PHÂN LOẠI SÁCH)
IF OBJECT_ID(N'dbo.categories', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.categories (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(100) NOT NULL,
        slug NVARCHAR(120) NOT NULL UNIQUE,
        description NVARCHAR(500) NULL,
        parent_id BIGINT NULL,
        active BIT NOT NULL DEFAULT 1,
        CONSTRAINT FK_categories_parent FOREIGN KEY (parent_id) REFERENCES dbo.categories(id)
    );
END
GO

-- 7. BẢNG BOOKS (SÁCH CŨ TRONG KHO CÁC CHI NHÁNH)
IF OBJECT_ID(N'dbo.books', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.books (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        shop_id BIGINT NOT NULL,
        category_id BIGINT NOT NULL,
        title NVARCHAR(255) NOT NULL,
        isbn NVARCHAR(50) NOT NULL UNIQUE,
        author NVARCHAR(150) NOT NULL,
        publisher NVARCHAR(150) NULL,
        publish_year INT NULL,
        book_condition NVARCHAR(30) NOT NULL, -- LIKE_NEW, VERY_GOOD, GOOD, ACCEPTABLE, OLD
        condition_note NVARCHAR(1000) NULL,
        original_price DECIMAL(18,2) NULL,
        price DECIMAL(18,2) NOT NULL,
        stock INT NOT NULL DEFAULT 0,
        sold_quantity INT NOT NULL DEFAULT 0,
        main_image_url NVARCHAR(500) NULL,
        active BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_books_shop FOREIGN KEY (shop_id) REFERENCES dbo.shops(id),
        CONSTRAINT FK_books_category FOREIGN KEY (category_id) REFERENCES dbo.categories(id)
    );
END
GO

-- 8. BẢNG BOOK_IMAGES (ẢNH CHI TIẾT CỦA SÁCH CŨ)
IF OBJECT_ID(N'dbo.book_images', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.book_images (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        book_id BIGINT NOT NULL,
        image_url NVARCHAR(500) NOT NULL,
        display_order INT NOT NULL DEFAULT 0,
        CONSTRAINT FK_book_images_book FOREIGN KEY (book_id) REFERENCES dbo.books(id) ON DELETE CASCADE
    );
END
GO

-- 9. BẢNG SHIPPING_CARRIERS (ĐỐI TÁC VẬN CHUYỂN)
IF OBJECT_ID(N'dbo.shipping_carriers', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.shipping_carriers (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(100) NOT NULL UNIQUE,
        shipping_fee DECIMAL(18,2) NOT NULL,
        description NVARCHAR(255) NULL,
        active BIT NOT NULL DEFAULT 1
    );
END
GO

-- 10. BẢNG VOUCHERS (MÃ GIẢM GIÁ KHUYẾN MÃI)
IF OBJECT_ID(N'dbo.vouchers', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.vouchers (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        code NVARCHAR(50) NOT NULL UNIQUE,
        title NVARCHAR(150) NOT NULL,
        discount_type NVARCHAR(30) NOT NULL, -- PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING
        discount_value DECIMAL(18,2) NOT NULL,
        min_order_amount DECIMAL(18,2) NULL,
        max_discount_amount DECIMAL(18,2) NULL,
        start_date DATETIME2 NOT NULL,
        end_date DATETIME2 NOT NULL,
        usage_limit INT NOT NULL DEFAULT 100,
        used_count INT NOT NULL DEFAULT 0,
        active BIT NOT NULL DEFAULT 1
    );
END
GO

-- 11. BẢNG APP_COMMISSIONS (TỶ LỆ CHIẾT KHẤU HOA HỒNG CHI NHÁNH)
IF OBJECT_ID(N'dbo.app_commissions', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.app_commissions (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        shop_id BIGINT NOT NULL UNIQUE,
        commission_rate DECIMAL(5,2) NOT NULL DEFAULT 10.00,
        note NVARCHAR(255) NULL,
        updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_app_commissions_shop FOREIGN KEY (shop_id) REFERENCES dbo.shops(id) ON DELETE CASCADE
    );
END
GO

-- 12. BẢNG CARTS (GIỎ HÀNG LƯU TRONG DB CỦA KHÁCH)
IF OBJECT_ID(N'dbo.carts', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.carts (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL UNIQUE,
        updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_carts_user FOREIGN KEY (user_id) REFERENCES dbo.users(id) ON DELETE CASCADE
    );
END
GO

-- 13. BẢNG CART_ITEMS (CHI TIẾT MỤC SÁCH TRONG GIỎ)
IF OBJECT_ID(N'dbo.cart_items', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.cart_items (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        cart_id BIGINT NOT NULL,
        book_id BIGINT NOT NULL,
        quantity INT NOT NULL DEFAULT 1,
        CONSTRAINT FK_cart_items_cart FOREIGN KEY (cart_id) REFERENCES dbo.carts(id) ON DELETE CASCADE,
        CONSTRAINT FK_cart_items_book FOREIGN KEY (book_id) REFERENCES dbo.books(id)
    );
END
GO

-- 14. BẢNG ORDERS (ĐƠN HÀNG TOÀN CHUỖI)
IF OBJECT_ID(N'dbo.orders', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.orders (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        order_code NVARCHAR(50) NOT NULL UNIQUE,
        customer_id BIGINT NOT NULL,
        shop_id BIGINT NOT NULL,
        shipper_id BIGINT NULL,
        voucher_id BIGINT NULL,
        shipping_carrier_id BIGINT NULL,
        status NVARCHAR(30) NOT NULL, -- PENDING, CONFIRMED, PROCESSING, SHIPPING, DELIVERED, CANCELLED, RETURNED, REFUNDED
        payment_method NVARCHAR(30) NOT NULL, -- COD, VNPAY, MOMO
        payment_status NVARCHAR(30) NOT NULL, -- PENDING, COMPLETED, FAILED, REFUNDED
        subtotal DECIMAL(18,2) NOT NULL,
        shipping_fee DECIMAL(18,2) NOT NULL DEFAULT 0,
        discount_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
        total_amount DECIMAL(18,2) NOT NULL,
        recipient_name NVARCHAR(100) NOT NULL,
        recipient_phone NVARCHAR(20) NOT NULL,
        shipping_address NVARCHAR(255) NOT NULL,
        note NVARCHAR(500) NULL,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_orders_customer FOREIGN KEY (customer_id) REFERENCES dbo.users(id),
        CONSTRAINT FK_orders_shop FOREIGN KEY (shop_id) REFERENCES dbo.shops(id),
        CONSTRAINT FK_orders_shipper FOREIGN KEY (shipper_id) REFERENCES dbo.users(id),
        CONSTRAINT FK_orders_voucher FOREIGN KEY (voucher_id) REFERENCES dbo.vouchers(id),
        CONSTRAINT FK_orders_carrier FOREIGN KEY (shipping_carrier_id) REFERENCES dbo.shipping_carriers(id)
    );
END
GO

-- 15. BẢNG ORDER_ITEMS (CHI TIẾT SẢN PHẨM TRONG ĐƠN)
IF OBJECT_ID(N'dbo.order_items', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.order_items (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        order_id BIGINT NOT NULL,
        book_id BIGINT NOT NULL,
        book_title NVARCHAR(255) NOT NULL,
        price DECIMAL(18,2) NOT NULL,
        quantity INT NOT NULL,
        subtotal DECIMAL(18,2) NOT NULL,
        CONSTRAINT FK_order_items_order FOREIGN KEY (order_id) REFERENCES dbo.orders(id) ON DELETE CASCADE,
        CONSTRAINT FK_order_items_book FOREIGN KEY (book_id) REFERENCES dbo.books(id)
    );
END
GO

-- 16. BẢNG ORDER_STATUS_HISTORIES (LỊCH SỬ THAY ĐỔI TRẠNG THÁI ĐƠN HÀNG)
IF OBJECT_ID(N'dbo.order_status_histories', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.order_status_histories (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        order_id BIGINT NOT NULL,
        status NVARCHAR(30) NOT NULL,
        note NVARCHAR(255) NULL,
        changed_by NVARCHAR(100) NULL,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_order_status_histories_order FOREIGN KEY (order_id) REFERENCES dbo.orders(id) ON DELETE CASCADE
    );
END
GO

-- 17. BẢNG PAYMENTS (LỊCH SỬ GIAO DỊCH THANH TOÁN VNPAY / MOMO)
IF OBJECT_ID(N'dbo.payments', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.payments (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        order_id BIGINT NOT NULL,
        transaction_no NVARCHAR(100) NULL,
        bank_code NVARCHAR(50) NULL,
        amount DECIMAL(18,2) NOT NULL,
        payment_method NVARCHAR(30) NOT NULL,
        status NVARCHAR(30) NOT NULL,
        response_code NVARCHAR(20) NULL,
        pay_date DATETIME2 NULL,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_payments_order FOREIGN KEY (order_id) REFERENCES dbo.orders(id) ON DELETE CASCADE
    );
END
GO

-- 18. BẢNG REVIEWS (ĐÁNH GIÁ SÁCH CŨ: COMMENT >= 50 KÝ TỰ, RATING 1-5 SAO)
IF OBJECT_ID(N'dbo.reviews', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.reviews (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        book_id BIGINT NOT NULL,
        user_id BIGINT NOT NULL,
        order_id BIGINT NULL,
        rating INT NOT NULL,
        comment NVARCHAR(2000) NOT NULL,
        active BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_reviews_book FOREIGN KEY (book_id) REFERENCES dbo.books(id),
        CONSTRAINT FK_reviews_user FOREIGN KEY (user_id) REFERENCES dbo.users(id),
        CONSTRAINT FK_reviews_order FOREIGN KEY (order_id) REFERENCES dbo.orders(id)
    );
END
GO

-- 19. BẢNG REVIEW_MEDIA (ẢNH / VIDEO MINH HỨNG CHO ĐÁNH GIÁ TRÊN CLOUDINARY)
IF OBJECT_ID(N'dbo.review_media', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.review_media (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        review_id BIGINT NOT NULL,
        media_url NVARCHAR(500) NOT NULL,
        media_type NVARCHAR(20) NOT NULL, -- IMAGE, VIDEO
        CONSTRAINT FK_review_media_review FOREIGN KEY (review_id) REFERENCES dbo.reviews(id) ON DELETE CASCADE
    );
END
GO

-- 20. BẢNG WISHLISTS (DANH SÁCH SÁCH YÊU THÍCH CỦA KHÁCH HÀNG)
IF OBJECT_ID(N'dbo.wishlists', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.wishlists (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        book_id BIGINT NOT NULL,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_wishlists_user FOREIGN KEY (user_id) REFERENCES dbo.users(id) ON DELETE CASCADE,
        CONSTRAINT FK_wishlists_book FOREIGN KEY (book_id) REFERENCES dbo.books(id) ON DELETE CASCADE
    );
END
GO

-- 21. BẢNG VIEWED_PRODUCTS (LỊCH SỬ SÁCH ĐÃ XEM GẦN ĐÂY)
IF OBJECT_ID(N'dbo.viewed_products', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.viewed_products (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        book_id BIGINT NOT NULL,
        viewed_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_viewed_products_user FOREIGN KEY (user_id) REFERENCES dbo.users(id) ON DELETE CASCADE,
        CONSTRAINT FK_viewed_products_book FOREIGN KEY (book_id) REFERENCES dbo.books(id) ON DELETE CASCADE
    );
END
GO

-- 22. BẢNG CHAT_MESSAGES (TIN NHẮN REALTIME WEBSOCKET KHÁCH HÀNG <-> CHỦ SHOP)
IF OBJECT_ID(N'dbo.chat_messages', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.chat_messages (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        sender_id BIGINT NOT NULL,
        recipient_id BIGINT NOT NULL,
        content NVARCHAR(2000) NOT NULL,
        is_read BIT NOT NULL DEFAULT 0,
        sent_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_chat_sender FOREIGN KEY (sender_id) REFERENCES dbo.users(id),
        CONSTRAINT FK_chat_recipient FOREIGN KEY (recipient_id) REFERENCES dbo.users(id)
    );
END
GO

-- =========================================================================================
-- PHẦN DỮ LIỆU BAN ĐẦU (SEED DATA MẪU)
-- MẬT KHẨU MẶC ĐỊNH CHO TẤT CẢ CÁC TÀI KHOẢN: 123456 (Bcrypt Hash)
-- $2a$10$w8.4/d/g1XlS.6uS9c0eMOPGfFfK4p1U07DqjD5C7s/pYcZlK9g.q
-- =========================================================================================

-- Seed Roles
IF NOT EXISTS (SELECT 1 FROM dbo.roles WHERE name = 'ADMIN')
    INSERT INTO dbo.roles (name, description) VALUES ('ADMIN', N'Quản trị viên toàn hệ thống chuỗi');
IF NOT EXISTS (SELECT 1 FROM dbo.roles WHERE name = 'MANAGER')
    INSERT INTO dbo.roles (name, description) VALUES ('MANAGER', N'Quản lý vận hành chuỗi cửa hàng');
IF NOT EXISTS (SELECT 1 FROM dbo.roles WHERE name = 'VENDOR')
    INSERT INTO dbo.roles (name, description) VALUES ('VENDOR', N'Chủ cửa hàng chi nhánh bán sách cũ');
IF NOT EXISTS (SELECT 1 FROM dbo.roles WHERE name = 'SHIPPER')
    INSERT INTO dbo.roles (name, description) VALUES ('SHIPPER', N'Nhân viên giao nhận đơn hàng');
IF NOT EXISTS (SELECT 1 FROM dbo.roles WHERE name = 'USER')
    INSERT INTO dbo.roles (name, description) VALUES ('USER', N'Khách hàng mua sách');
GO

-- Seed Accounts (Pass: 123456 -> $2a$10$tZzC8g6vX9s21cEa3Q8YpuxoR93rP4H57tqZ5WdM1oF3hK8/p3z9O)
-- Lưu ý: Khi ứng dụng chạy lần đầu, Spring Security DataInitializer sẽ tự sinh và encode chuẩn xác
GO

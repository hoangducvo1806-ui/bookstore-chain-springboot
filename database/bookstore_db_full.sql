-- =========================================================================================
-- DO AN: WEBSITE QUAN LY CHUOI CUA HANG SACH CU (DE TAI 14) - FULL DATABASE DUMP
-- He quan tri: Microsoft SQL Server 2019/2022 | Database: bookstore_db
-- Ngay xuat: 2026-10-28 10:46 | Gom: CREATE TABLE + PK + FK + INSERT du lieu mau
-- Mat khau tat ca tai khoan mau: 123456 (BCrypt)
-- Cach dung: mo file trong SSMS -> Execute (F5). Script tu tao database neu chua co.
-- =========================================================================================
IF DB_ID(N'bookstore_db') IS NULL CREATE DATABASE bookstore_db COLLATE Latin1_General_100_CI_AS_SC_UTF8;
GO
USE bookstore_db;
GO

-- ===== XOA BANG CU (THEO THU TU NGUOC KHOA NGOAI) =====
IF OBJECT_ID(N'dbo.review_media', N'U') IS NOT NULL DROP TABLE dbo.[review_media];
IF OBJECT_ID(N'dbo.order_status_histories', N'U') IS NOT NULL DROP TABLE dbo.[order_status_histories];
IF OBJECT_ID(N'dbo.order_items', N'U') IS NOT NULL DROP TABLE dbo.[order_items];
IF OBJECT_ID(N'dbo.book_images', N'U') IS NOT NULL DROP TABLE dbo.[book_images];
IF OBJECT_ID(N'dbo.wishlists', N'U') IS NOT NULL DROP TABLE dbo.[wishlists];
IF OBJECT_ID(N'dbo.viewed_products', N'U') IS NOT NULL DROP TABLE dbo.[viewed_products];
IF OBJECT_ID(N'dbo.reviews', N'U') IS NOT NULL DROP TABLE dbo.[reviews];
IF OBJECT_ID(N'dbo.payments', N'U') IS NOT NULL DROP TABLE dbo.[payments];
IF OBJECT_ID(N'dbo.orders', N'U') IS NOT NULL DROP TABLE dbo.[orders];
IF OBJECT_ID(N'dbo.chat_messages', N'U') IS NOT NULL DROP TABLE dbo.[chat_messages];
IF OBJECT_ID(N'dbo.cart_items', N'U') IS NOT NULL DROP TABLE dbo.[cart_items];
IF OBJECT_ID(N'dbo.books', N'U') IS NOT NULL DROP TABLE dbo.[books];
IF OBJECT_ID(N'dbo.app_commissions', N'U') IS NOT NULL DROP TABLE dbo.[app_commissions];
IF OBJECT_ID(N'dbo.user_roles', N'U') IS NOT NULL DROP TABLE dbo.[user_roles];
IF OBJECT_ID(N'dbo.shops', N'U') IS NOT NULL DROP TABLE dbo.[shops];
IF OBJECT_ID(N'dbo.carts', N'U') IS NOT NULL DROP TABLE dbo.[carts];
IF OBJECT_ID(N'dbo.addresses', N'U') IS NOT NULL DROP TABLE dbo.[addresses];
IF OBJECT_ID(N'dbo.vouchers', N'U') IS NOT NULL DROP TABLE dbo.[vouchers];
IF OBJECT_ID(N'dbo.users', N'U') IS NOT NULL DROP TABLE dbo.[users];
IF OBJECT_ID(N'dbo.shipping_carriers', N'U') IS NOT NULL DROP TABLE dbo.[shipping_carriers];
IF OBJECT_ID(N'dbo.roles', N'U') IS NOT NULL DROP TABLE dbo.[roles];
IF OBJECT_ID(N'dbo.otp_tokens', N'U') IS NOT NULL DROP TABLE dbo.[otp_tokens];
IF OBJECT_ID(N'dbo.categories', N'U') IS NOT NULL DROP TABLE dbo.[categories];
GO

-- ===== BANG: categories =====
CREATE TABLE dbo.[categories] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [active] bit NOT NULL,
    [description] nvarchar(255) NULL,
    [name] nvarchar(100) NOT NULL,
    [slug] varchar(120) NOT NULL,
    CONSTRAINT [PK__categori__3213E83F1607CB3B] PRIMARY KEY ([id]),
    CONSTRAINT [UKoul14ho7bctbefv8jywp5v3i2] UNIQUE ([slug]),
    CONSTRAINT [UQ_categories_name] UNIQUE ([name])
);
GO

-- ===== BANG: otp_tokens =====
CREATE TABLE dbo.[otp_tokens] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [email] varchar(100) NOT NULL,
    [expires_at] datetime2(7) NOT NULL,
    [otp_code] varchar(10) NOT NULL,
    [type] varchar(30) NOT NULL,
    [used] bit NOT NULL,
    CONSTRAINT [PK__otp_toke__3213E83FD59B18D9] PRIMARY KEY ([id])
);
GO

-- ===== BANG: roles =====
CREATE TABLE dbo.[roles] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [description] nvarchar(255) NULL,
    [name] varchar(30) NOT NULL,
    CONSTRAINT [PK__roles__3213E83FB09FFFEF] PRIMARY KEY ([id]),
    CONSTRAINT [UKofx66keruapi6vyqpv6f2or37] UNIQUE ([name])
);
GO

-- ===== BANG: shipping_carriers =====
CREATE TABLE dbo.[shipping_carriers] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [active] bit NOT NULL,
    [description] nvarchar(255) NULL,
    [name] nvarchar(100) NOT NULL,
    [shipping_fee] numeric(12,2) NOT NULL,
    CONSTRAINT [PK__shipping__3213E83F32034D05] PRIMARY KEY ([id]),
    CONSTRAINT [UQ_shipping_carriers_name] UNIQUE ([name])
);
GO

-- ===== BANG: users =====
CREATE TABLE dbo.[users] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [avatar_url] varchar(500) NULL,
    [created_at] datetime2(7) NOT NULL,
    [email] varchar(100) NOT NULL,
    [enabled] bit NOT NULL,
    [full_name] nvarchar(100) NOT NULL,
    [locked] bit NOT NULL,
    [password] varchar(255) NOT NULL,
    [phone] varchar(20) NULL,
    [updated_at] datetime2(7) NOT NULL,
    CONSTRAINT [PK__users__3213E83F0484370D] PRIMARY KEY ([id]),
    CONSTRAINT [UK6dotkott2kjsp8vw4d0m25fb7] UNIQUE ([email])
);
GO

-- ===== BANG: vouchers =====
CREATE TABLE dbo.[vouchers] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [active] bit NOT NULL,
    [code] varchar(50) NOT NULL,
    [discount_type] varchar(30) NOT NULL,
    [discount_value] numeric(12,2) NOT NULL,
    [end_date] datetime2(7) NULL,
    [max_discount_amount] numeric(12,2) NULL,
    [min_order_amount] numeric(12,2) NULL,
    [start_date] datetime2(7) NULL,
    [title] nvarchar(150) NOT NULL,
    [usage_limit] int NULL,
    [used_count] int NULL,
    CONSTRAINT [PK__vouchers__3213E83F883F13B3] PRIMARY KEY ([id]),
    CONSTRAINT [UK30ftp2biebbvpik8e49wlmady] UNIQUE ([code])
);
GO

-- ===== BANG: addresses =====
CREATE TABLE dbo.[addresses] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [district] nvarchar(100) NOT NULL,
    [is_default] bit NOT NULL,
    [phone] varchar(20) NOT NULL,
    [province] nvarchar(100) NOT NULL,
    [recipient_name] nvarchar(100) NOT NULL,
    [street_address] nvarchar(255) NOT NULL,
    [ward] nvarchar(100) NOT NULL,
    [user_id] bigint NOT NULL,
    CONSTRAINT [PK__addresse__3213E83F39DE2102] PRIMARY KEY ([id])
);
GO

-- ===== BANG: carts =====
CREATE TABLE dbo.[carts] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [updated_at] datetime2(7) NOT NULL,
    [user_id] bigint NOT NULL,
    CONSTRAINT [PK__carts__3213E83F9D5D06A1] PRIMARY KEY ([id]),
    CONSTRAINT [UK64t7ox312pqal3p7fg9o503c2] UNIQUE ([user_id])
);
GO

-- ===== BANG: shops =====
CREATE TABLE dbo.[shops] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [active] bit NOT NULL,
    [address] nvarchar(255) NOT NULL,
    [banner_url] varchar(500) NULL,
    [created_at] datetime2(7) NOT NULL,
    [description] nvarchar(1000) NULL,
    [logo_url] varchar(500) NULL,
    [name] nvarchar(150) NOT NULL,
    [phone] varchar(20) NULL,
    [owner_id] bigint NOT NULL,
    [bio] nvarchar(500) NULL,
    CONSTRAINT [PK__shops__3213E83F2AED2D81] PRIMARY KEY ([id]),
    CONSTRAINT [UK6x3im56qg96va2stnwgkk7vtm] UNIQUE ([owner_id])
);
GO

-- ===== BANG: user_roles =====
CREATE TABLE dbo.[user_roles] (
    [user_id] bigint NOT NULL,
    [role_id] bigint NOT NULL,
    CONSTRAINT [PK__user_rol__6EDEA153C5FA3840] PRIMARY KEY ([user_id], [role_id])
);
GO

-- ===== BANG: app_commissions =====
CREATE TABLE dbo.[app_commissions] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [commission_rate] numeric(5,2) NOT NULL,
    [created_at] datetime2(7) NOT NULL,
    [note] nvarchar(255) NULL,
    [shop_id] bigint NOT NULL,
    CONSTRAINT [PK__app_comm__3213E83F0560C2DC] PRIMARY KEY ([id]),
    CONSTRAINT [UKox39jpjkvlkofirlru55ojy0f] UNIQUE ([shop_id])
);
GO

-- ===== BANG: books =====
CREATE TABLE dbo.[books] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [active] bit NOT NULL,
    [author] nvarchar(150) NOT NULL,
    [book_condition] varchar(30) NOT NULL,
    [condition_note] nvarchar(MAX) NULL,
    [created_at] datetime2(7) NOT NULL,
    [description] nvarchar(MAX) NULL,
    [isbn] varchar(50) NULL,
    [main_image_url] varchar(500) NULL,
    [original_price] numeric(12,2) NOT NULL,
    [price] numeric(12,2) NOT NULL,
    [publish_year] int NULL,
    [publisher] nvarchar(150) NULL,
    [sold_quantity] int NOT NULL,
    [stock] int NOT NULL,
    [title] nvarchar(255) NOT NULL,
    [updated_at] datetime2(7) NOT NULL,
    [category_id] bigint NOT NULL,
    [shop_id] bigint NOT NULL,
    [promotional_price] numeric(12,2) NULL,
    CONSTRAINT [PK__books__3213E83F173DC6AF] PRIMARY KEY ([id])
);
GO

-- ===== BANG: cart_items =====
CREATE TABLE dbo.[cart_items] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [quantity] int NOT NULL,
    [book_id] bigint NOT NULL,
    [cart_id] bigint NOT NULL,
    CONSTRAINT [PK__cart_ite__3213E83F6BC6F8D2] PRIMARY KEY ([id])
);
GO

-- ===== BANG: chat_messages =====
CREATE TABLE dbo.[chat_messages] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [content] nvarchar(2000) NOT NULL,
    [is_read] bit NOT NULL,
    [sent_at] datetime2(7) NOT NULL,
    [book_id] bigint NULL,
    [receiver_id] bigint NOT NULL,
    [sender_id] bigint NOT NULL,
    CONSTRAINT [PK__chat_mes__3213E83FF488D8A5] PRIMARY KEY ([id])
);
GO

-- ===== BANG: orders =====
CREATE TABLE dbo.[orders] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [created_at] datetime2(7) NOT NULL,
    [discount_amount] numeric(12,2) NOT NULL,
    [note] nvarchar(500) NULL,
    [order_code] varchar(50) NOT NULL,
    [payment_method] varchar(20) NOT NULL,
    [payment_status] varchar(20) NOT NULL,
    [recipient_name] nvarchar(100) NOT NULL,
    [recipient_phone] varchar(20) NOT NULL,
    [shipping_address] nvarchar(500) NOT NULL,
    [shipping_fee] numeric(12,2) NOT NULL,
    [status] varchar(30) NOT NULL,
    [subtotal] numeric(12,2) NOT NULL,
    [total_amount] numeric(12,2) NOT NULL,
    [updated_at] datetime2(7) NOT NULL,
    [user_id] bigint NOT NULL,
    [shipper_id] bigint NULL,
    [shop_id] bigint NOT NULL,
    CONSTRAINT [PK__orders__3213E83F1D8AE2AB] PRIMARY KEY ([id]),
    CONSTRAINT [UKdhk2umg8ijjkg4njg6891trit] UNIQUE ([order_code])
);
GO

-- ===== BANG: payments =====
CREATE TABLE dbo.[payments] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [amount] numeric(12,2) NOT NULL,
    [created_at] datetime2(7) NOT NULL,
    [payment_info] nvarchar(255) NULL,
    [payment_method] varchar(20) NOT NULL,
    [payment_status] varchar(20) NOT NULL,
    [payment_time] datetime2(7) NULL,
    [transaction_id] varchar(100) NULL,
    [order_id] bigint NOT NULL,
    CONSTRAINT [PK__payments__3213E83F47D957D2] PRIMARY KEY ([id])
);
GO

-- ===== BANG: reviews =====
CREATE TABLE dbo.[reviews] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [comment] nvarchar(2000) NULL,
    [created_at] datetime2(7) NOT NULL,
    [rating] int NOT NULL,
    [book_id] bigint NOT NULL,
    [order_id] bigint NOT NULL,
    [user_id] bigint NOT NULL,
    CONSTRAINT [PK__reviews__3213E83FF41E3056] PRIMARY KEY ([id])
);
GO

-- ===== BANG: viewed_products =====
CREATE TABLE dbo.[viewed_products] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [viewed_at] datetime2(7) NOT NULL,
    [book_id] bigint NOT NULL,
    [user_id] bigint NOT NULL,
    CONSTRAINT [PK__viewed_p__3213E83F37495B98] PRIMARY KEY ([id]),
    CONSTRAINT [UKlx0hwumangp3bbwo2t6gnvvsc] UNIQUE ([user_id], [book_id])
);
GO

-- ===== BANG: wishlists =====
CREATE TABLE dbo.[wishlists] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [created_at] datetime2(7) NOT NULL,
    [book_id] bigint NOT NULL,
    [user_id] bigint NOT NULL,
    CONSTRAINT [PK__wishlist__3213E83FDD0C66A6] PRIMARY KEY ([id]),
    CONSTRAINT [UKqantvi8dvhsq7r5j6qbbbym5r] UNIQUE ([user_id], [book_id])
);
GO

-- ===== BANG: book_images =====
CREATE TABLE dbo.[book_images] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [image_url] varchar(500) NOT NULL,
    [book_id] bigint NOT NULL,
    CONSTRAINT [PK__book_ima__3213E83FEAE646D1] PRIMARY KEY ([id])
);
GO

-- ===== BANG: order_items =====
CREATE TABLE dbo.[order_items] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [price] numeric(12,2) NOT NULL,
    [quantity] int NOT NULL,
    [book_id] bigint NOT NULL,
    [order_id] bigint NOT NULL,
    CONSTRAINT [PK__order_it__3213E83FADEF6D6F] PRIMARY KEY ([id])
);
GO

-- ===== BANG: order_status_histories =====
CREATE TABLE dbo.[order_status_histories] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [created_at] datetime2(7) NOT NULL,
    [note] nvarchar(500) NULL,
    [status] varchar(30) NOT NULL,
    [order_id] bigint NOT NULL,
    [updated_by_user_id] bigint NULL,
    CONSTRAINT [PK__order_st__3213E83FB32996BF] PRIMARY KEY ([id])
);
GO

-- ===== BANG: review_media =====
CREATE TABLE dbo.[review_media] (
    [id] bigint IDENTITY(1,1) NOT NULL,
    [media_type] varchar(20) NOT NULL,
    [media_url] varchar(500) NOT NULL,
    [review_id] bigint NOT NULL,
    CONSTRAINT [PK__review_m__3213E83FD6E9628D] PRIMARY KEY ([id])
);
GO

-- ===== DU LIEU: categories (7 dong) =====
SET IDENTITY_INSERT dbo.[categories] ON;
INSERT INTO dbo.[categories] ([id], [active], [description], [name], [slug]) VALUES (1, 1, N'Tiểu thuyết, truyện ngắn, thơ ca Việt Nam qua các thời kỳ hiện thực và đương đại', N'Văn Học Việt Nam', N'van-hoc-viet-nam');
INSERT INTO dbo.[categories] ([id], [active], [description], [name], [slug]) VALUES (5, 1, N'Manga, truyện tranh màu và các tác phẩm văn học thiếu nhi kinh điển', N'Truyện Tranh & Thiếu Nhi', N'truyen-tranh-thieu-nhi');
INSERT INTO dbo.[categories] ([id], [active], [description], [name], [slug]) VALUES (6, 1, N'Sách phát triển bản thân, nghệ thuật giao tiếp, thói quen và rèn luyện tư duy', N'Tâm Lý & Kỹ Năng Sống', N'tam-ly-ky-nang');
INSERT INTO dbo.[categories] ([id], [active], [description], [name], [slug]) VALUES (7, 1, N'Quản trị kinh doanh, bài học khởi nghiệp, đầu tư chứng khoán và quản lý tài chính cá nhân', N'Kinh Tế & Tài Chính - Đầu Tư', N'kinh-te-tai-chinh');
INSERT INTO dbo.[categories] ([id], [active], [description], [name], [slug]) VALUES (8, 1, N'Giáo trình đại học, cấu trúc dữ liệu, giải thuật, kiến trúc phần mềm, web và AI', N'Công Nghệ Thông Tin & Lập Trình', N'cong-nghe-thong-tin');
INSERT INTO dbo.[categories] ([id], [active], [description], [name], [slug]) VALUES (9, 1, N'Các kiệt tác văn học thế giới đạt giải Nobel, tiểu thuyết lãng mạn, trinh thám và giả tưởng', N'Văn Học Nước Ngoài & Kinh Điển', N'van-hoc-nuoc-ngoai');
INSERT INTO dbo.[categories] ([id], [active], [description], [name], [slug]) VALUES (10, 1, N'Nghiên cứu lịch sử Việt Nam và thế giới, vật lý thiên văn, vũ trụ và sinh học tiến hóa', N'Khoa Học - Vũ Trụ - Lịch Sử', N'khoa-hoc-lich-su');
SET IDENTITY_INSERT dbo.[categories] OFF;
GO

-- ===== DU LIEU: roles (5 dong) =====
SET IDENTITY_INSERT dbo.[roles] ON;
INSERT INTO dbo.[roles] ([id], [description], [name]) VALUES (1, N'Vai trò USER', N'USER');
INSERT INTO dbo.[roles] ([id], [description], [name]) VALUES (2, N'Vai trò VENDOR', N'VENDOR');
INSERT INTO dbo.[roles] ([id], [description], [name]) VALUES (3, N'Vai trò SHIPPER', N'SHIPPER');
INSERT INTO dbo.[roles] ([id], [description], [name]) VALUES (4, N'Vai trò MANAGER', N'MANAGER');
INSERT INTO dbo.[roles] ([id], [description], [name]) VALUES (5, N'Vai trò ADMIN', N'ADMIN');
SET IDENTITY_INSERT dbo.[roles] OFF;
GO

-- ===== DU LIEU: shipping_carriers (3 dong) =====
SET IDENTITY_INSERT dbo.[shipping_carriers] ON;
INSERT INTO dbo.[shipping_carriers] ([id], [active], [description], [name], [shipping_fee]) VALUES (1, 1, N'Giao hàng tiêu chuẩn 2-3 ngày toàn quốc', N'Giao Hàng Nhanh (GHN)', 25000.00);
INSERT INTO dbo.[shipping_carriers] ([id], [active], [description], [name], [shipping_fee]) VALUES (2, 1, N'Mạng lưới bưu cục rộng khắp cả nước', N'Viettel Post', 30000.00);
INSERT INTO dbo.[shipping_carriers] ([id], [active], [description], [name], [shipping_fee]) VALUES (3, 1, N'Chỉ áp dụng trong khu vực nội thành có chi nhánh shop', N'Hỏa Tốc Nội Thành (Giao trong 2h)', 45000.00);
SET IDENTITY_INSERT dbo.[shipping_carriers] OFF;
GO

-- ===== DU LIEU: users (5 dong) =====
SET IDENTITY_INSERT dbo.[users] ON;
INSERT INTO dbo.[users] ([id], [avatar_url], [created_at], [email], [enabled], [full_name], [locked], [password], [phone], [updated_at]) VALUES (1, NULL, '2026-10-23 15:18:54.2361495', N'admin@gmail.com', 1, N'Quản Trị Viên (Admin)', 0, N'$2a$10$e3EgoC5eQkrcv5CY91.t3eyMEFUsCTK047CLU7v1RPXWy8cw/fL/q', N'0900000001', '2026-10-23 15:18:54.2361495');
INSERT INTO dbo.[users] ([id], [avatar_url], [created_at], [email], [enabled], [full_name], [locked], [password], [phone], [updated_at]) VALUES (2, NULL, '2026-10-23 15:18:54.2529222', N'manager@gmail.com', 1, N'Quản Lý Chuỗi Cửa Hàng (Manager)', 0, N'$2a$10$e3EgoC5eQkrcv5CY91.t3eyMEFUsCTK047CLU7v1RPXWy8cw/fL/q', N'0900000002', '2026-10-23 15:18:54.2529222');
INSERT INTO dbo.[users] ([id], [avatar_url], [created_at], [email], [enabled], [full_name], [locked], [password], [phone], [updated_at]) VALUES (3, NULL, '2026-10-23 15:18:54.2582755', N'shipper@gmail.com', 1, N'Nhân Viên Giao Hàng (Shipper)', 0, N'$2a$10$e3EgoC5eQkrcv5CY91.t3eyMEFUsCTK047CLU7v1RPXWy8cw/fL/q', N'0900000003', '2026-10-23 15:18:54.2587986');
INSERT INTO dbo.[users] ([id], [avatar_url], [created_at], [email], [enabled], [full_name], [locked], [password], [phone], [updated_at]) VALUES (4, NULL, '2026-10-23 15:18:54.2632213', N'vendor@gmail.com', 1, N'Chủ Chi Nhánh Cửa Hàng (Vendor)', 0, N'$2a$10$e3EgoC5eQkrcv5CY91.t3eyMEFUsCTK047CLU7v1RPXWy8cw/fL/q', N'0900000004', '2026-10-23 15:18:54.2632213');
INSERT INTO dbo.[users] ([id], [avatar_url], [created_at], [email], [enabled], [full_name], [locked], [password], [phone], [updated_at]) VALUES (5, NULL, '2026-10-23 15:18:54.2694247', N'user@gmail.com', 1, N'Khách Mua Sách (User)', 0, N'$2a$10$e3EgoC5eQkrcv5CY91.t3eyMEFUsCTK047CLU7v1RPXWy8cw/fL/q', N'0900000005', '2026-10-23 15:18:54.2694247');
SET IDENTITY_INSERT dbo.[users] OFF;
GO

-- ===== DU LIEU: vouchers (3 dong) =====
SET IDENTITY_INSERT dbo.[vouchers] ON;
INSERT INTO dbo.[vouchers] ([id], [active], [code], [discount_type], [discount_value], [end_date], [max_discount_amount], [min_order_amount], [start_date], [title], [usage_limit], [used_count]) VALUES (1, 1, N'SACHCU10', N'PERCENTAGE', 10.00, '2027-01-25 17:32:57.7014063', 30000.00, 100000.00, '2026-10-24 17:32:57.7014063', N'Giảm 10% cho đơn từ 100.000 VNĐ', 100, 0);
INSERT INTO dbo.[vouchers] ([id], [active], [code], [discount_type], [discount_value], [end_date], [max_discount_amount], [min_order_amount], [start_date], [title], [usage_limit], [used_count]) VALUES (2, 1, N'FREESHIP', N'FREE_SHIPPING', 30000.00, '2027-01-25 17:32:57.7103053', 30000.00, 150000.00, '2026-10-24 17:32:57.7103053', N'Miễn phí vận chuyển (Tối đa 30.000 VNĐ)', 50, 0);
INSERT INTO dbo.[vouchers] ([id], [active], [code], [discount_type], [discount_value], [end_date], [max_discount_amount], [min_order_amount], [start_date], [title], [usage_limit], [used_count]) VALUES (3, 1, N'CHAOBAN', N'FIXED_AMOUNT', 20000.00, '2027-01-25 17:32:57.7153497', 20000.00, 50000.00, '2026-10-24 17:32:57.7153497', N'Giảm ngay 20.000 VNĐ cho khách hàng mới', 200, 0);
SET IDENTITY_INSERT dbo.[vouchers] OFF;
GO

-- ===== DU LIEU: carts (1 dong) =====
SET IDENTITY_INSERT dbo.[carts] ON;
INSERT INTO dbo.[carts] ([id], [updated_at], [user_id]) VALUES (1, '2026-10-26 22:13:42.5789413', 5);
SET IDENTITY_INSERT dbo.[carts] OFF;
GO

-- ===== DU LIEU: shops (1 dong) =====
SET IDENTITY_INSERT dbo.[shops] ON;
INSERT INTO dbo.[shops] ([id], [active], [address], [banner_url], [created_at], [description], [logo_url], [name], [phone], [owner_id], [bio]) VALUES (1, 1, N'Số 1 Võ Văn Ngân, TP. Thủ Đức, TP. Hồ Chí Minh', NULL, '2026-10-23 15:18:54.3037843', N'Chuyên thu mua và bán lại sách cũ, giáo trình sinh viên giá tốt', NULL, N'Hiệu Sách Cũ Thủ Đức - Chi Nhánh 1', N'0900000004', 4, NULL);
SET IDENTITY_INSERT dbo.[shops] OFF;
GO

-- ===== DU LIEU: user_roles (6 dong) =====
INSERT INTO dbo.[user_roles] ([user_id], [role_id]) VALUES (1, 5);
INSERT INTO dbo.[user_roles] ([user_id], [role_id]) VALUES (2, 4);
INSERT INTO dbo.[user_roles] ([user_id], [role_id]) VALUES (3, 3);
INSERT INTO dbo.[user_roles] ([user_id], [role_id]) VALUES (4, 1);
INSERT INTO dbo.[user_roles] ([user_id], [role_id]) VALUES (4, 2);
INSERT INTO dbo.[user_roles] ([user_id], [role_id]) VALUES (5, 1);
GO

-- ===== DU LIEU: app_commissions (1 dong) =====
SET IDENTITY_INSERT dbo.[app_commissions] ON;
INSERT INTO dbo.[app_commissions] ([id], [commission_rate], [created_at], [note], [shop_id]) VALUES (1, 5.00, '2026-10-25 17:32:57.6888052', N'Mức chiết khấu ưu đãi cho chi nhánh sáng lập chuỗi', 1);
SET IDENTITY_INSERT dbo.[app_commissions] OFF;
GO

-- ===== DU LIEU: books (93 dong) =====
SET IDENTITY_INSERT dbo.[books] ON;
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (1, 1, N'Vũ Trọng Phụng', N'OLD', N'Sách bìa mềm ố vàng theo thời gian, đủ trang, chữ rõ nét', '2026-10-23 15:18:54.3236469', NULL, N'ISBN9780001', N'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0e/Sodobanindau.jpg/330px-Sodobanindau.jpg', 120000.00, 65000.00, 1989, N'NXB Văn Học', 27, 5, N'Số Đỏ (Bản in 1989)', '2026-10-26 21:02:07.9109956', 1, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (2, 1, N'Nhiều Tác Giả (ĐHQG)', N'VERY_GOOD', N'Giáo trình chuẩn cho sinh viên CNTT, có ghi chú nhẹ bằng bút chì vài trang', '2026-10-23 15:18:54.3327360', NULL, N'ISBN9780002', N'https://salt.tikicdn.com/cache/750x750/ts/product/63/ca/0a/6b17851aa8faa7c703ab13be2ca6fa86.png', 95000.00, 45000.00, 2021, N'NXB ĐHQG', 60, 15, N'Giáo trình Cấu Trúc Dữ Liệu & Giải Thuật', '2026-10-26 21:02:07.8665275', 8, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (3, 1, N'Trần Trọng Kim', N'LIKE_NEW', N'Bộ sách lịch sử Việt Nam bằng chữ quốc ngữ đầu tiên, bìa cứng mới 98%', '2026-10-23 15:18:54.3380297', NULL, N'ISBN9780003', N'https://salt.tikicdn.com/cache/750x750/ts/product/96/b6/f9/bec5a94d2ce1113125dcd85714be6b61.jpg', 180000.00, 110000.00, 2005, N'NXB Văn Hóa Thông Tin', 21, 5, N'Việt Nam Sử Lược', '2026-10-26 21:02:08.0560177', 10, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (4, 1, N'Tô Hoài', N'ACCEPTABLE', N'Bìa có sờn mép nhẹ, ruột sách lành lặn, màu giấy cổ kính', '2026-10-23 15:18:54.3436080', NULL, N'ISBN9780004', N'https://salt.tikicdn.com/cache/750x750/ts/product/70/4a/98/90a8e9f3276ab4b84165e9fc12d9a0a1.jpg', 70000.00, 35000.00, 1995, N'NXB Kim Đồng', 18, 4, N'Dế Mèn Phiêu Lưu Ký (Ấn bản xưa)', '2026-10-26 21:02:07.6827171', 5, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (5, 1, N'Dan Senor & Saul Singer', N'GOOD', N'Câu chuyện về nền kinh tế thần kỳ của Israel, sách còn sáng rõ', '2026-10-25 17:32:57.6456790', NULL, N'ISBN9780005', N'https://salt.tikicdn.com/cache/750x750/ts/product/04/c0/b6/72ffbd7fed4d3fa7ed0255d18d3c5c32.png', 150000.00, 75000.00, 2018, N'NXB Thế Giới', 32, 9, N'Quốc Gia Khởi Nghiệp (Start-up Nation)', '2026-10-26 21:02:07.8174130', 7, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (6, 1, N'Robert C. Martin (Uncle Bob)', N'LIKE_NEW', N'Bản cẩm nang bất hủ cho mọi lập trình viên muốn viết code sạch, dễ bảo trì', '2026-10-26 14:23:36.7422701', NULL, N'ISBN9780006', N'https://salt.tikicdn.com/cache/750x750/media/catalog/product/product/6a/91/52/5800fbc7d5903968a0ebb111ea28ff4e.jpg', 260000.00, 145000.00, 2020, N'NXB Bách Khoa', 54, 12, N'Clean Code - Mã Nguồn Sạch', '2026-10-26 21:02:07.8544239', 8, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (7, 1, N'Ngô Sĩ Liên', N'VERY_GOOD', N'Ấn bản đóng bìa da mạ nhũ vàng cổ điển, giữ gìn kỹ', '2026-10-26 14:23:36.7984645', NULL, N'ISBN9780007', N'https://salt.tikicdn.com/cache/750x750/ts/product/ab/fa/cd/3220b4dac1f50035b534f7b5a907b62e.jpg', 350000.00, 195000.00, 2010, N'NXB Khoa Học Xã Hội', 15, 4, N'Đại Việt Sử Ký Toàn Thư (Trọn Bộ)', '2026-10-26 14:23:36.7984645', 10, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (8, 1, N'Nguyễn Văn Huyên', N'VERY_GOOD', N'Bản in chuyên khảo lịch sử giao thương hàng hải Đông Dương cổ, giấy dày đẹp', '2026-10-26 14:23:36.8039632', NULL, N'ISBN9780008', N'https://salt.tikicdn.com/cache/750x750/ts/product/79/10/ff/62028a69b559820b240e99c1663f398b.jpg', 190000.00, 115000.00, 2016, N'NXB Tri Thức', 19, 6, N'Hải Trình Thương Mại Đông Dương', '2026-10-26 21:02:07.8494251', 7, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (9, 1, N'Fujiko F. Fujio', N'LIKE_NEW', N'Bản in mới 98%, giấy sáng đẹp, không nhăn mép, đủ tranh màu', '2026-10-26 21:02:07.6413768', NULL, N'ISBN9786042171014', N'https://salt.tikicdn.com/cache/750x750/ts/product/76/f3/03/c0ba0a5b7d1b3d0df993efbdd068cb47.png', 25000.00, 15000.00, 2020, N'NXB Kim Đồng', 45, 12, N'Doraemon Tuyển Tập - Tập 1', '2026-10-26 21:02:07.6413768', 5, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (10, 1, N'Fujiko F. Fujio', N'VERY_GOOD', N'Sách giữ gìn cẩn thận, bìa bóng đẹp, không rách trang', '2026-10-26 21:02:07.6530066', NULL, N'ISBN9786042171021', N'https://salt.tikicdn.com/cache/750x750/ts/product/88/60/bc/a65ed3e888ed146f4f07ace96c666988.jpg', 25000.00, 15000.00, 2020, N'NXB Kim Đồng', 38, 8, N'Doraemon Tuyển Tập - Tập 2', '2026-10-26 21:02:07.6530066', 5, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (11, 1, N'Gosho Aoyama', N'LIKE_NEW', N'Ấn bản tái bản mới 99%, có bọc bìa kiếng nylon bảo vệ', '2026-10-26 21:02:07.6582287', NULL, N'ISBN9786042182010', N'https://salt.tikicdn.com/cache/750x750/ts/product/e3/47/b5/52a095864ab48b5168e69a258bb16e22.jpg', 25000.00, 16000.00, 2021, N'NXB Kim Đồng', 62, 15, N'Thám Tử Lừng Danh Conan - Tập 1', '2026-10-26 21:02:07.6582287', 5, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (12, 1, N'Gosho Aoyama', N'LIKE_NEW', N'Tập đặc biệt mốc 100 tập, còn nguyên postcard tặng kèm', '2026-10-26 21:02:07.6637811', NULL, N'ISBN9786042234009', N'https://salt.tikicdn.com/cache/750x750/ts/product/12/27/c1/8d4a5e0e661ae1e868c63fcedf96ceb7.jpg', 30000.00, 20000.00, 2022, N'NXB Kim Đồng', 40, 6, N'Thám Tử Lừng Danh Conan - Tập 100', '2026-10-26 21:02:07.6637811', 5, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (13, 1, N'Eiichiro Oda', N'VERY_GOOD', N'Tập mở đầu đại hải trình, truyện sạch sẽ, giấy ngả vàng nhẹ tự nhiên', '2026-10-26 21:02:07.6678539', NULL, N'ISBN9786042131018', N'https://salt.tikicdn.com/cache/750x750/ts/product/04/4e/03/7c388e502d6c0c7e3daaa95ff0050b22.jpg', 25000.00, 14000.00, 2019, N'NXB Kim Đồng', 50, 9, N'One Piece - Tập 1: Romance Dawn', '2026-10-26 21:02:07.6678539', 5, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (14, 1, N'Akira Toriyama', N'GOOD', N'Bìa hơi sờn mép nhẹ, ruột truyện nguyên vẹn không rách rời', '2026-10-26 21:02:07.6716438', NULL, N'ISBN9786042101011', N'https://salt.tikicdn.com/cache/750x750/ts/product/9d/3f/a2/a3b6b5baf3111b7817e8649f857cc25f.jpg', 22000.00, 12000.00, 2018, N'NXB Kim Đồng', 28, 7, N'Dragon Ball - 7 Viên Ngọc Rồng - Tập 1', '2026-10-26 21:02:07.6716438', 5, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (15, 1, N'Yoshito Usui', N'VERY_GOOD', N'Truyện tranh cười hài hước, tình trạng tốt 90%, đủ trang', '2026-10-26 21:02:07.6758567', NULL, N'ISBN9786042163019', N'https://salt.tikicdn.com/cache/750x750/ts/product/ee/3f/7e/cb3e809e337f89ab8877e18061d26f3f.jpg', 25000.00, 15000.00, 2020, N'NXB Kim Đồng', 31, 10, N'Shin - Cậu Bé Bút Chì - Tập 1', '2026-10-26 21:02:07.6758567', 5, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (16, 1, N'Tô Hoài - Tạ Huy Long', N'LIKE_NEW', N'Bản in bìa cứng cao cấp kèm tranh minh họa màu tuyệt đẹp của Tạ Huy Long', '2026-10-26 21:02:07.6908826', NULL, N'ISBN9786042125017', N'https://salt.tikicdn.com/cache/750x750/ts/product/45/40/22/69db1eb71db1dd38012908c82183af5c.jpg', 150000.00, 95000.00, 2019, N'NXB Kim Đồng', 24, 5, N'Dế Mèn Phiêu Lưu Ký (Bản Minh Họa Màu)', '2026-10-26 21:02:07.6908826', 5, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (17, 1, N'Nguyễn Nhật Ánh', N'VERY_GOOD', N'Bộ truyện học trò gắn liền tuổi thơ, sách còn rất mới 92%', '2026-10-26 21:02:07.6946102', NULL, N'ISBN9786041121010', N'https://covers.openlibrary.org/b/id/7014101-L.jpg', 55000.00, 32000.00, 2018, N'NXB Trẻ', 26, 8, N'Kính Vạn Hoa - Tập 1: Nhà Ảo Thuật', '2026-10-26 21:02:07.6946102', 5, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (18, 1, N'Nguyễn Nhật Ánh', N'LIKE_NEW', N'Bản bìa mềm, tình trạng như mới 96%, trang sách tinh tươm', '2026-10-26 21:02:07.6996246', NULL, N'ISBN9786041142015', N'https://salt.tikicdn.com/cache/750x750/ts/product/97/3a/8c/80eadabd4c3cc0c023a44677b439b328.jpg', 85000.00, 52000.00, 2019, N'NXB Trẻ', 48, 11, N'Cho Tôi Xin Một Vé Đi Tuổi Thơ', '2026-10-26 21:02:07.6996246', 5, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (19, 1, N'Nguyễn Nhật Ánh', N'VERY_GOOD', N'Tác phẩm văn học lãng mạn chuyển thể điện ảnh, giấy đẹp không quăn mép', '2026-10-26 21:02:07.7039107', NULL, N'ISBN9786041151017', N'https://salt.tikicdn.com/cache/750x750/ts/product/aa/41/13/35116c017b8294342e46f6fd002ccbea.png', 110000.00, 68000.00, 2019, N'NXB Trẻ', 55, 14, N'Mắt Biếc', '2026-10-26 21:02:07.7039107', 5, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (20, 1, N'Nguyễn Nhật Ánh', N'GOOD', N'Bản in năm 2018, tình trạng 85%, không ghi chú, gáy chắc chắn', '2026-10-26 21:02:07.7076071', NULL, N'ISBN9786041131019', N'https://salt.tikicdn.com/cache/750x750/ts/product/ac/92/49/3384968eac3888e307a84118b424c1d4.jpg', 95000.00, 55000.00, 2018, N'NXB Trẻ', 34, 7, N'Tôi Thấy Hoa Vàng Trên Cỏ Xanh', '2026-10-26 21:02:07.7076071', 5, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (21, 1, N'Nguyễn Nhật Ánh', N'LIKE_NEW', N'Bìa đẹp, sách giữ kỹ trong tủ kính, gần như chưa qua sử dụng', '2026-10-26 21:02:07.7126717', NULL, N'ISBN9786041112018', N'https://salt.tikicdn.com/cache/750x750/ts/product/92/b9/15/687fddc6ed00d35d3598661cd92a22d3.jpg', 80000.00, 48000.00, 2019, N'NXB Trẻ', 29, 6, N'Cô Gái Đến Từ Hôm Qua', '2026-10-26 21:02:07.7126717', 5, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (22, 1, N'Dale Carnegie', N'LIKE_NEW', N'Bản dịch kinh điển của First News, sách mới 98%, giấy không tì vết', '2026-10-26 21:02:07.7186419', NULL, N'ISBN9786045891012', N'https://cdn1.fahasa.com/media/catalog/product/8/9/8935088526277.jpg', 98000.00, 58000.00, 2021, N'NXB Tổng Hợp TP.HCM', 85, 20, N'Đắc Nhân Tâm (How to Win Friends and Influence People)', '2026-10-26 21:02:07.7186419', 6, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (23, 1, N'Dale Carnegie', N'VERY_GOOD', N'Sách còn nguyên vẹn 90%, có bút dạ quang highlight vài dòng trang đầu', '2026-10-26 21:02:07.7249369', NULL, N'ISBN9786045882010', N'https://salt.tikicdn.com/cache/750x750/ts/product/5a/c7/9d/b122b9102f5e2c5f726d6e1c0c58c4bb.png', 88000.00, 49000.00, 2020, N'NXB Tổng Hợp TP.HCM', 42, 10, N'Quẳng Gánh Lo Đi Và Vui Sống', '2026-10-26 21:02:07.7249369', 6, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (24, 1, N'Baird T. Spalding - Nguyên Phong dịch', N'LIKE_NEW', N'Tác phẩm minh triết phương Đông nổi tiếng, bìa đẹp như mới 97%', '2026-10-26 21:02:07.7318041', NULL, N'ISBN9786049811013', N'https://salt.tikicdn.com/cache/750x750/ts/product/c4/1e/d0/afbe47f888abc7c3dadbb012cec33b34.jpg', 118000.00, 69000.00, 2019, N'NXB Hồng Đức', 53, 12, N'Hành Trình Về Phương Đông', '2026-10-26 21:02:07.7318041', 6, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (25, 1, N'Daniel Kahneman', N'VERY_GOOD', N'Sách tâm lý học hành vi đạt giải Nobel Kinh tế, gáy dày chắc chắn', '2026-10-26 21:02:07.7388023', NULL, N'ISBN9786047741014', N'https://cdn1.fahasa.com/media/catalog/product/8/9/8935280911765.jpg', 219000.00, 125000.00, 2018, N'NXB Thế Giới', 36, 7, N'Tư Duy Nhanh Và Chậm (Thinking, Fast and Slow)', '2026-10-26 21:02:07.7388023', 6, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (26, 1, N'Stephen R. Covey', N'LIKE_NEW', N'Cẩm nang phát triển bản thân hàng đầu thế giới, sách nguyên vẹn 95%', '2026-10-26 21:02:07.7444771', NULL, N'ISBN9786041162013', N'https://upload.wikimedia.org/wikipedia/en/a/a2/The_7_Habits_of_Highly_Effective_People.jpg', 145000.00, 85000.00, 2020, N'NXB Trẻ', 44, 9, N'7 Thói Quen Của Người Thành Đạt', '2026-10-26 21:02:07.7444771', 6, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (27, 1, N'Richard Koch', N'GOOD', N'Bí quyết đạt nhiều kết quả hơn với ít nỗ lực hơn, tình trạng tốt 85%', '2026-10-26 21:02:07.7504287', NULL, N'ISBN9786041143012', N'https://covers.openlibrary.org/b/isbn/9780385491747-L.jpg', 115000.00, 62000.00, 2019, N'NXB Trẻ', 30, 8, N'Nguyên Lý 80/20 (The 80/20 Principle)', '2026-10-26 21:02:07.7504287', 6, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (28, 1, N'James Clear', N'LIKE_NEW', N'Bản in mới nhất, cuốn sách xây dựng thói quen bán chạy toàn cầu', '2026-10-26 21:02:07.7564337', NULL, N'ISBN9786047791019', N'https://cdn1.fahasa.com/media/catalog/product/9/7/9781847941831.jpg', 189000.00, 115000.00, 2021, N'NXB Thế Giới', 67, 15, N'Thói Quen Nguyên Tử (Atomic Habits)', '2026-10-26 21:02:07.7564337', 6, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (29, 1, N'Mark Manson', N'VERY_GOOD', N'Lối tư duy thực tế và hài hước, sách sạch đẹp không rách', '2026-10-26 21:02:07.7615251', NULL, N'ISBN9786049832018', N'https://upload.wikimedia.org/wikipedia/en/b/bd/The_Subtle_Art_of_Not_Giving_a_F%2Ack_by_Mark_Manson_-_Book_Cover.png', 125000.00, 69000.00, 2019, N'NXB Lao Động', 39, 10, N'Nghệ Thuật Tinh Tế Của Việc Đếch Quan Tâm', '2026-10-26 21:02:07.7615251', 6, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (30, 1, N'Vãn Tình', N'LIKE_NEW', N'Sách truyền cảm hứng sống tự lập và bản lĩnh của Vãn Tình', '2026-10-26 21:02:07.7664310', NULL, N'ISBN9786047781010', N'https://covers.openlibrary.org/b/id/10574211-L.jpg', 109000.00, 65000.00, 2020, N'NXB Thế Giới', 41, 11, N'Càng Kỷ Luật Càng Tự Do', '2026-10-26 21:02:07.7669316', 6, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (31, 1, N'Kishimi Ichiro & Koga Fumitake', N'LIKE_NEW', N'Tâm lý học Adler dưới góc nhìn đối thoại triết học, tình trạng 96%', '2026-10-26 21:02:07.7709348', NULL, N'ISBN9786049822019', N'https://covers.openlibrary.org/b/isbn/9781501197277-L.jpg', 119000.00, 72000.00, 2019, N'NXB Lao Động', 46, 9, N'Dám Bị Ghét', '2026-10-26 21:02:07.7709348', 6, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (32, 1, N'Chade-Meng Tan', N'GOOD', N'Chương trình đào tạo trí tuệ cảm xúc nổi tiếng tại Google', '2026-10-26 21:02:07.7750636', NULL, N'ISBN9786046531015', N'https://covers.openlibrary.org/b/isbn/9780062116932-L.jpg', 135000.00, 75000.00, 2018, N'NXB Lao Động Xã Hội', 22, 6, N'Search Inside Yourself', '2026-10-26 21:02:07.7750636', 6, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (33, 1, N'Eckhart Tolle', N'VERY_GOOD', N'Chỉ dẫn tâm linh vượt thoát căng thẳng và an trú trong hiện tại', '2026-10-26 21:02:07.7786596', NULL, N'ISBN9786045871014', N'https://upload.wikimedia.org/wikipedia/en/6/66/TPON_Cover_LG.jpg', 110000.00, 64000.00, 2019, N'NXB Tổng Hợp TP.HCM', 35, 8, N'Sức Mạnh Của Hiện Tại (The Power of Now)', '2026-10-26 21:02:07.7786596', 6, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (34, 1, N'Sasaki Fumio', N'VERY_GOOD', N'Cách giải phóng không gian sống và tâm trí để đón nhận hạnh phúc', '2026-10-26 21:02:07.7834138', NULL, N'ISBN9786049531011', N'https://covers.openlibrary.org/b/isbn/9780399581212-L.jpg', 95000.00, 52000.00, 2018, N'NXB Thái Hà', 27, 7, N'Lối Sống Tối Giản Của Người Nhật', '2026-10-26 21:02:07.7834138', 6, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (35, 1, N'Thiền sư Minh Niệm', N'LIKE_NEW', N'Tác phẩm chữa lành tâm hồn sâu sắc của Thiền sư Minh Niệm, bản in đẹp', '2026-10-26 21:02:07.7873282', NULL, N'ISBN9786041172012', N'https://covers.openlibrary.org/b/id/14237928-L.jpg', 145000.00, 89000.00, 2020, N'NXB Trẻ', 58, 13, N'Hiểu Về Trái Tim', '2026-10-26 21:02:07.7873282', 6, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (36, 1, N'Robert T. Kiyosaki', N'LIKE_NEW', N'Để không có tiền vẫn tạo ra tiền, bản in chuẩn của NXB Trẻ, sách 95%', '2026-10-26 21:02:07.7918264', NULL, N'ISBN9786041152014', N'https://cdn1.fahasa.com/media/catalog/product/8/9/8934974180968_3.jpg', 95000.00, 55000.00, 2019, N'NXB Trẻ', 72, 18, N'Dạy Con Làm Giàu - Tập 1 (Rich Dad Poor Dad)', '2026-10-26 21:02:07.7918264', 7, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (37, 1, N'Robert T. Kiyosaki', N'VERY_GOOD', N'Khám phá Kim tứ đồ tài chính, tình trạng sách rất tốt 90%', '2026-10-26 21:02:07.7958265', NULL, N'ISBN9786041152021', N'https://cdn1.fahasa.com/media/catalog/product/8/9/8934974180975.jpg', 110000.00, 65000.00, 2019, N'NXB Trẻ', 39, 10, N'Dạy Con Làm Giàu - Tập 2: Sử Dụng Đồng Vốn', '2026-10-26 21:02:07.7958265', 7, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (38, 1, N'Morgan Housel', N'LIKE_NEW', N'Những bài học vượt thời gian về sự giàu có, lòng tham và hạnh phúc', '2026-10-26 21:02:07.8008277', NULL, N'ISBN9786043041018', N'https://cdn1.fahasa.com/media/catalog/product/8/9/8936066694537.jpg', 179000.00, 110000.00, 2021, N'NXB Dân Trí', 61, 14, N'Tâm Lý Học Về Tiền (The Psychology of Money)', '2026-10-26 21:02:07.8008277', 7, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (39, 1, N'Benjamin Graham', N'VERY_GOOD', N'Kinh thánh đầu tư giá trị của người thầy của Warren Buffett, bản dày bìa cứng', '2026-10-26 21:02:07.8048508', NULL, N'ISBN9786046541014', N'https://cdn1.fahasa.com/media/catalog/product/n/h/nha-dau-thu-thong-minh-tb.jpg', 250000.00, 145000.00, 2018, N'NXB Lao Động Xã Hội', 34, 8, N'Nhà Đầu Tư Thông Minh (The Intelligent Investor)', '2026-10-26 21:02:07.8058504', 7, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (40, 1, N'Peter Lynch', N'GOOD', N'Kinh nghiệm đầu tư cổ phiếu thực chiến từ nhà quản lý quỹ huyền thoại', '2026-10-26 21:02:07.8088986', NULL, N'ISBN9786046551012', N'https://covers.openlibrary.org/b/isbn/9780743200400-L.jpg', 160000.00, 89000.00, 2019, N'NXB Lao Động Xã Hội', 26, 7, N'Trên Đỉnh Phố Wall (One Up On Wall Street)', '2026-10-26 21:02:07.8088986', 7, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (41, 1, N'Jim Collins', N'VERY_GOOD', N'Nghiên cứu kinh điển về các công ty có bước nhảy vọt vĩ đại', '2026-10-26 21:02:07.8210156', NULL, N'ISBN9786041144019', N'https://upload.wikimedia.org/wikipedia/en/0/03/Cover_Good_2_Gr8.jpg', 175000.00, 98000.00, 2019, N'NXB Trẻ', 37, 8, N'Từ Tốt Đến Vĩ Đại (Good to Great)', '2026-10-26 21:02:07.8210156', 7, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (42, 1, N'W. Chan Kim & Renée Mauborgne', N'LIKE_NEW', N'Tạo khoảng trống thị trường không có cạnh tranh, sách đẹp 95%', '2026-10-26 21:02:07.8249047', NULL, N'ISBN9786049751012', N'https://upload.wikimedia.org/wikipedia/en/a/a3/BOStrategy.jpg', 180000.00, 105000.00, 2018, N'NXB Lao Động', 28, 6, N'Chiến Lược Đại Dương Xanh (Blue Ocean Strategy)', '2026-10-26 21:02:07.8249047', 7, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (43, 1, N'Steven D. Levitt & Stephen J. Dubner', N'VERY_GOOD', N'Góc nhìn dí dỏm giải mã các hiện tượng xã hội qua lăng kính kinh tế học', '2026-10-26 21:02:07.8282374', NULL, N'ISBN9786047761012', N'https://covers.openlibrary.org/b/isbn/9780060731328-L.jpg', 129000.00, 75000.00, 2019, N'NXB Thế Giới', 30, 8, N'Kinh Tế Học Hài Hước (Freakonomics)', '2026-10-26 21:02:07.8282374', 7, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (44, 1, N'Philip A. Fisher', N'VERY_GOOD', N'Phương pháp chọn cổ phiếu tăng trưởng nổi tiếng thế giới', '2026-10-26 21:02:07.8326396', NULL, N'ISBN9786046561011', N'https://covers.openlibrary.org/b/isbn/9780471445500-L.jpg', 135000.00, 78000.00, 2019, N'NXB Lao Động Xã Hội', 25, 7, N'Cổ Phiếu Thường, Lợi Nhuận Phi Thường', '2026-10-26 21:02:07.8326396', 7, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (45, 1, N'Dan Ariely', N'GOOD', N'Khám phá những lực vô hình chi phối các quyết định mua sắm của con người', '2026-10-26 21:02:07.8364558', NULL, N'ISBN9786046571010', N'https://covers.openlibrary.org/b/isbn/9780061353239-L.jpg', 140000.00, 79000.00, 2018, N'NXB Lao Động Xã Hội', 21, 6, N'Phi Lý Trí (Predictably Irrational)', '2026-10-26 21:02:07.8364558', 7, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (46, 1, N'Malcolm Gladwell', N'LIKE_NEW', N'Bí mật đằng sau thành công của những vĩ nhân và quy tắc 10.000 giờ', '2026-10-26 21:02:07.8403453', NULL, N'ISBN9786047753017', N'https://upload.wikimedia.org/wikipedia/en/e/ec/Outliers_%28book_cover%29.png', 130000.00, 76000.00, 2018, N'NXB Thế Giới', 43, 10, N'Những Kẻ Xuất Chúng (Outliers)', '2026-10-26 21:02:07.8403453', 7, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (47, 1, N'Malcolm Gladwell', N'VERY_GOOD', N'Làm thế nào những điều nhỏ bé có thể tạo nên sự khác biệt lớn', '2026-10-26 21:02:07.8442364', NULL, N'ISBN9786047754014', N'https://upload.wikimedia.org/wikipedia/en/7/73/Thetippingpoint.jpg', 125000.00, 72000.00, 2018, N'NXB Thế Giới', 31, 8, N'Điểm Bùng Phát (The Tipping Point)', '2026-10-26 21:02:07.8442364', 7, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (48, 1, N'Robert C. Martin', N'LIKE_NEW', N'Hướng dẫn thiết kế cấu trúc phần mềm chuyên nghiệp, sách mới 98%', '2026-10-26 21:02:07.8564237', NULL, N'ISBN9786049921015', N'https://covers.openlibrary.org/b/isbn/9780134494166-L.jpg', 280000.00, 165000.00, 2021, N'NXB Bách Khoa', 38, 9, N'Clean Architecture - Kiến Trúc Sạch', '2026-10-26 21:02:07.8564237', 8, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (49, 1, N'Andrew Hunt & David Thomas', N'LIKE_NEW', N'Kỷ niệm 20 năm xuất bản, cẩm nang từ thợ code đến bậc thầy phần mềm', '2026-10-26 21:02:07.8604646', NULL, N'ISBN9786048031012', N'https://covers.openlibrary.org/b/isbn/9780201616224-L.jpg', 275000.00, 155000.00, 2020, N'NXB Thông Tin & Truyền Thông', 33, 8, N'The Pragmatic Programmer', '2026-10-26 21:02:07.8604646', 8, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (50, 1, N'Gang of Four (GoF)', N'VERY_GOOD', N'23 mẫu thiết kế kinh điển giúp tái sử dụng phần mềm hướng đối tượng hiệu quả', '2026-10-26 21:02:07.8694335', NULL, N'ISBN9786047361013', N'https://covers.openlibrary.org/b/isbn/9780201633610-L.jpg', 290000.00, 160000.00, 2019, N'NXB ĐHQG', 29, 7, N'Design Patterns - Mẫu Thiết Kế Hướng Đối Tượng', '2026-10-26 21:02:07.8694335', 8, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (51, 1, N'Martin Fowler', N'LIKE_NEW', N'Bản in mới, chỉ dẫn từng bước cải tiến thiết kế mã nguồn mà không làm đổi hành vi', '2026-10-26 21:02:07.8733651', NULL, N'ISBN9786048041011', N'https://covers.openlibrary.org/b/isbn/9780134757599-L.jpg', 310000.00, 185000.00, 2021, N'NXB Thông Tin & Truyền Thông', 24, 6, N'Refactoring - Tối Ưu Hóa Mã Nguồn Hiện Có', '2026-10-26 21:02:07.8733651', 8, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (52, 1, N'Eric Evans', N'VERY_GOOD', N'Xử lý độ phức tạp ở tầng lõi phần mềm của Eric Evans, sách còn mới 92%', '2026-10-26 21:02:07.8766310', NULL, N'ISBN9786047371012', N'https://covers.openlibrary.org/b/isbn/9780321125217-L.jpg', 320000.00, 190000.00, 2020, N'NXB ĐHQG', 20, 5, N'Domain-Driven Design (Thiết Kế Hướng Miền)', '2026-10-26 21:02:07.8766310', 8, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (53, 1, N'Aditya Bhargava', N'LIKE_NEW', N'Cuốn sách thuật toán trực quan, dễ hiểu nhất kèm hình minh họa dí dỏm', '2026-10-26 21:02:07.8803414', NULL, N'ISBN9786043121017', N'https://covers.openlibrary.org/b/isbn/9781617292231-L.jpg', 165000.00, 99000.00, 2020, N'NXB Dân Trí', 52, 14, N'Grokking Algorithms - Minh Họa Thuật Toán', '2026-10-26 21:02:07.8803414', 8, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (54, 1, N'Khoa CNTT - ĐHQG TP.HCM', N'LIKE_NEW', N'Giáo trình thực hành xây dựng ứng dụng Web Enterprise với Spring Boot & Hibernate', '2026-10-26 21:02:07.8852145', NULL, N'ISBN9786047381011', N'https://covers.openlibrary.org/b/id/8231996-L.jpg', 140000.00, 85000.00, 2022, N'NXB ĐHQG TP.HCM', 47, 16, N'Lập Trình Web Hiện Đại Với Spring Boot & JPA', '2026-10-26 21:02:07.8852145', 8, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (55, 1, N'Martin Kleppmann', N'LIKE_NEW', N'Cuốn cẩm nang toàn diện về hệ thống phân tán, cơ sở dữ liệu lớn và độ tin cậy', '2026-10-26 21:02:07.8894358', NULL, N'ISBN9786049941013', N'https://covers.openlibrary.org/b/isbn/9781449373320-L.jpg', 380000.00, 225000.00, 2021, N'NXB Bách Khoa', 35, 8, N'Designing Data-Intensive Applications', '2026-10-26 21:02:07.8894358', 8, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (56, 1, N'Thomas H. Cormen et al.', N'VERY_GOOD', N'Bộ sách thuật toán toàn diện bậc nhất được giảng dạy tại MIT và các trường ĐH', '2026-10-26 21:02:07.8929078', NULL, N'ISBN9786049951012', N'https://upload.wikimedia.org/wikipedia/en/f/ff/Clrs4.jpeg', 420000.00, 240000.00, 2019, N'NXB Bách Khoa', 18, 5, N'Introduction to Algorithms (CLRS)', '2026-10-26 21:02:07.8929078', 8, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (57, 1, N'Nhiều Tác Giả', N'LIKE_NEW', N'Hướng dẫn tự học Python từ cơ bản đến xử lý dữ liệu và tự động hóa', '2026-10-26 21:02:07.8960447', NULL, N'ISBN9786046711019', N'https://covers.openlibrary.org/b/id/9255566-L.jpg', 135000.00, 78000.00, 2021, N'NXB Khoa Học & Kỹ Thuật', 41, 11, N'Lập Trình Python Căn Bản Và Nâng Cao', '2026-10-26 21:02:07.8960447', 8, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (58, 1, N'Nhiều Tác Giả', N'VERY_GOOD', N'Nắm vững câu lệnh SQL, Indexing, Transaction và tối ưu hóa truy vấn CSDL', '2026-10-26 21:02:07.9009267', NULL, N'ISBN9786048051010', N'https://covers.openlibrary.org/b/id/8091515-L.jpg', 120000.00, 68000.00, 2020, N'NXB Bưu Điện', 36, 10, N'Cơ Sở Dữ Liệu Quan Hệ & SQL Thực Chiến', '2026-10-26 21:02:07.9009267', 8, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (59, 1, N'Viện CNTT ĐHQG', N'LIKE_NEW', N'Tổng quan về Machine Learning, Deep Learning và mạng nơ-ron nhân tạo', '2026-10-26 21:02:07.9049471', NULL, N'ISBN9786047391010', N'https://covers.openlibrary.org/b/isbn/9780136042594-L.jpg', 195000.00, 119000.00, 2022, N'NXB ĐHQG', 30, 9, N'Trí Tuệ Nhân Tạo Trong Kỷ Nguyên Số', '2026-10-26 21:02:07.9049471', 8, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (60, 1, N'Nam Cao', N'VERY_GOOD', N'Tác phẩm hiện thực phê phán xuất sắc của Nam Cao, sách nguyên vẹn 90%', '2026-10-26 21:02:07.9142274', NULL, N'ISBN9786049611013', N'https://cdn1.fahasa.com/media/catalog/product/8/9/8935236430128.jpg', 65000.00, 38000.00, 2018, N'NXB Văn Học', 44, 12, N'Chí Phèo', '2026-10-26 21:02:07.9142274', 1, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (61, 1, N'Ngô Tất Tố', N'LIKE_NEW', N'Hình tượng chị Dậu bất hủ trong nền văn học Việt Nam, tình trạng như mới 95%', '2026-10-26 21:02:07.9183738', NULL, N'ISBN9786049621012', N'https://upload.wikimedia.org/wikipedia/vi/b/b1/T%E1%BA%AFt_%C4%91%C3%A8n-Nh%C3%A3_Nam.jpeg', 70000.00, 42000.00, 2019, N'NXB Văn Học', 39, 10, N'Tắt Đèn', '2026-10-26 21:02:07.9183738', 1, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (62, 1, N'Nam Cao', N'VERY_GOOD', N'Tuyển chọn các truyện ngắn nhân đạo lay động lòng người của Nam Cao', '2026-10-26 21:02:07.9222491', NULL, N'ISBN9786042141017', N'https://cdn1.fahasa.com/media/catalog/product/8/9/8935236430111.jpg', 60000.00, 35000.00, 2019, N'NXB Kim Đồng', 31, 9, N'Lão Hạc (Tuyển Tập Truyện Ngắn)', '2026-10-26 21:02:07.9222491', 1, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (63, 1, N'Đại thi hào Nguyễn Du', N'LIKE_NEW', N'Ấn bản bìa cứng có phụ bản tranh khắc gỗ truyền thống, rất giá trị', '2026-10-26 21:02:07.9250843', NULL, N'ISBN9786049891015', N'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b7/%C4%90o%E1%BA%A1n_tr%C6%B0%E1%BB%9Dng_t%C3%A2n_thanh.png/330px-%C4%90o%E1%BA%A1n_tr%C6%B0%E1%BB%9Dng_t%C3%A2n_thanh.png', 180000.00, 110000.00, 2020, N'NXB Hội Nhà Văn', 28, 7, N'Truyện Kiều (Bản Khảo Đính Đầy Đủ)', '2026-10-26 21:02:07.9250843', 1, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (64, 1, N'Bảo Ninh', N'LIKE_NEW', N'Tiểu thuyết chiến tranh Việt Nam vang danh thế giới, sách còn rất mới', '2026-10-26 21:02:07.9299621', NULL, N'ISBN9786049871017', N'https://thumb.wikimedia.org/wikipedia/vi/thumb/1/11/Noi_buon_chien_tranh.jpg/330px-Noi_buon_chien_tranh.jpg', 125000.00, 75000.00, 2019, N'NXB Hội Nhà Văn', 33, 8, N'Nỗi Buồn Chiến Tranh', '2026-10-26 21:02:07.9299621', 1, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (65, 1, N'Lê Lựu', N'GOOD', N'Tác phẩm cột mốc của văn học đổi mới về thân phận con người thời hậu chiến', '2026-10-26 21:02:07.9339636', NULL, N'ISBN9786049861018', N'https://covers.openlibrary.org/b/id/7946911-L.jpg', 95000.00, 52000.00, 2018, N'NXB Hội Nhà Văn', 22, 6, N'Thời Xa Vắng', '2026-10-26 21:02:07.9339636', 1, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (66, 1, N'Đoàn Giỏi', N'LIKE_NEW', N'Thiên nhiên hoang sơ trù phú và con người hào sảng phương Nam, sách mới 97%', '2026-10-26 21:02:07.9380094', NULL, N'ISBN9786042151016', N'https://cdn1.fahasa.com/media/catalog/product/d/a/dat-rung-phuong-nam.jpg', 85000.00, 50000.00, 2020, N'NXB Kim Đồng', 46, 11, N'Đất Rừng Phương Nam', '2026-10-26 21:02:07.9380094', 1, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (67, 1, N'Thạch Lam', N'VERY_GOOD', N'Những trang văn nhẹ nhàng, tinh tế và giàu tình trắc ẩn của Thạch Lam', '2026-10-26 21:02:07.9419616', NULL, N'ISBN9786049631011', N'https://cdn1.fahasa.com/media/catalog/product/8/9/8935236430159.jpg', 65000.00, 38000.00, 2019, N'NXB Văn Học', 30, 10, N'Gió Đầu Mùa (Tuyển Tập)', '2026-10-26 21:02:07.9419616', 1, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (68, 1, N'Kim Lân', N'LIKE_NEW', N'Các kiệt tác Làng, Vợ nhặt, Con chó xấu xí... bản in bìa đẹp', '2026-10-26 21:02:07.9449611', NULL, N'ISBN9786042161015', N'https://cdn1.fahasa.com/media/catalog/product/8/9/8935236430142.jpg', 70000.00, 42000.00, 2020, N'NXB Kim Đồng', 35, 8, N'Vợ Nhặt (Tuyển Tập Kim Lân)', '2026-10-26 21:02:07.9449611', 1, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (69, 1, N'Thạch Lam', N'LIKE_NEW', N'Bản tùy bút bất hủ về nét đẹp ẩm thực và phong vị kinh kỳ Thăng Long xưa', '2026-10-26 21:02:07.9499612', NULL, N'ISBN9786049881016', N'https://cdn1.fahasa.com/media/catalog/product/8/9/8935236413282.jpg', 75000.00, 45000.00, 2019, N'NXB Hội Nhà Văn', 51, 13, N'Hà Nội Băm Sáu Phố Phường', '2026-10-26 21:02:07.9510623', 1, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (70, 1, N'Nguyễn Khải', N'GOOD', N'Truyện ngắn tái hiện công cuộc tái thiết cuộc sống mới ở nông trường Điện Biên', '2026-10-26 21:02:07.9550366', NULL, N'ISBN9786049641010', N'https://covers.openlibrary.org/b/id/8315266-L.jpg', 55000.00, 30000.00, 2018, N'NXB Văn Học', 19, 7, N'Mùa Lạc', '2026-10-26 21:02:07.9550366', 1, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (71, 1, N'Paulo Coelho', N'LIKE_NEW', N'Cuốn sách bán chạy chỉ sau Kinh Thánh, bài học theo đuổi vận mệnh đời mình', '2026-10-26 21:02:07.9590564', NULL, N'ISBN9786049901017', N'https://cdn1.fahasa.com/media/catalog/product/n/h/nha-gia-kim-b.jpg', 89000.00, 55000.00, 2020, N'NXB Hội Nhà Văn & Nhã Nam', 95, 22, N'Nhà Giả Kim (The Alchemist)', '2026-10-26 21:02:07.9590564', 9, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (72, 1, N'Antoine de Saint-Exupéry', N'LIKE_NEW', N'Ấn bản minh họa màu gốc tuyệt đẹp của chính tác giả, sách như mới 98%', '2026-10-26 21:02:07.9629998', NULL, N'ISBN9786042172011', N'https://cdn1.fahasa.com/media/catalog/product/8/9/8935236439565.jpg', 85000.00, 52000.00, 2020, N'NXB Kim Đồng', 80, 18, N'Hoàng Tử Bé (Le Petit Prince)', '2026-10-26 22:23:24.4612287', 9, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (73, 1, N'Ernest Hemingway', N'VERY_GOOD', N'Kiệt tác đoạt giải Nobel Văn học, biểu tượng cho ý chí kiên cường của con người', '2026-10-26 21:02:07.9675074', NULL, N'ISBN9786049651019', N'https://thumb.wikimedia.org/wikipedia/vi/thumb/7/73/Oldmansea.jpg/330px-Oldmansea.jpg', 75000.00, 45000.00, 2019, N'NXB Văn Học', 46, 11, N'Ông Già Và Biển Cả', '2026-10-26 21:02:07.9675074', 9, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (74, 1, N'Haruki Murakami', N'LIKE_NEW', N'Tác phẩm nổi tiếng nhất của Murakami, bản dịch của Trịnh Lữ, sách 95%', '2026-10-26 21:02:07.9721706', NULL, N'ISBN9786049911016', N'https://cdn1.fahasa.com/media/catalog/product/8/9/8936024919047_1_1.jpg', 135000.00, 79000.00, 2019, N'NXB Hội Nhà Văn', 58, 14, N'Rừng Na Uy (Norwegian Wood)', '2026-10-26 21:02:07.9721706', 9, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (75, 1, N'Gabriel García Márquez', N'VERY_GOOD', N'Đỉnh cao của chủ nghĩa hiện thực huyền ảo về dòng họ Buendía ở Macondo', '2026-10-26 21:02:07.9763057', NULL, N'ISBN9786049661018', N'https://cdn1.fahasa.com/media/catalog/product/t/r/tram_nam_co_don-01.jpg', 180000.00, 105000.00, 2018, N'NXB Văn Học', 34, 7, N'Trăm Năm Cô Đơn (Cien años de soledad)', '2026-10-26 21:02:07.9763057', 9, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (76, 1, N'Hector Malot', N'LIKE_NEW', N'Hành trình lưu lạc đầy nghị lực của chú bé Rémi và gánh xiếc cụ Vitalis', '2026-10-26 21:02:07.9803062', NULL, N'ISBN9786049671017', N'https://thumb.wikimedia.org/wikipedia/vi/thumb/f/fa/Kh%C3%B4ng_gia_%C4%91%C3%ACnh_1_%28s%C3%A1ch%29.jpg/330px-Kh%C3%B4ng_gia_%C4%91%C3%ACnh_1_%28s%C3%A1ch%29.jpg', 160000.00, 95000.00, 2020, N'NXB Văn Học', 41, 9, N'Không Gia Đình (Sans Famille)', '2026-10-26 21:02:07.9803062', 9, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (77, 1, N'Jeffrey Archer', N'VERY_GOOD', N'Tiểu thuyết kinh điển về cuộc đối đầu định mệnh giữa hai người đàn ông kiệt xuất', '2026-10-26 21:02:07.9843091', NULL, N'ISBN9786049681016', N'https://upload.wikimedia.org/wikipedia/en/f/f3/KaneAndAbel.jpg', 175000.00, 102000.00, 2019, N'NXB Văn Học', 36, 8, N'Hai Số Phận (Kane and Abel)', '2026-10-26 21:02:07.9843091', 9, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (78, 1, N'Mario Puzo', N'LIKE_NEW', N'Bản dịch xuất sắc của Ngọc Thứ Lang, thế giới ngầm mafia và tình cảm gia đình', '2026-10-26 21:02:07.9883435', NULL, N'ISBN9786049691015', N'https://cdn1.fahasa.com/media/catalog/product/b/o/bo_gia_3845-2_1.jpg', 145000.00, 88000.00, 2019, N'NXB Văn Học', 62, 13, N'Bố Già (The Godfather)', '2026-10-26 21:02:07.9883435', 9, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (79, 1, N'Victor Hugo', N'VERY_GOOD', N'Bản in bìa cứng dày dặn, thiên sử thi về lòng nhân ái và công lý', '2026-10-26 21:02:07.9918099', NULL, N'ISBN9786049701011', N'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/99/Ebcosette.jpg/330px-Ebcosette.jpg', 290000.00, 175000.00, 2018, N'NXB Văn Học', 23, 5, N'Những Người Khốn Khổ (Trọn Bộ 2 Tập)', '2026-10-26 21:02:07.9918099', 9, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (80, 1, N'Lev Tolstoy', N'VERY_GOOD', N'Kiệt tác vĩ đại nhất của văn học Nga thế kỷ 19, sách đẹp được bảo quản kỹ', '2026-10-26 21:02:07.9949917', NULL, N'ISBN9786049711010', N'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/af/Tolstoy_-_War_and_Peace_-_first_edition%2C_1869.jpg/330px-Tolstoy_-_War_and_Peace_-_first_edition%2C_1869.jpg', 350000.00, 210000.00, 2019, N'NXB Văn Học', 17, 4, N'Chiến Tranh Và Hòa Bình (Trọn Bộ)', '2026-10-26 21:02:07.9949917', 9, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (81, 1, N'Ayn Rand', N'LIKE_NEW', N'Tuyên ngôn về chủ nghĩa cá nhân và sự sáng tạo kiến trúc của Howard Roark', '2026-10-26 21:02:08.0010880', NULL, N'ISBN9786041132016', N'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6e/The_Fountain_Head_%281943_1st_ed%29_-_Ayn_Rand.jpg/330px-The_Fountain_Head_%281943_1st_ed%29_-_Ayn_Rand.jpg', 260000.00, 155000.00, 2018, N'NXB Trẻ', 29, 7, N'Suối Nguồn (The Fountainhead)', '2026-10-26 21:02:08.0010880', 9, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (82, 1, N'Harper Lee', N'LIKE_NEW', N'Bài học về lòng dũng cảm, công lý và lòng thấu cảm qua con mắt trẻ thơ', '2026-10-26 21:02:08.0039921', NULL, N'ISBN9786049721019', N'https://upload.wikimedia.org/wikipedia/vi/5/5c/Mockingbirdfirst.JPG', 110000.00, 68000.00, 2020, N'NXB Văn Học', 45, 10, N'Giết Con Chim Nhại (To Kill a Mockingbird)', '2026-10-26 21:02:08.0039921', 9, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (83, 1, N'George Orwell', N'LIKE_NEW', N'Tác phẩm phản địa đàng (dystopian) kinh điển cảnh báo về quyền tự do cá nhân', '2026-10-26 21:02:08.0084518', NULL, N'ISBN9786049922012', N'https://covers.openlibrary.org/b/isbn/9780451524935-L.jpg', 125000.00, 75000.00, 2020, N'NXB Hội Nhà Văn', 48, 11, N'1984 (Một Chín Tám Tư)', '2026-10-26 21:02:08.0084518', 9, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (84, 1, N'George Orwell', N'VERY_GOOD', N'Truyện ngụ ngôn chính trị sâu cay và sắc sảo, tình trạng sách rất tốt 92%', '2026-10-26 21:02:08.0117651', NULL, N'ISBN9786049923019', N'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fb/Animal_Farm_-_1st_edition.jpg/330px-Animal_Farm_-_1st_edition.jpg', 85000.00, 50000.00, 2019, N'NXB Hội Nhà Văn', 37, 9, N'Trại Súc Vật (Animal Farm)', '2026-10-26 21:02:08.0117651', 9, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (85, 1, N'Stephen Hawking', N'LIKE_NEW', N'Cuốn sách phổ biến khoa học vũ trụ từ Vụ Nổ Lớn đến Lỗ Đen nổi tiếng nhất', '2026-10-26 21:02:08.0166545', NULL, N'ISBN9786041153011', N'https://cdn1.fahasa.com/media/catalog/product/8/9/8934974179238.jpg', 125000.00, 75000.00, 2019, N'NXB Trẻ', 49, 12, N'Lược Sử Thời Gian (A Brief History of Time)', '2026-10-26 21:02:08.0166545', 10, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (86, 1, N'Stephen Hawking', N'LIKE_NEW', N'Khám phá các bí ẩn của lý thuyết M, thời gian đa chiều kèm hình màu trực quan', '2026-10-26 21:02:08.0208668', NULL, N'ISBN9786041163010', N'https://upload.wikimedia.org/wikipedia/vi/6/6c/Cover_Universe_in_a_nutshell.jpg', 160000.00, 98000.00, 2020, N'NXB Trẻ', 35, 8, N'Vũ Trụ Trong Vỏ Hạt Dẻ', '2026-10-26 21:02:08.0208668', 10, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (87, 1, N'Carl Sagan', N'VERY_GOOD', N'Cuộc du hành kỳ thú khám phá 15 tỷ năm tiến hóa của vũ trụ và nền văn minh', '2026-10-26 21:02:08.0259719', NULL, N'ISBN9786047771011', N'https://covers.openlibrary.org/b/isbn/9780345331359-L.jpg', 210000.00, 125000.00, 2019, N'NXB Thế Giới', 27, 6, N'Cosmos (Vũ Trụ)', '2026-10-26 21:02:08.0259719', 10, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (88, 1, N'Yuval Noah Harari', N'LIKE_NEW', N'Tác phẩm hiện tượng toàn cầu giải mã 70.000 năm lịch sử loài người', '2026-10-26 21:02:08.0308959', NULL, N'ISBN9786049431013', N'https://cdn1.fahasa.com/media/catalog/product/8/9/8935270703554.jpg', 239000.00, 145000.00, 2018, N'NXB Tri Thức', 68, 15, N'Sapiens - Lược Sử Loài Người', '2026-10-26 21:02:08.0308959', 10, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (89, 1, N'Yuval Noah Harari', N'LIKE_NEW', N'Dự phóng về số phận của nhân loại trong kỷ nguyên trí tuệ nhân tạo và công nghệ sinh học', '2026-10-26 21:02:08.0354401', NULL, N'ISBN9786047782017', N'https://covers.openlibrary.org/b/isbn/9780062464316-L.jpg', 229000.00, 138000.00, 2019, N'NXB Thế Giới', 47, 11, N'Homo Deus - Lược Sử Tương Lai', '2026-10-26 21:02:08.0354401', 10, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (90, 1, N'Yuval Noah Harari', N'VERY_GOOD', N'Những vấn đề cấp bách nhất hiện nay: tin giả, chiến tranh, khủng hoảng khí hậu', '2026-10-26 21:02:08.0388980', NULL, N'ISBN9786047783014', N'https://covers.openlibrary.org/b/isbn/9780525512172-L.jpg', 219000.00, 129000.00, 2019, N'NXB Thế Giới', 39, 9, N'21 Bài Học Cho Thế Kỷ 21', '2026-10-26 21:02:08.0388980', 10, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (91, 1, N'Richard Dawkins', N'VERY_GOOD', N'Tác phẩm kinh điển về thuyết tiến hóa giải thích hành vi động vật và con người', '2026-10-26 21:02:08.0440247', NULL, N'ISBN9786049441012', N'https://covers.openlibrary.org/b/isbn/9780192860927-L.jpg', 210000.00, 125000.00, 2020, N'NXB Tri Thức', 22, 6, N'Gen Vị Kỷ (The Selfish Gene)', '2026-10-26 21:02:08.0440247', 10, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (92, 1, N'Bill Bryson', N'GOOD', N'Chuyến du hành khoa học từ thuyết Vụ Nổ Lớn đến sự trỗi dậy của loài người', '2026-10-26 21:02:08.0510258', NULL, N'ISBN9786047784011', N'https://upload.wikimedia.org/wikipedia/en/e/ed/Bill_bryson_a_short_history.jpg', 195000.00, 110000.00, 2018, N'NXB Thế Giới', 26, 7, N'Lược Sử Vạn Vật', '2026-10-26 21:02:08.0510258', 10, 1, NULL);
INSERT INTO dbo.[books] ([id], [active], [author], [book_condition], [condition_note], [created_at], [description], [isbn], [main_image_url], [original_price], [price], [publish_year], [publisher], [sold_quantity], [stock], [title], [updated_at], [category_id], [shop_id], [promotional_price]) VALUES (93, 1, N'Hoàng đế La Mã Marcus Aurelius', N'LIKE_NEW', N'Tác phẩm triết học Khắc Kỷ (Stoicism) vĩ đại nhất của bậc minh quân La Mã', '2026-10-26 21:02:08.0590239', NULL, N'ISBN9786043141015', N'https://covers.openlibrary.org/b/isbn/9780140449334-L.jpg', 120000.00, 75000.00, 2021, N'NXB Dân Trí', 53, 12, N'Suy Tưởng (Meditations)', '2026-10-26 21:02:08.0590239', 10, 1, NULL);
SET IDENTITY_INSERT dbo.[books] OFF;
GO

-- ===== DU LIEU: chat_messages (2 dong) =====
SET IDENTITY_INSERT dbo.[chat_messages] ON;
INSERT INTO dbo.[chat_messages] ([id], [content], [is_read], [sent_at], [book_id], [receiver_id], [sender_id]) VALUES (1, N'tôi c?n tu v?n sachas', 0, '2026-10-27 00:36:13.2373224', NULL, 4, 4);
INSERT INTO dbo.[chat_messages] ([id], [content], [is_read], [sent_at], [book_id], [receiver_id], [sender_id]) VALUES (2, N'hi', 0, '2026-10-27 00:36:26.6745252', NULL, 4, 4);
SET IDENTITY_INSERT dbo.[chat_messages] OFF;
GO

-- ===== DU LIEU: orders (2 dong) =====
SET IDENTITY_INSERT dbo.[orders] ON;
INSERT INTO dbo.[orders] ([id], [created_at], [discount_amount], [note], [order_code], [payment_method], [payment_status], [recipient_name], [recipient_phone], [shipping_address], [shipping_fee], [status], [subtotal], [total_amount], [updated_at], [user_id], [shipper_id], [shop_id]) VALUES (1, '2026-10-25 17:32:57.7323473', 0.00, N'Giao gi? hành chính', N'ORD1792924377730', N'COD', N'COMPLETED', N'Khách Mua Sách (User)', N'0900000005', N'123 Lê Văn Việt, Tăng Nhơn Phú B, TP. Thủ Đức', 25000.00, N'DELIVERED', 65000.00, 90000.00, '2026-10-25 17:32:57.7323473', 5, 3, 1);
INSERT INTO dbo.[orders] ([id], [created_at], [discount_amount], [note], [order_code], [payment_method], [payment_status], [recipient_name], [recipient_phone], [shipping_address], [shipping_fee], [status], [subtotal], [total_amount], [updated_at], [user_id], [shipper_id], [shop_id]) VALUES (2, '2026-10-26 22:15:05.0562750', 0.00, N'', N'ORD20261026-0007', N'COD', N'PENDING', N'duc', N'0789771806', N'Quảng Ngãi', 25000.00, N'SHIPPING', 52000.00, 77000.00, '2026-10-27 15:09:29.6138149', 5, NULL, 1);
SET IDENTITY_INSERT dbo.[orders] OFF;
GO

-- ===== DU LIEU: payments (1 dong) =====
SET IDENTITY_INSERT dbo.[payments] ON;
INSERT INTO dbo.[payments] ([id], [amount], [created_at], [payment_info], [payment_method], [payment_status], [payment_time], [transaction_id], [order_id]) VALUES (1, 77000.00, '2026-10-26 22:15:05.0702168', N'Thanh toán don hàng ORD20261026-0007', N'COD', N'PENDING', NULL, NULL, 2);
SET IDENTITY_INSERT dbo.[payments] OFF;
GO

-- ===== DU LIEU: viewed_products (4 dong) =====
SET IDENTITY_INSERT dbo.[viewed_products] ON;
INSERT INTO dbo.[viewed_products] ([id], [viewed_at], [book_id], [user_id]) VALUES (1, '2026-10-26 14:32:16.0792994', 8, 5);
INSERT INTO dbo.[viewed_products] ([id], [viewed_at], [book_id], [user_id]) VALUES (2, '2026-10-26 22:13:50.2441874', 72, 5);
INSERT INTO dbo.[viewed_products] ([id], [viewed_at], [book_id], [user_id]) VALUES (3, '2026-10-26 22:15:35.8143239', 81, 5);
INSERT INTO dbo.[viewed_products] ([id], [viewed_at], [book_id], [user_id]) VALUES (4, '2026-10-27 12:36:09.6390198', 4, 5);
SET IDENTITY_INSERT dbo.[viewed_products] OFF;
GO

-- ===== DU LIEU: order_items (1 dong) =====
SET IDENTITY_INSERT dbo.[order_items] ON;
INSERT INTO dbo.[order_items] ([id], [price], [quantity], [book_id], [order_id]) VALUES (1, 52000.00, 1, 72, 2);
SET IDENTITY_INSERT dbo.[order_items] OFF;
GO

-- ===== DU LIEU: order_status_histories (4 dong) =====
SET IDENTITY_INSERT dbo.[order_status_histories] ON;
INSERT INTO dbo.[order_status_histories] ([id], [created_at], [note], [status], [order_id], [updated_by_user_id]) VALUES (1, '2026-10-26 22:15:05.0665086', N'Đơn hàng mới tạo thành công', N'NEW', 2, 5);
INSERT INTO dbo.[order_status_histories] ([id], [created_at], [note], [status], [order_id], [updated_by_user_id]) VALUES (2, '2026-10-27 14:55:32.6085725', N'Shop cập nhật trạng thái đơn sang: Đã xác nhận', N'CONFIRMED', 2, 4);
INSERT INTO dbo.[order_status_histories] ([id], [created_at], [note], [status], [order_id], [updated_by_user_id]) VALUES (3, '2026-10-27 14:55:48.3798982', N'Shop cập nhật trạng thái đơn sang: Đã lấy hàng', N'PICKED_UP', 2, 4);
INSERT INTO dbo.[order_status_histories] ([id], [created_at], [note], [status], [order_id], [updated_by_user_id]) VALUES (4, '2026-10-27 15:09:29.5529515', N'Đã bàn giao cho đơn vị vận chuyển Viettel Post thành công', N'SHIPPING', 2, 4);
SET IDENTITY_INSERT dbo.[order_status_histories] OFF;
GO

-- ===== RANG BUOC KHOA NGOAI =====
ALTER TABLE dbo.[addresses] ADD CONSTRAINT [FK1fa36y2oqhao3wgg2rw1pi459] FOREIGN KEY ([user_id]) REFERENCES dbo.[users]([id]);
ALTER TABLE dbo.[app_commissions] ADD CONSTRAINT [FKed0i55ul0ruep2mpx5tttbv28] FOREIGN KEY ([shop_id]) REFERENCES dbo.[shops]([id]);
ALTER TABLE dbo.[book_images] ADD CONSTRAINT [FKcnpy06tjmrsjisjf2bqpuvvbl] FOREIGN KEY ([book_id]) REFERENCES dbo.[books]([id]);
ALTER TABLE dbo.[books] ADD CONSTRAINT [FKleqa3hhc0uhfvurq6mil47xk0] FOREIGN KEY ([category_id]) REFERENCES dbo.[categories]([id]);
ALTER TABLE dbo.[books] ADD CONSTRAINT [FKmfjhptaju03egl87fhelf5wbs] FOREIGN KEY ([shop_id]) REFERENCES dbo.[shops]([id]);
ALTER TABLE dbo.[cart_items] ADD CONSTRAINT [FKhiu1jw80o45wfiw5tgok1xpkl] FOREIGN KEY ([book_id]) REFERENCES dbo.[books]([id]);
ALTER TABLE dbo.[cart_items] ADD CONSTRAINT [FKpcttvuq4mxppo8sxggjtn5i2c] FOREIGN KEY ([cart_id]) REFERENCES dbo.[carts]([id]);
ALTER TABLE dbo.[carts] ADD CONSTRAINT [FKb5o626f86h46m4s7ms6ginnop] FOREIGN KEY ([user_id]) REFERENCES dbo.[users]([id]);
ALTER TABLE dbo.[chat_messages] ADD CONSTRAINT [FKand7mh9iu4kt3n1tn2w9i9of0] FOREIGN KEY ([receiver_id]) REFERENCES dbo.[users]([id]);
ALTER TABLE dbo.[chat_messages] ADD CONSTRAINT [FKca83dswfr64wbp6oy13y9tmf4] FOREIGN KEY ([book_id]) REFERENCES dbo.[books]([id]);
ALTER TABLE dbo.[chat_messages] ADD CONSTRAINT [FKgiqeap8ays4lf684x7m0r2729] FOREIGN KEY ([sender_id]) REFERENCES dbo.[users]([id]);
ALTER TABLE dbo.[order_items] ADD CONSTRAINT [FKbioxgbv59vetrxe0ejfubep1w] FOREIGN KEY ([order_id]) REFERENCES dbo.[orders]([id]);
ALTER TABLE dbo.[order_items] ADD CONSTRAINT [FKi4ptndslo2pyfp9r1x0eulh9g] FOREIGN KEY ([book_id]) REFERENCES dbo.[books]([id]);
ALTER TABLE dbo.[order_status_histories] ADD CONSTRAINT [FKgair3047h1rin6u8y0fvc1fg5] FOREIGN KEY ([order_id]) REFERENCES dbo.[orders]([id]);
ALTER TABLE dbo.[order_status_histories] ADD CONSTRAINT [FKsv75mqi43d3qrifengdypqud2] FOREIGN KEY ([updated_by_user_id]) REFERENCES dbo.[users]([id]);
ALTER TABLE dbo.[orders] ADD CONSTRAINT [FK21gttsw5evi5bbsvleui69d7r] FOREIGN KEY ([shop_id]) REFERENCES dbo.[shops]([id]);
ALTER TABLE dbo.[orders] ADD CONSTRAINT [FK32ql8ubntj5uh44ph9659tiih] FOREIGN KEY ([user_id]) REFERENCES dbo.[users]([id]);
ALTER TABLE dbo.[orders] ADD CONSTRAINT [FKmjnf91amspoh2m4qg8ct5y8uj] FOREIGN KEY ([shipper_id]) REFERENCES dbo.[users]([id]);
ALTER TABLE dbo.[payments] ADD CONSTRAINT [FK81gagumt0r8y3rmudcgpbk42l] FOREIGN KEY ([order_id]) REFERENCES dbo.[orders]([id]);
ALTER TABLE dbo.[review_media] ADD CONSTRAINT [FK5eeii8rke42oss6yq63k2hrte] FOREIGN KEY ([review_id]) REFERENCES dbo.[reviews]([id]);
ALTER TABLE dbo.[reviews] ADD CONSTRAINT [FK6a9k6xvev80se5rreqvuqr7f9] FOREIGN KEY ([book_id]) REFERENCES dbo.[books]([id]);
ALTER TABLE dbo.[reviews] ADD CONSTRAINT [FKcgy7qjc1r99dp117y9en6lxye] FOREIGN KEY ([user_id]) REFERENCES dbo.[users]([id]);
ALTER TABLE dbo.[reviews] ADD CONSTRAINT [FKqwgq1lxgahsxdspnwqfac6sv6] FOREIGN KEY ([order_id]) REFERENCES dbo.[orders]([id]);
ALTER TABLE dbo.[shops] ADD CONSTRAINT [FKrduswa89ayj0poad3l70nag19] FOREIGN KEY ([owner_id]) REFERENCES dbo.[users]([id]);
ALTER TABLE dbo.[user_roles] ADD CONSTRAINT [FKh8ciramu9cc9q3qcqiv4ue8a6] FOREIGN KEY ([role_id]) REFERENCES dbo.[roles]([id]);
ALTER TABLE dbo.[user_roles] ADD CONSTRAINT [FKhfh9dx7w3ubf1co1vdev94g3f] FOREIGN KEY ([user_id]) REFERENCES dbo.[users]([id]);
ALTER TABLE dbo.[viewed_products] ADD CONSTRAINT [FK4f4tspa14iw47yuj83okbdvpi] FOREIGN KEY ([user_id]) REFERENCES dbo.[users]([id]);
ALTER TABLE dbo.[viewed_products] ADD CONSTRAINT [FKhdwst4x5fiaq5gvgbj9gi54ha] FOREIGN KEY ([book_id]) REFERENCES dbo.[books]([id]);
ALTER TABLE dbo.[wishlists] ADD CONSTRAINT [FK330pyw2el06fn5g28ypyljt16] FOREIGN KEY ([user_id]) REFERENCES dbo.[users]([id]);
ALTER TABLE dbo.[wishlists] ADD CONSTRAINT [FKo69vdp48axn9ovctfp0jrlgum] FOREIGN KEY ([book_id]) REFERENCES dbo.[books]([id]);
GO
-- ===== HET FILE =====

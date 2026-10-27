#!/bin/bash
# ==============================================================================
# Script tự động khởi tạo cơ sở dữ liệu bookstore_db trong SQL Server Container
# ==============================================================================

echo ">> Đang chờ Microsoft SQL Server 2022 khởi động hoàn tất..."

# Kiểm tra kết nối SQL Server
for i in {1..60}; do
    /opt/mssql-tools18/bin/sqlcmd -S sqlserver -U sa -P "${MSSQL_SA_PASSWORD:-Password123@}" -C -Q "SELECT 1" > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo ">> SQL Server đã sẵn sàng tiếp nhận kết nối!"
        break
    fi
    echo ">> SQL Server chưa sẵn sàng, thử lại sau 2 giây... ($i/60)"
    sleep 2
done

echo ">> Kiểm tra và tạo database 'bookstore_db' nếu chưa tồn tại..."
/opt/mssql-tools18/bin/sqlcmd -S sqlserver -U sa -P "${MSSQL_SA_PASSWORD:-Password123@}" -C -Q "
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'bookstore_db')
BEGIN
    CREATE DATABASE bookstore_db COLLATE Latin1_General_100_CI_AS_SC_UTF8;
    PRINT 'Đã tạo database bookstore_db thành công với chuẩn UTF-8!';
END
ELSE
BEGIN
    PRINT 'Database bookstore_db đã tồn tại, tiếp tục vận hành.';
END
"

echo ">> Khởi tạo database hoàn tất! Sẵn sàng cho ứng dụng Spring Boot kết nối."

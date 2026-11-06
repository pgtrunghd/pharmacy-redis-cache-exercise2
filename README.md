# Bài tập — Đồng bộ dữ liệu và TTL cho Redis Cache

Đề bài: https://portal.rikkei.edu.vn/learn/159/homework/8342

Phát triển tiếp bài 8341. Thuốc lưu trong PostgreSQL, chi tiết thuốc được cache trong Redis theo khóa `medicines::{id}`. Cache `medicines` có TTL 60 phút. Khi cập nhật thuốc, `MedicineService.update` dùng `@CacheEvict(value = "medicines", key = "#medicine.id")` để xóa đúng khóa sau khi lưu thay đổi.

## Chạy

Cần JDK 21, PostgreSQL, Redis. Tạo database `pharmacy_cache_db` và tài khoản có quyền tạo bảng. Nếu macOS đang dùng JDK khác: `export JAVA_HOME=$(/usr/libexec/java_home -v 21)`.

```bash
DB_PASSWORD='mat-khau-postgresql-cua-ban' ./gradlew bootRun
```

Các biến tùy chọn: `DB_USERNAME` (`postgres`), `DB_NAME` (`pharmacy_cache_db`), `DB_PORT` (`5432`), `REDIS_HOST` (`localhost`), `REDIS_PORT` (`6379`), `SERVER_PORT` (`8081`).

## Kiểm tra

Tạo thuốc ID 1 (trong database mới):

```bash
curl -fsS -X POST http://localhost:8081/api/v1/medicines \
  -H 'Content-Type: application/json' \
  -d '{"name":"Paracetamol 500mg","price":12000}'
```

Gọi hai lần:

```bash
curl -fsS http://localhost:8081/api/v1/medicines/1
curl -fsS http://localhost:8081/api/v1/medicines/1
redis-cli TTL 'medicines::1'
```

Console chỉ có một câu `Hibernate: select ... from medicines ...` cho hai lần GET. TTL gần 3600 giây.

Sửa giá:

```bash
curl -fsS -X PUT http://localhost:8081/api/v1/medicines/1 \
  -H 'Content-Type: application/json' \
  -d '{"name":"Paracetamol 500mg","price":15000}'
redis-cli EXISTS 'medicines::1'
curl -fsS http://localhost:8081/api/v1/medicines/1
```

`EXISTS` trả `0` ngay sau PUT. GET tiếp theo trả giá `15000.00`, chạy SQL để nạp dữ liệu mới và tạo lại khóa Redis.

Đã kiểm tra toàn luồng với PostgreSQL và Redis tạm: TTL ban đầu `3600`, khóa biến mất sau PUT, giá mới được trả về và SQL xuất hiện lại. Chạy `./gradlew clean test bootJar` để kiểm tra build.

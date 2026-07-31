# IT Admin Roadmap

Tai lieu nay goi y cach bat dau xay dung phan IT Admin cho project backend hien tai. Muc tieu la tach ro giua admin ban hang thong thuong va IT Admin quan tri he thong, bao mat, giam sat va van hanh.

## 1. Ket luan nhanh

Project hien tai da du tot de bat dau lam admin dashboard o muc development. Backend da co cac nen tang quan trong:

- JWT authentication.
- Role `ADMIN`.
- Nhieu API da bao ve bang `@PreAuthorize("hasRole('ADMIN')")`.
- Cac module quan trong: user, product, order, discount, voucher, return, review, statistic, blog, policy.
- Spring Boot Actuator da co trong dependency, co the tan dung de lam health check.

Tuy nhien, project chua nen xem la production-ready cho IT Admin that su vi con cac van de:

- Secret dang hardcode trong `application.properties` va `docker-compose.yml`.
- Dang bat stacktrace trong response loi.
- `spring.jpa.hibernate.ddl-auto=update` chua phu hop production.
- Chua co test tu dong ro rang.
- Con nhieu `System.out` va `printStackTrace`.
- Chua co audit log cho hanh dong quan tri.

## 2. Nen hieu IT Admin la gi trong project nay

Admin ban hang thuong tap trung vao:

- Quan ly san pham.
- Quan ly don hang.
- Quan ly voucher/discount.
- Quan ly review.
- Xem doanh thu va thong ke.

IT Admin nen tap trung them vao:

- Quan ly user va role.
- Theo doi tinh trang backend, database, Redis, Elasticsearch, ChromaDB.
- Theo doi loi va log.
- Theo doi bao mat dang nhap.
- Audit hanh dong quan tri.
- Quan ly cau hinh van hanh an toan.
- Trigger cac tac vu he thong nhu sync statistic, reindex search, clear cache.

## 3. Module nen lam dau tien

### 3.1 Admin login va route guard

Muc tieu:

- Admin dang nhap bang API hien co.
- Frontend doc JWT token.
- Goi `/api/users/my-profile` de lay role.
- Chi cho user co role `ADMIN` hoac `IT_ADMIN` vao trang admin.

Can co:

- Trang login rieng cho admin.
- Middleware/route guard.
- Xu ly token het han.
- Nut logout.

Neu chua co role `IT_ADMIN`, co the bat dau bang `ADMIN` truoc.

### 3.2 Admin layout

Nen co sidebar don gian:

- Dashboard.
- Users.
- Orders.
- Products.
- Discounts/Vouchers.
- Returns.
- Reviews.
- Statistics.
- System.
- Logs/Audit.

Nen co topbar:

- Ten admin dang dang nhap.
- Role hien tai.
- Nut logout.

### 3.3 User Management

Day la module nen lam som vi backend da co API `/api/users`.

Chuc nang nen co:

- Xem danh sach user.
- Tim kiem theo email, phone, name.
- Loc theo role va status.
- Xem chi tiet user.
- Tao user admin/staff.
- Khoa/mo khoa user.
- Doi role user.
- Reset password hoac tao password moi.

Can can than:

- Khong cho admin thuong tu nang role len `SUPER_ADMIN`.
- Khong cho user tu khoa chinh minh.
- Nen co confirm dialog truoc khi khoa user hoac doi role.

### 3.4 Order Management

Muc tieu:

- Xem tat ca don hang.
- Loc theo status.
- Xem chi tiet don hang.
- Cap nhat trang thai don.

Nen co cac trang thai ro rang:

- Pending.
- Confirmed.
- Shipping.
- Completed.
- Cancelled.
- Returned.

Can can than:

- Khong cho cap nhat trang thai lung tung, vi du `Completed` quay lai `Pending`.
- Nen hien thi lich su cap nhat don hang neu sau nay co audit log.

### 3.5 Dashboard tong quan

Muc tieu:

- Admin nhin nhanh tinh trang shop.

Nen co:

- Doanh thu hom nay/thang nay.
- So don moi.
- So user moi.
- Don dang cho xu ly.
- Don return/refund.
- Top san pham ban chay.
- Bieu do doanh thu theo thang.

Co the dung API statistic hien co truoc, sau do bo sung sau.

## 4. Module IT Admin dung nghia

### 4.1 System Health

Nen tao trang System de xem:

- Backend status.
- Database status.
- Redis status.
- Elasticsearch status.
- ChromaDB status.
- Disk/memory basic neu co.

Backend co the expose cac endpoint:

- `GET /api/admin/system/health`
- `GET /api/admin/system/dependencies`
- `GET /api/admin/system/version`

Co the tan dung Spring Actuator, nhung khong nen public actuator endpoints ra ngoai.

### 4.2 Audit Log

Audit log la diem rat quan trong cho IT Admin.

Nen log cac hanh dong:

- Admin dang nhap.
- Dang nhap that bai.
- Tao user.
- Doi role user.
- Khoa/mo khoa user.
- Xoa/sua san pham.
- Doi trang thai don hang.
- Tao/sua voucher.
- Trigger sync/reindex.

Bang goi y:

```text
audit_logs
- id
- actor_user_id
- actor_email
- action
- target_type
- target_id
- old_value
- new_value
- ip_address
- user_agent
- created_at
```

Frontend nen co trang:

- Danh sach audit log.
- Loc theo admin.
- Loc theo action.
- Loc theo ngay.
- Xem chi tiet thay doi.

### 4.3 Error Log va Monitoring

Nen co trang Logs de xem:

- Loi backend gan day.
- Endpoint loi nhieu nhat.
- So luong loi 4xx/5xx.
- Loi theo thoi gian.
- Chi tiet exception da duoc rut gon an toan.

Khong nen hien stacktrace day du cho admin frontend neu co thong tin nhay cam. Nen chi hien message da sanitize va trace id.

Nen bo sung:

- Request ID / Trace ID.
- Logging bang SLF4J thay cho `System.out`.
- Global exception response chuan.

### 4.4 System Tools

Trang System Tools co the co cac nut:

- Trigger statistic sync.
- Reindex Elasticsearch.
- Clear Redis cache theo key prefix.
- Test email SMTP.
- Test Cloudinary upload config.
- Test AI/Gemini key.

Can can than:

- Chi `IT_ADMIN` hoac `SUPER_ADMIN` moi duoc dung.
- Nut nguy hiem phai co confirm.
- Moi lan bam phai ghi audit log.

## 5. Phan quyen nen thiet ke

Ban co the bat dau voi:

- `CUSTOMER`: khach hang.
- `ADMIN`: quan tri shop.
- `IT_ADMIN`: quan tri ky thuat.
- `SUPER_ADMIN`: toan quyen.

Goi y quyen:

```text
CUSTOMER
- Xem/sua profile cua minh
- Dat hang
- Review
- Return request

ADMIN
- Quan ly san pham
- Quan ly don hang
- Quan ly voucher
- Quan ly review
- Xem thong ke ban hang

IT_ADMIN
- Xem system health
- Xem logs
- Xem audit logs
- Trigger sync/reindex
- Quan ly cau hinh van hanh

SUPER_ADMIN
- Tat ca quyen
- Doi role admin khac
- Tao IT admin
- Thao tac nguy hiem
```

Neu project chua can phuc tap, co the lam `ADMIN` truoc, nhung nen code theo huong sau nay them role khong kho.

## 6. Viec backend nen sua truoc khi deploy nghiem tuc

### 6.1 Dua secret ra environment variables

Khong nen hardcode:

- DB password.
- JWT secret.
- Redis URL.
- Gmail app password.
- Cloudinary secret.
- FPT API key.
- Payment secret.

Nen doi sang dang:

```properties
spring.datasource.password=${DB_PASSWORD}
app.jwt-secret=${JWT_SECRET}
spring.mail.password=${MAIL_PASSWORD}
cloudinary.api-secret=${CLOUDINARY_API_SECRET}
```

Sau khi secret da tung nam trong repo, nen rotate key.

### 6.2 Tat stacktrace trong response

Production nen dung:

```properties
server.error.include-message=never
server.error.include-stacktrace=never
server.error.include-exception=false
```

Development co the bat bang profile rieng.

### 6.3 Thay `ddl-auto=update`

Production nen dung:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Va quan ly schema bang Flyway hoac Liquibase.

### 6.4 Doi logging

Nen thay:

- `System.out.println`
- `printStackTrace`

Bang:

- `log.info`
- `log.warn`
- `log.error`

Dung Lombok:

```java
@Slf4j
```

### 6.5 Them test toi thieu

Nen co test cho:

- Login.
- Role guard.
- User management.
- Order status update.
- Payment callback.
- Voucher/discount logic.

## 7. Roadmap de lam theo tuan tu

### Phase 1: Admin co ban

- Admin login.
- Admin layout.
- User list.
- User detail.
- Lock/unlock user.
- Order list.
- Order detail.
- Update order status.

### Phase 2: Shop operation admin

- Product CRUD.
- Category/brand CRUD.
- Voucher/discount CRUD.
- Return/refund management.
- Review management.
- Dashboard statistics.

### Phase 3: IT Admin

- System health page.
- Dependency status.
- Audit log.
- Error log.
- Trigger statistic sync.
- Reindex Elasticsearch.
- Clear cache.

### Phase 4: Bao mat va production hardening

- Env var cho secret.
- Rotate exposed keys.
- Tat stacktrace.
- Logging chuan.
- Migration DB.
- Test tu dong.
- Rate limit login.
- Refresh token hoac token lifecycle tot hon.

## 8. Thu tu nen lam ngay bay gio

Neu can bat dau ngay, nen lam theo thu tu nay:

1. Tao admin frontend layout.
2. Lam admin login va route guard.
3. Lam trang Users.
4. Lam trang Orders.
5. Lam trang Dashboard.
6. Them role `IT_ADMIN`.
7. Them System Health API.
8. Them Audit Log.
9. Xu ly secret va config production.

## 9. Tieu chi xem la "du on"

Co the xem project du on de demo admin khi:

- Admin dang nhap duoc.
- User khong co role admin khong vao duoc admin.
- Quan ly duoc user.
- Quan ly duoc don hang.
- Xem duoc thong ke co ban.
- Cac loi API duoc hien thi dep tren frontend.

Co the xem project du on de deploy nghiem tuc khi:

- Khong con secret hardcode.
- Da rotate cac key tung bi commit.
- Tat stacktrace response.
- Co logging chuan.
- Co audit log.
- Co test cho cac luong quan trong.
- Co DB migration.
- Co health check.
- Co backup/restore DB.


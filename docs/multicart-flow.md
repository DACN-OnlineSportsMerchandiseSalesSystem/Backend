# Multicart Flow Proposal

## 1. Mục tiêu

Hiện tại giỏ hàng thường được hiểu là mỗi người dùng chỉ có một cart đang hoạt động. Với multicart, một người dùng có thể tạo nhiều cart khác nhau để gom sản phẩm theo từng mục đích mua sắm.

Ví dụ:

- Cart "Setup phong lam viec": ban, ghe, den.
- Cart "Mua cho gia dinh": ao, tui, giay.
- Cart "Mua sau": tai nghe, sac du phong.

Khi muốn mua nhóm sản phẩm nào, người dùng mở cart đó và nhấn thanh toán. Checkout chỉ áp dụng cho các item nằm trong cart được chọn.

## 2. Khái niệm chính

### Cart

Cart là một danh sách mua hàng thuộc về một người dùng.

Thông tin đề xuất:

| Field | Ý nghĩa |
| --- | --- |
| `id` | ID của cart |
| `user_id` | Người sở hữu cart |
| `name` | Tên cart do người dùng đặt |
| `status` | Trạng thái cart |
| `is_default` | Đánh dấu cart mặc định |
| `created_at` | Thời điểm tạo |
| `updated_at` | Thời điểm cập nhật gần nhất |

Trạng thái đề xuất:

| Status | Ý nghĩa |
| --- | --- |
| `ACTIVE` | Cart đang dùng |
| `CHECKED_OUT` | Cart đã được dùng để tạo order |
| `ARCHIVED` | Cart đã bị ẩn/xóa mềm |

### Cart Item

Cart item là một sản phẩm nằm trong một cart cụ thể.

Thông tin đề xuất:

| Field | Ý nghĩa |
| --- | --- |
| `id` | ID của cart item |
| `cart_id` | Cart chứa item |
| `product_id` | Sản phẩm |
| `variant_id` | Biến thể sản phẩm nếu có, ví dụ size/màu |
| `quantity` | Số lượng |
| `price_snapshot` | Giá tại thời điểm thêm vào cart, nếu cần |
| `created_at` | Thời điểm thêm |
| `updated_at` | Thời điểm cập nhật |

## 3. Flow người dùng

### 3.1. Tạo cart

1. Người dùng mở trang quản lý cart.
2. Người dùng nhấn tạo cart mới.
3. Người dùng nhập tên cart.
4. Backend tạo cart với `status = ACTIVE`.
5. Cart mới xuất hiện trong danh sách cart của người dùng.

Nếu người dùng chưa có cart nào, backend có thể tự tạo một cart mặc định như "Gio hang cua toi".

### 3.2. Thêm sản phẩm vào cart

1. Người dùng bấm thêm sản phẩm vào giỏ.
2. Frontend hiển thị danh sách cart đang `ACTIVE`.
3. Người dùng chọn cart muốn thêm sản phẩm vào.
4. Backend kiểm tra cart có thuộc về user hiện tại không.
5. Backend kiểm tra sản phẩm/biến thể còn hợp lệ không.
6. Nếu item cùng `product_id` và `variant_id` đã tồn tại trong cart, backend tăng `quantity`.
7. Nếu chưa tồn tại, backend tạo cart item mới.

### 3.3. Xem danh sách cart

1. Người dùng mở danh sách cart.
2. Backend trả về các cart của người dùng.
3. Mỗi cart nên có thông tin tóm tắt:
   - Tên cart.
   - Số lượng item.
   - Tổng số lượng sản phẩm.
   - Tổng tiền tạm tính.
   - Trạng thái cart.

### 3.4. Xem chi tiết cart

1. Người dùng chọn một cart.
2. Backend kiểm tra cart thuộc về user hiện tại.
3. Backend trả về danh sách item trong cart.
4. Frontend hiển thị sản phẩm, số lượng, giá hiện tại, giá snapshot nếu có, và tổng tiền.

### 3.5. Cập nhật item trong cart

Người dùng có thể:

- Tăng/giảm số lượng.
- Xóa item khỏi cart.
- Chuyển item sang cart khác, nếu muốn hỗ trợ.

Rule đề xuất:

- `quantity` phải lớn hơn 0.
- Nếu `quantity = 0`, nên xử lý như xóa item.
- Không cho sửa item trong cart đã `CHECKED_OUT` hoặc `ARCHIVED`.

### 3.6. Checkout một cart

1. Người dùng mở cart muốn mua.
2. Người dùng nhấn thanh toán.
3. Backend kiểm tra cart thuộc về user hiện tại.
4. Backend kiểm tra cart đang `ACTIVE`.
5. Backend kiểm tra cart có ít nhất một item.
6. Backend kiểm tra lại:
   - Sản phẩm còn bán không.
   - Biến thể còn hợp lệ không.
   - Tồn kho có đủ không.
   - Giá hiện tại.
   - Voucher/discount nếu có.
7. Backend tạo order từ toàn bộ item trong cart đó.
8. Sau khi tạo order thành công, backend đổi cart sang `CHECKED_OUT`.
9. Cart đã checkout không còn được chỉnh sửa.

## 4. API đề xuất

### Cart APIs

```http
GET /carts
```

Lấy danh sách cart của user hiện tại.

```http
POST /carts
```

Tạo cart mới.

Body ví dụ:

```json
{
  "name": "Setup phong lam viec"
}
```

```http
GET /carts/{cartId}
```

Lấy chi tiết một cart.

```http
PATCH /carts/{cartId}
```

Cập nhật tên cart hoặc trạng thái nếu được phép.

```http
DELETE /carts/{cartId}
```

Xóa mềm cart, chuyển `status = ARCHIVED`.

### Cart Item APIs

```http
POST /carts/{cartId}/items
```

Thêm item vào cart.

Body ví dụ:

```json
{
  "productId": 1,
  "variantId": 10,
  "quantity": 2
}
```

```http
PATCH /carts/{cartId}/items/{itemId}
```

Cập nhật số lượng item.

Body ví dụ:

```json
{
  "quantity": 3
}
```

```http
DELETE /carts/{cartId}/items/{itemId}
```

Xóa item khỏi cart.

### Checkout API

```http
POST /carts/{cartId}/checkout
```

Tạo order từ cart được chọn.

Body có thể bổ sung sau:

```json
{
  "shippingAddressId": 5,
  "paymentMethod": "COD",
  "voucherCode": "SUMMER10"
}
```

## 5. Response mẫu

### Danh sách cart

```json
[
  {
    "id": 1,
    "name": "Setup phong lam viec",
    "status": "ACTIVE",
    "isDefault": true,
    "itemCount": 3,
    "totalQuantity": 5,
    "subtotal": 2500000
  },
  {
    "id": 2,
    "name": "Mua cho gia dinh",
    "status": "ACTIVE",
    "isDefault": false,
    "itemCount": 2,
    "totalQuantity": 2,
    "subtotal": 900000
  }
]
```

### Chi tiết cart

```json
{
  "id": 1,
  "name": "Setup phong lam viec",
  "status": "ACTIVE",
  "isDefault": true,
  "items": [
    {
      "id": 101,
      "productId": 1,
      "productName": "Ban lam viec",
      "variantId": 10,
      "variantName": "Mau den",
      "quantity": 1,
      "unitPrice": 1200000,
      "lineTotal": 1200000
    },
    {
      "id": 102,
      "productId": 2,
      "productName": "Den ban",
      "variantId": null,
      "variantName": null,
      "quantity": 2,
      "unitPrice": 250000,
      "lineTotal": 500000
    }
  ],
  "subtotal": 1700000
}
```

## 6. Rule nghiệp vụ cần chốt

1. Một user được có nhiều cart `ACTIVE`.
2. Một user nên có tối đa một cart `is_default = true`.
3. Một sản phẩm có thể nằm trong nhiều cart khác nhau.
4. Trong cùng một cart, cùng `product_id` và `variant_id` thì không tạo dòng trùng, chỉ tăng quantity.
5. Cart đã `CHECKED_OUT` không được sửa item.
6. Cart đã `ARCHIVED` không hiển thị ở flow mua hàng mặc định.
7. Checkout chỉ tạo order từ item trong cart được chọn.
8. Khi checkout phải kiểm tra lại giá và tồn kho, không tin tuyệt đối dữ liệu trong cart.
9. Nếu checkout thất bại, cart vẫn giữ `ACTIVE` để người dùng chỉnh lại.
10. Nếu checkout thành công, cart chuyển sang `CHECKED_OUT`.

## 7. Các điểm cần duyệt trước khi code

### 7.1. Sau checkout có giữ cart không?

Đề xuất: giữ cart và đổi sang `CHECKED_OUT`.

Lý do:

- Người dùng có thể xem lại list đã mua.
- Dễ debug khi có lỗi order.
- Có thể hỗ trợ "mua lại" bằng cách clone cart cũ.

### 7.2. Có cho clone cart không?

Đề xuất: chưa cần ở version đầu, nhưng schema nên không chặn khả năng này.

API có thể thêm sau:

```http
POST /carts/{cartId}/clone
```

### 7.3. Có giới hạn số cart không?

Đề xuất: có giới hạn mềm để tránh dữ liệu rác.

Ví dụ:

- Tối đa 20 cart `ACTIVE` mỗi user.
- Cart `CHECKED_OUT` và `ARCHIVED` không tính vào giới hạn này.

### 7.4. Xóa cart là xóa thật hay xóa mềm?

Đề xuất: xóa mềm bằng `status = ARCHIVED`.

Lý do:

- Tránh mất dữ liệu nhầm.
- Dễ audit nếu cart đã liên quan đến checkout/order.

### 7.5. Cart mặc định xử lý thế nào?

Đề xuất:

- Khi user chưa có cart, backend tạo cart mặc định.
- Khi thêm sản phẩm mà frontend không truyền `cartId`, backend thêm vào cart mặc định.
- User có thể đổi cart mặc định sau.

## 8. Hướng migration từ single cart sang multicart

Nếu hệ thống hiện tại đang có logic mỗi user chỉ có một cart:

1. Đảm bảo bảng cart có `id` riêng và `user_id`.
2. Bỏ giả định `user_id` là unique trong cart.
3. Thêm `name`, `status`, `is_default` vào cart.
4. Các query tìm cart phải chuyển từ `findByUserId` sang `findByUserIdAndCartId`.
5. Các API cart item phải nhận `cartId`.
6. Checkout phải nhận `cartId` và chỉ xử lý cart đó.

## 9. Phạm vi version đầu

Nên làm version đầu theo phạm vi sau:

- Tạo cart.
- Sửa tên cart.
- Xóa mềm cart.
- Xem danh sách cart.
- Xem chi tiết cart.
- Thêm item vào cart.
- Cập nhật quantity.
- Xóa item.
- Checkout một cart.

Chưa cần làm:

- Clone cart.
- Chuyển item giữa các cart.
- Chia sẻ cart cho user khác.
- Cart theo nhóm/gia đình.
- Lịch sử thay đổi item trong cart.


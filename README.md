# Backend

## 1. Cấu trúc thư mục

```
src/
├── main/
│   ├── java/
│   │   └── com/javaweb/
│   │       ├── controller/       # API Controllers (RESTful)
│   │       ├── dto/              # Data Transfer Objects (Request/Response)
│   │       ├── entity/           # JPA Entities (Database Tables)
│   │       ├── exception/        # Custom Exceptions
│   │       ├── repository/       # Spring Data JPA Repositories
│   │       ├── security/         # Security Configuration (JWT, Filters)
│   │       ├── service/          # Business Logic
│   │       │   ├── impl/         # Service Implementations
│   │       │   └── jwt/          # JWT Utilities
│   │       └── OnlineSportsMerchandiseSalesSystemApplication.java # Main Class
│   └── resources/
│       ├── application.properties  # Configuration (DB, Server Port)
│       └── static/               # Static Files (Optional)
└── test/
```

## 2. Các thành phần chính

### 2.1. Entities (src/main/java/com/javaweb/entity)

- **User.java**: Thông tin người dùng
- **Role.java**: Vai trò (ADMIN, USER, MANAGER)
- **Product.java**: Thông tin sản phẩm
- **Category.java**: Danh mục sản phẩm
- **Brand.java**: Thương hiệu
- **Order.java**: Đơn hàng
- **OrderDetail.java**: Chi tiết đơn hàng
- **Payment.java**: Thông tin thanh toán
- **Review.java**: Đánh giá sản phẩm
- **Image.java**: Hình ảnh sản phẩm
- **Cart.java**: Giỏ hàng
- **CartItem.java**: Sản phẩm trong giỏ hàng

### 2.2. DTOs (src/main/java/com/javaweb/dto)

- **UserDTO.java**: Trả về thông tin user
- **UserRequestDTO.java**: Nhận request tạo/update user
- **ProductDTO.java**: Trả về thông tin product
- **ProductRequestDTO.java**: Nhận request tạo/update product
- **AuthResponseDTO.java**: Trả về JWT token
- **CartDTO.java**: Trả về thông tin giỏ hàng
- **AddCartRequestDTO.java**: Nhận request thêm vào giỏ hàng

### 2.3. Controllers (src/main/java/com/javaweb/controller)

- **AuthController.java**: Đăng ký, đăng nhập
- **UserController.java**: Quản lý user
- **ProductController.java**: Quản lý product
- **CategoryController.java**: Quản lý category
- **BrandController.java**: Quản lý brand
- **OrderController.java**: Quản lý order
- **PaymentController.java**: Quản lý payment
- **ReviewController.java**: Quản lý review
- **ImageController.java**: Quản lý image
- **CartController.java**: Quản lý giỏ hàng

### 2.4. Services (src/main/java/com/javaweb/service)

- **UserService.java**: Logic nghiệp vụ user
- **ProductService.java**: Logic nghiệp vụ product
- **CategoryService.java**: Logic nghiệp vụ category
- **BrandService.java**: Logic nghiệp vụ brand
- **OrderService.java**: Logic nghiệp vụ order
- **PaymentService.java**: Logic nghiệp vụ payment
- **ReviewService.java**: Logic nghiệp vụ review
- **ImageService.java**: Logic nghiệp vụ image
- **CartService.java**: Logic nghiệp vụ giỏ hàng

### 2.5. Security (src/main/java/com/javaweb/security)

- **JwtAuthenticationFilter.java**: Filter xác thực JWT
- **JwtTokenProvider.java**: Tạo và xác thực JWT token
- **SecurityConfig.java**: Cấu hình Spring Security
- **CustomUserDetailsService.java**: Load user từ database

## 3. Cấu hình

### 3.1. application.properties

```properties
# Server port
server.port=8080

# Database configuration
spring.datasource.url=jdbc:mysql://localhost:3306/online_sports_store
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# JWT configuration
jwt.secret=your-secret-key
jwt.expiration=86400000
```

## 4. API Endpoints

### 4.1. Authentication

- `POST /api/auth/register`: Đăng ký tài khoản
- `POST /api/auth/login`: Đăng nhập

### 4.2. Users

- `GET /api/users`: Lấy tất cả user
- `GET /api/users/{id}`: Lấy user theo ID
- `POST /api/users`: Tạo user mới
- `PUT /api/users/{id}`: Cập nhật user
- `DELETE /api/users/{id}`: Xóa user

### 4.3. Products

- `GET /api/products`: Lấy tất cả product
- `GET /api/products/{id}`: Lấy product theo ID
- `POST /api/products`: Tạo product mới
- `PUT /api/products/{id}`: Cập nhật product
- `DELETE /api/products/{id}`: Xóa product

### 4.4. Categories

- `GET /api/categories`: Lấy tất cả category
- `GET /api/categories/{id}`: Lấy category theo ID
- `POST /api/categories`: Tạo category mới
- `PUT /api/categories/{id}`: Cập nhật category
- `DELETE /api/categories/{id}`: Xóa category

### 4.5. Brands

- `GET /api/brands`: Lấy tất cả brand
- `GET /api/brands/{id}`: Lấy brand theo ID
- `POST /api/brands`: Tạo brand mới
- `PUT /api/brands/{id}`: Cập nhật brand
- `DELETE /api/brands/{id}`: Xóa brand

### 4.6. Orders

- `GET /api/orders`: Lấy tất cả order
- `GET /api/orders/{id}`: Lấy order theo ID
- `POST /api/orders`: Tạo order mới
- `PUT /api/orders/{id}`: Cập nhật order
- `DELETE /api/orders/{id}`: Xóa order

### 4.7. Payments

- `GET /api/payments`: Lấy tất cả payment
- `GET /api/payments/{id}`: Lấy payment theo ID
- `POST /api/payments`: Tạo payment mới
- `PUT /api/payments/{id}`: Cập nhật payment
- `DELETE /api/payments/{id}`: Xóa payment

### 4.8. Reviews

- `GET /api/reviews`: Lấy tất cả review
- `GET /api/reviews/{id}`: Lấy review theo ID
- `POST /api/reviews`: Tạo review mới
- `PUT /api/reviews/{id}`: Cập nhật review
- `DELETE /api/reviews/{id}`: Xóa review

### 4.9. Images

- `GET /api/images`: Lấy tất cả image
- `GET /api/images/{id}`: Lấy image theo ID
- `POST /api/images`: Tạo image mới
- `PUT /api/images/{id}`: Cập nhật image
- `DELETE /api/images/{id}`: Xóa image

### 4.10. Carts

- `GET /api/carts`: Lấy thông tin giỏ hàng của user
- `POST /api/carts/items`: Thêm sản phẩm vào giỏ hàng
- `DELETE /api/carts/items/{itemId}`: Xóa sản phẩm khỏi giỏ hàng
- `DELETE /api/carts`: Xóa toàn bộ sản phẩm trong giỏ hàng

## 5. Cách chạy

1. **Cài đặt Java 17+**
2. **Cài đặt Maven**
3. **Cài đặt MySQL**
4. **Tạo database**

```sql
CREATE DATABASE online_sports_store;
```

5. **Chạy ứng dụng**

```bash
mvn spring-boot:run
```

Ứng dụng sẽ chạy tại `http://localhost:8080`

## 6. Cấu trúc Git

- **main**: Production code
- **develop**: Development branch
- **feature/***: Feature branches
- **hotfix/***: Hotfix branches

# 🛒 Online Sports Merchandise Sales System - Backend

Dự án Hệ thống Backend E-commerce bán đồ thể thao trực tuyến, được phát triển theo tiêu chuẩn doanh nghiệp (Layered Architecture).

## 🚀 Công nghệ sử dụng (Tech Stack)
- **Ngôn ngữ:** Java 21
- **Framework:** Spring Boot 3.4.1
- **Cơ sở dữ liệu (Database):** MySQL (Quản lý qua Spring Data JPA)
- **Bảo mật (Security):** Spring Security + JWT (JSON Web Token)
- **Công cụ hỗ trợ:** Lombok, Maven

## 📂 Cấu trúc thư mục (Architecture)
Cấu trúc chuẩn của dự án được chia làm các tầng rõ rệt giúp dễ dàng bảo trì và mở rộng:
- `├── config/`: Các file cấu hình chung của hệ thống.
- `├── controller/`: Tầng giao tiếp (API Gateway) nhận các Restful HTTP Request.
- `├── dto/`: Data Transfer Object - Quản lý cấu trúc dữ liệu gửi và nhận.
- `├── entity/`: Định nghĩa thông tin Bảng dưới MySQL.
- `├── exception/`: Bắt lỗi toàn cục (Global Exception Handler) xử lý chuẩn REST.
- `├── repository/`: Tầng truy vấn cơ sở dữ liệu với Spring Data JPA.
- `├── security/`: Chứa các bộ lọc bảo mật và cung cấp JWT Token.
- `└── service/`: (Interfaces & Impl) Nơi thực thi Core Business Logic của ứng dụng.

## 🔑 Tính năng chính cốt lõi
1. **Quản lý Sinh thái Người Dùng (`User`, `Role`)**
   - Đầy đủ luồng Thêm/Xoá/Sửa/Tìm kiếm.
   - Ứng dụng "Soft Delete" (Xóa mềm) để bảo tồn lịch sử mua hàng.
2. **Quản lý Thông tin Sản Phẩm (`Product`, `Category`, `Brand`)**
   - Xây dựng sơ đồ phân cấp Danh mục, Hãng và Biến thể Sản phẩm.
3. **Bảo mật và Phân Quyền (JWT Authentication)**
   - Hệ thống khóa chặn vạn năng dựa trên Token. (Hạn sử dụng: 7 ngày).
4. **Giỏ hàng (Cart)**
   - Quản lý giỏ hàng của người dùng, hỗ trợ thêm/xóa/xem sản phẩm trong giỏ hàng.

## 🛠️ Hướng dẫn cài đặt (Getting Started)
1. Hãy tìm mở file `src/main/resources/application.properties`.
2. Thay đổi tên **database**, **username**, và **password** phù hợp với cài đặt MySQL của máy tính cấu hình của bạn.
3. Chạy lệnh: `mvn clean install` trên Terminal để tải các thư viện.
4. Nhấn phím Mũi tên xanh (Run) ở file gốc để khởi động máy chủ. Hibernate sẽ tự lập các bảng MySQL giúp bạn!
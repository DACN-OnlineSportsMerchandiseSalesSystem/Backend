# 🛒 Online Sports Merchandise Sales System - Backend

Dự án Hệ thống Backend E-commerce bán đồ thể thao trực tuyến, được phát triển theo tiêu chuẩn hệ thống lớn, kiến trúc phân tầng chuyên nghiệp (**Layered Architecture**) phục vụ Đồ án Chuyên ngành (DACN). 

Hệ thống tích hợp các công nghệ hiện đại bậc nhất hiện nay như **Trí tuệ nhân tạo (AI RAG)**, **Chuyển đổi văn bản thành giọng nói (TTS)**, **Thanh toán điện tử MoMo**, và **Hệ thống cá nhân hóa người dùng cao cấp**.

---

## 🚀 Công nghệ sử dụng (Tech Stack)

*   **Core Backend:** Java 21 / 25 (Hỗ trợ tương thích hoàn hảo bằng Lombok `1.18.40`)
*   **Framework chính:** Spring Boot 3.4.1 (Spring Security, Spring Data JPA, Spring Web)
*   **Hệ thống AI & RAG:** LangChain4j (Tích hợp Gemini API)
*   **Bộ nhớ đệm & Bộ nhớ Chat:** Redis (Quản lý Session Chat Memory)
*   **Cơ sở dữ liệu (Database):** MySQL 8.x
*   **Cổng thanh toán:** MoMo E-Wallet API
*   **Giọng nói AI:** FPT Text-to-Speech (TTS) API
*   **Lưu trữ đám mây:** Cloudinary API (Upload ảnh sản phẩm và hóa đơn hoàn trả)
*   **Tài liệu hóa API:** OpenAPI 3 / Swagger UI
*   **Quản lý thư viện:** Maven 3.x

---

## 📂 Cấu trúc thư mục (Architecture)

Kiến trúc dự án được thiết kế phân tầng chuẩn doanh nghiệp (Layered Architecture):

```
src/main/java/com/javaweb/
├── config/           # Cấu hình hệ thống (AI, Cloudinary, Swagger, Security, MoMo)
├── controller/       # Tầng API RESTful (Nhận request và trả phản hồi JSON/SSE)
├── dto/              # Data Transfer Objects (Payloads gửi/nhận dữ liệu)
├── entity/           # JPA Entities (Ánh xạ các bảng cơ sở dữ liệu MySQL)
├── exception/        # Quản lý và xử lý ngoại lệ tập trung (Global Exception Handler)
├── repository/       # Tầng truy vấn cơ sở dữ liệu (Spring Data JPA)
├── security/         # Bộ lọc bảo mật JWT & Phân quyền Spring Security
└── service/          # Tầng xử lý logic nghiệp vụ (Core Business Logic)
    └── impl/         # Lớp triển khai chi tiết của các Service
```

---

## 🔑 Tính năng cao cấp nổi bật (Advanced Features)

### 1. 🤖 Trí tuệ nhân tạo AI RAG Chatbot (Server-Sent Events)
*   **SSE Streaming:** Phản hồi tin nhắn từ AI dạng gõ chữ thời gian thực (real-time stream) mang lại trải nghiệm mượt mà bằng **SseEmitter**.
*   **Hybrid RAG Search:** Kết hợp tìm kiếm ngữ nghĩa Vector (Vector Search) dựa trên nhúng từ (Embeddings) của LangChain4j và tìm kiếm từ khóa truyền thống để trả về kết quả sản phẩm chính xác nhất.
*   **Function Calling (Generative UI):** Chatbot tự động phát hiện ý định mua sắm của khách hàng để gọi hàm lấy danh sách sản phẩm và hiển thị thẻ sản phẩm (Product Cards) trực tiếp lên khung chat.
*   **Redis Chat Memory:** Lưu trữ ngữ cảnh hội thoại theo từng Session ID giúp chatbot ghi nhớ nội dung trò chuyện trước đó của khách hàng.

### 2. 🗣️ Chuyển đổi Văn bản thành Giọng nói (FPT TTS)
*   Tích hợp trực tiếp với API của **FPT Text-to-Speech** để chuyển câu trả lời văn bản của chatbot thành file âm thanh dạng giọng đọc tự nhiên, tăng tính tương tác thông minh cho hệ thống.

### 3. 💳 Cổng thanh toán Điện tử MoMo
*   Tích hợp cổng thanh toán trực tuyến **MoMo**. Hỗ trợ tạo link checkout thanh toán bảo mật và xử lý phản hồi bất đồng bộ từ MoMo (**Instant Payment Notification - IPN**) để cập nhật trạng thái hóa đơn tự động.

### 4. 🥇 Hệ thống Thành viên & Cá nhân hóa Người dùng
*   **Membership Ranks:** Tự động thăng hạng khách hàng dựa trên lịch sử mua sắm (`NEW`, `BRONZE`, `SILVER`, `GOLD`, `DIAMOND`) với các ưu đãi và voucher riêng biệt.
*   **User Interest Onboarding:** Liên kết Nhiều-Nhiều (Many-to-Many) giữa tài khoản người dùng và danh mục sản phẩm nhằm nắm bắt sở thích thể thao khi đăng ký tài khoản, phục vụ gợi ý sản phẩm cá nhân hóa.

### 5. 📰 Quản lý Tin tức & CDC Auto Vectorization
*   Hỗ trợ đầy đủ bộ API CRUD cho mục **Blog / Tin tức**.
*   Khi có bất kỳ thay đổi nào (thêm mới hoặc cập nhật bài viết), hệ thống tự động đặt trạng thái `is_vectorized = false`. Tiến trình chạy ngầm (**DataIngestionService**) sau mỗi 5 phút sẽ tự động quét, mã hóa nhúng và đồng bộ bài viết mới vào **Vector Database** giúp AI cập nhật kiến thức ngay lập tức.

### 6. 📝 Tài liệu hóa API Swagger UI (100% Tiếng Anh)
*   Được viết tài liệu chi tiết bằng tiếng Anh học thuật chuyên nghiệp cho toàn bộ **14 Controllers** và các DTOs.
*   Mô tả rõ ràng tham số, kiểu dữ liệu, các mã lỗi trả về (`200`, `201`, `400`, `403`, `404`) phục vụ hội đồng chấm đồ án.

---

## 🛠️ Hướng dẫn cài đặt & Khởi chạy (Getting Started)

### 1. Chuẩn bị Cơ sở dữ liệu MySQL
1. Khởi động MySQL Server trên máy của bạn.
2. Tạo mới một cơ sở dữ liệu trống:
   ```sql
   CREATE DATABASE backend_dacn;
   ```
3. Import schema từ file **[backend.sql](file:///c:/Users/admin/Desktop/Study/252/DACN/database/backend.sql)**.
4. Nạp dữ liệu mẫu ban đầu từ file **[data.sql](file:///c:/Users/admin/Desktop/Study/252/DACN/database/data.sql)**.

### 2. Cấu hình ứng dụng
Mở file `src/main/resources/application.properties` để điều chỉnh các tham số kết nối:
```properties
# MySQL Connection
spring.datasource.url=jdbc:mysql://localhost:3306/backend_dacn?useSSL=false&serverTimezone=UTC
spring.datasource.username=TÊN_USER_MYSQL_CỦA_BẠN
spring.datasource.password=MẬT_KHẨU_MYSQL_CỦA_BẠN

# API Keys & Third-party integrations
gemini.api.key=YOUR_GEMINI_API_KEY
cloudinary.cloud-name=YOUR_CLOUDINARY_NAME
cloudinary.api-key=YOUR_CLOUDINARY_KEY
cloudinary.api-secret=YOUR_CLOUDINARY_SECRET
fpt.tts.api-key=YOUR_FPT_TTS_API_KEY
```

### 3. Biên dịch và Chạy ứng dụng
Mở Terminal tại thư mục dự án và thực hiện các lệnh sau:

*   **Tải thư viện và Biên dịch:**
    ```bash
    mvn clean compile
    ```
*   **Khởi chạy Server:**
    ```bash
    mvn spring-boot:run
    ```
mvn clean spring-boot:run
    ```
Ứng dụng sẽ được khởi chạy tại cổng mặc định: **`http://localhost:8080`**

---

## 🔍 Xem và thử nghiệm API (Swagger UI)

Sau khi khởi chạy ứng dụng thành công, bồ truy cập vào đường dẫn sau trên trình duyệt để kiểm tra và test thử tất cả các đầu API trực quan:
👉 **`http://localhost:8080/swagger-ui/index.html`**

Tài liệu hướng dẫn trực quan đã được tích hợp đầy đủ, hỗ trợ gọi trực tiếp qua giao diện web!
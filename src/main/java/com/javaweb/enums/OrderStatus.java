package com.javaweb.enums;

public enum OrderStatus {
    PENDING, // Chờ xử lý
    CONFIRMED, // Đã xác nhận
    PAID, // Đã thanh toán
    SHIPPING, // Đang giao hàng
    DELIVERED, // Đã giao hàng
    COMPLETED, // Hoàn thành
    CANCELED, // Đã hủy
    REFUNDED, // Đã hoàn tiền
    RETURN_REQUESTED, // Yêu cầu đổi trả
    RETURNED // Đã hoàn hàng
}

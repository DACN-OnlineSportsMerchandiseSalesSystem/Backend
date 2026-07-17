package com.javaweb.enums;

public enum CartStatus {
    //rỗng  -> chọn hàng -> thanh toán -> checkout -> xuất hoá đơn
    //duy trì nhiều giỏ hàng
    //lưu tạm giỏ hàng
    // khách hàng
    //người bán hàng : quà tặng kèm (giỏ hàng được xây dựng sẵn -> không thể chỉnh sửa)
    
    ACTIVE,//ok
    CHECKED_OUT,//đã thanh toán
    ARCHIVED

    //thêm bên bán hàng 
    //more than 1 active cart
    //lược đồ hoạt động or tuần tự (giỏ hàng)

    //semanticSearch -- vectorSearch
    //nghiên cứu RAG
}

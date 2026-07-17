package com.javaweb.scheduler;

import com.javaweb.entity.Orders;
import com.javaweb.enums.OrderStatus;
import com.javaweb.repository.OrderRepository;
import com.javaweb.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderTimeoutScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutScheduler.class);
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    /**
     * Chạy định kỳ mỗi 1 phút (60,000 ms) để quét và hủy các đơn hàng PENDING đã quá 15 phút.
     */
    @Scheduled(fixedDelay = 60000)
    public void cancelExpiredPendingOrders() {
        log.info(">>> [Order Scheduler] Đang quét các đơn hàng quá hạn thanh toán...");
        
        // Ngưỡng thời gian: 15 phút trước
        Date timeoutThreshold = new Date(System.currentTimeMillis() - 15 * 60 * 1000);

        List<Orders> expiredOrders = orderRepository.findByStatusAndCreateAtBefore(OrderStatus.PENDING, timeoutThreshold);

        if (!expiredOrders.isEmpty()) {
            log.info(">>> [Order Scheduler] Phát hiện {} đơn hàng PENDING đã quá hạn 15 phút.", expiredOrders.size());
            for (Orders order : expiredOrders) {
                try {
                    orderService.updateOrderStatus(order.getId(), OrderStatus.CANCELED);
                    log.info(">>> [Order Scheduler] Đã tự động HỦY đơn hàng ID={} (Tồn kho và voucher đã được khôi phục thành công).", order.getId());
                } catch (Exception e) {
                    log.error("!!! [Order Scheduler] Gặp lỗi khi hủy đơn hàng ID={}: {}", order.getId(), e.getMessage());
                }
            }
        } else {
            log.info(">>> [Order Scheduler] Không phát hiện đơn hàng quá hạn nào.");
        }
    }
}

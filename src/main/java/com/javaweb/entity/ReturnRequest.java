package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import com.javaweb.enums.ReturnStatus;

@Entity
@Table(name = "return_requests")
@Getter
@Setter
public class ReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReturnStatus status;

    @Column(name = "refund_amount")
    private BigDecimal refundAmount;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "created_at")
    @CreationTimestamp
    private Date createdAt;

    // --- CÁC MỐI QUAN HỆ ---

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Orders order;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "returnRequest")
    private Set<ReturnItem> returnItems = new HashSet<>();

    public void addReturnItem(ReturnItem item) {
        if (item != null) {
            if (this.returnItems == null) {
                this.returnItems = new HashSet<>();
            }
            this.returnItems.add(item);
            item.setReturnRequest(this);
        }
    }
}

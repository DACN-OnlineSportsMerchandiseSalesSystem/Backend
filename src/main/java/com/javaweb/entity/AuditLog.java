package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "actor_email", nullable = false)
    private String actorEmail;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "target_type")
    private String targetType;

    @Column(name = "target_id")
    private String targetId;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "created_at")
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
}

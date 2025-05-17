package com.fudan.xiaozhong_dianping.invitation.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "invitation_record")
public class InvitationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long inviterId;

    @Column(nullable = false)
    private Long inviteeId;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private LocalDateTime createTime= LocalDateTime.now();

    @Column(nullable = false)
    private BigDecimal orderAmount;

    @Column(nullable = false)
    private LocalDateTime orderTime;

    @Column(nullable = false)
    private Boolean isValid=true;
}

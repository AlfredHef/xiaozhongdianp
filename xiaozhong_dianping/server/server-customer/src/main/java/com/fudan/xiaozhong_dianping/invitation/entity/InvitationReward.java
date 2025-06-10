package com.fudan.xiaozhong_dianping.invitation.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "invitation_reward")
public class InvitationReward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;  // 获得奖励的用户ID

    @Column(nullable = false)
    private Long couponId;  // 关联优惠券ID

    @Column(nullable = false)
    private Integer invitationCount;  // 触发奖励的邀请数量

    @Column(nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();  // 创建时间
}
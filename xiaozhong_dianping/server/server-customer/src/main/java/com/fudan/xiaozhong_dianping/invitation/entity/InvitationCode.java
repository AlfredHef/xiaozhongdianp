package com.fudan.xiaozhong_dianping.invitation.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "invitation_code")
public class InvitationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true,length = 8)
    private String code;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime createTime=LocalDateTime.now();

}

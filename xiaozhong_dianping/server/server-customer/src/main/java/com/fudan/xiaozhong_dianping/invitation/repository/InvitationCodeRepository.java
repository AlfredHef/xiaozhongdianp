package com.fudan.xiaozhong_dianping.invitation.repository;

import com.fudan.xiaozhong_dianping.invitation.entity.InvitationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvitationCodeRepository extends JpaRepository<InvitationCode, Long> {
    //  根据用户id查询邀请码
    InvitationCode findByUserId(Long userId);

    //  根据邀请码查询
    InvitationCode findByCode(String code);
}


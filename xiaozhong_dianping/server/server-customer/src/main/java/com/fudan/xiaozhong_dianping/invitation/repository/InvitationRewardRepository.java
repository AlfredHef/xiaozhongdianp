package com.fudan.xiaozhong_dianping.invitation.repository;

import com.fudan.xiaozhong_dianping.invitation.entity.InvitationReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InvitationRewardRepository extends JpaRepository<InvitationReward, Long> {
    // 查询用户获得的所有邀请奖励
    List<InvitationReward> findByUserIdOrderByCreateTimeDesc(Long userId);

    // 查询用户最近一次获得邀请奖励时的邀请数量
    InvitationReward findFirstByUserIdOrderByInvitationCountDesc(Long userId);
}
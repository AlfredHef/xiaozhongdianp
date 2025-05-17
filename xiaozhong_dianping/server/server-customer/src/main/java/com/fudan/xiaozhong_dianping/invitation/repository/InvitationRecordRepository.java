package com.fudan.xiaozhong_dianping.invitation.repository;
import org.springframework.data.jpa.repository.Query;
import  org.springframework.stereotype.Repository;
import  org.springframework.data.jpa.repository.JpaRepository;
import com.fudan.xiaozhong_dianping.invitation.entity.InvitationRecord;

import java.util.List;

@Repository
public interface InvitationRecordRepository extends JpaRepository<InvitationRecord, Long>{

    //查询邀请人的所有邀请记录
    List<InvitationRecord> findByInviterIdOrderByCreateTimeDesc(Long inviterId);

    //检查特定用户是否被邀请过
    boolean existsByInviteeId(Long inviteeId);

    //统计邀请人有效邀请的数量
    @Query("SELECT COUNT(r) FROM InvitationRecord r WHERE r.inviterId = ?1 AND r.isValid = true")
    int countValidInvitationByInviterId(Long inviterId);

    //查询指定用户未奖励的邀请记录数
    // 查询指定用户未奖励的邀请记录数
    @Query("SELECT COUNT(r) FROM InvitationRecord r WHERE r.inviterId = ?1 AND r.isValid = true AND " +
            "NOT EXISTS (SELECT 1 FROM InvitationReward rw WHERE rw.userId = r.inviterId AND rw.invitationCount <= " +
            "(SELECT COUNT(r2) FROM InvitationRecord r2 WHERE r2.inviterId = r.inviterId AND r2.isValid = true AND r2.createTime <= r.createTime))")
    int countUnrewardedInvitationsByInviterId(Long inviterId);
}

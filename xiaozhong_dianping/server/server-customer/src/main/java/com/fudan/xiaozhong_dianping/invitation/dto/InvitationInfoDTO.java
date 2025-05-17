package com.fudan.xiaozhong_dianping.invitation.dto;

import com.fudan.xiaozhong_dianping.invitation.entity.InvitationRecord;
import com.fudan.xiaozhong_dianping.invitation.entity.InvitationReward;
import lombok.Data;

import java.util.List;

/**
 * 邀请信息数据传输对象
 */
@Data
public class InvitationInfoDTO {
    private String invitationCode;
    private List<InvitationRecord> invitationRecords;
    private List<InvitationReward> invitationRewards;
} 
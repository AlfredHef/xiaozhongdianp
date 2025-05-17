package com.fudan.xiaozhong_dianping.invitation.controller;

import com.fudan.xiaozhong_dianping.invitation.dto.InvitationInfoDTO;
import com.fudan.xiaozhong_dianping.invitation.entity.InvitationCode;
import com.fudan.xiaozhong_dianping.invitation.entity.InvitationRecord;
import com.fudan.xiaozhong_dianping.invitation.entity.InvitationReward;
import com.fudan.xiaozhong_dianping.invitation.service.InvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invitation")
public class InvitationController {

    @Autowired
    private InvitationService invitationService;

    /**
     * 获取用户邀请信息（包含邀请码、邀请记录和奖励记录）
     * @param userId 用户ID
     * @return 用户邀请信息
     */
    @GetMapping("/info")
    public ResponseEntity<InvitationInfoDTO> getInvitationInfo(@RequestHeader("userId") Long userId) {
        // 获取用户邀请码
        InvitationCode invitationCode = invitationService.getUserInvitationCode(userId);

        // 获取邀请记录
        List<InvitationRecord> invitationRecords = invitationService.getUserInvitationRecords(userId);

        // 获取奖励记录
        List<InvitationReward> invitationRewards = invitationService.getUserInvitationRewards(userId);

        // 组装返回数据
        InvitationInfoDTO response = new InvitationInfoDTO();
        response.setInvitationCode(invitationCode.getCode());
        response.setInvitationRecords(invitationRecords);
        response.setInvitationRewards(invitationRewards);

        return ResponseEntity.ok(response);
    }
}
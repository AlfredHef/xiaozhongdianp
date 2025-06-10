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

    /**
     * 调试接口：查看指定用户的邀请记录详情
     * @param userId 邀请人的用户ID
     * @return 成功消息
     */
    @GetMapping("/debug/records/{userId}")
    public ResponseEntity<Map<String, String>> debugInvitationRecords(@PathVariable Long userId) {
        System.out.println("=== 调试接口被调用：查看用户" + userId + "的邀请记录 ===");
        
        // 调用调试方法（需要先转换为实现类）
        if (invitationService instanceof com.fudan.xiaozhong_dianping.invitation.service.impl.InvitationServiceImpl) {
            com.fudan.xiaozhong_dianping.invitation.service.impl.InvitationServiceImpl impl = 
                (com.fudan.xiaozhong_dianping.invitation.service.impl.InvitationServiceImpl) invitationService;
            impl.debugPrintInvitationRecords(userId);
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "调试信息已输出到控制台，请查看服务器日志");
        response.put("userId", userId.toString());
        return ResponseEntity.ok(response);
    }

    /**
     * 调试接口：检查指定用户的邀请状态
     * @param userId 被邀请人的用户ID
     * @return 成功消息
     */
    @GetMapping("/debug/status/{userId}")
    public ResponseEntity<Map<String, String>> debugUserInvitationStatus(@PathVariable Long userId) {
        System.out.println("=== 调试接口被调用：检查用户" + userId + "的邀请状态 ===");
        
        // 调用调试方法
        if (invitationService instanceof com.fudan.xiaozhong_dianping.invitation.service.impl.InvitationServiceImpl) {
            com.fudan.xiaozhong_dianping.invitation.service.impl.InvitationServiceImpl impl = 
                (com.fudan.xiaozhong_dianping.invitation.service.impl.InvitationServiceImpl) invitationService;
            impl.debugCheckUserInvitationStatus(userId);
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "调试信息已输出到控制台，请查看服务器日志");
        response.put("userId", userId.toString());
        return ResponseEntity.ok(response);
    }
}
package com.graddu.oa.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.graddu.oa.module.system.dto.NotifyConfigDTO;
import com.graddu.oa.module.system.entity.SysNotifyConfig;
import com.graddu.oa.module.system.mapper.SysNotifyConfigMapper;
import com.graddu.oa.module.system.service.SysNotifyConfigService;
import com.graddu.oa.module.system.vo.NotifyConfigVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 提醒机器人配置及消息派发实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysNotifyConfigServiceImpl extends ServiceImpl<SysNotifyConfigMapper, SysNotifyConfig> implements SysNotifyConfigService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<NotifyConfigVO> listConfigs() {
        List<SysNotifyConfig> list = this.list(new LambdaQueryWrapper<SysNotifyConfig>()
                .orderByDesc(SysNotifyConfig::getId));
        return list.stream().map(this::convertVO).collect(Collectors.toList());
    }

    @Override
    public Long saveConfig(NotifyConfigDTO dto) {
        SysNotifyConfig config = new SysNotifyConfig();
        BeanUtils.copyProperties(dto, config);
        if (config.getStatus() == null) {
            config.setStatus(1);
        }
        if (config.getNotifyEvents() == null || config.getNotifyEvents().isBlank()) {
            config.setNotifyEvents("TASK_ASSIGNED,PROCESS_FINISHED,NOTICE_PUBLISHED");
        }
        this.saveOrUpdate(config);
        return config.getId();
    }

    @Override
    public boolean deleteConfig(Long id) {
        return this.removeById(id);
    }

    @Override
    public String sendTestMessage(Long id) {
        SysNotifyConfig config = this.getById(id);
        if (config == null) {
            throw new IllegalArgumentException("未找到指定的提醒机器人配置");
        }
        String timeStr = LocalDateTime.now().format(dtf);
        String title = "🔔 协同办公平台 · 机器人联通性测试";
        String content = "### 🔔 协同办公平台 · 联通性测试\n" +
                "> **测试结果**：<font color=\"info\">连通正常</font>\n" +
                "> **机器人名称**：" + config.getName() + "\n" +
                "> **发送平台**：" + config.getPlatform() + "\n" +
                "> **发送时间**：" + timeStr + "\n\n" +
                "当业务流程到达新待办或公文发布时，将以此卡片样式实时推送提醒。";
        return dispatchWebhook(config, title, content);
    }

    @Async
    @Override
    public void notifyTaskAssigned(String title, String procKey, String applicantName, String nodeName, String assigneeUsername, String assigneeName) {
        List<SysNotifyConfig> configs = getActiveConfigsWithEvent("TASK_ASSIGNED");
        if (configs.isEmpty()) {
            return;
        }
        String timeStr = LocalDateTime.now().format(dtf);
        String subject = "🔔 待办审批提醒：" + title;
        String content = "### 🔔 协同办公平台 · 待办审批提醒\n" +
                "> **单据名称**：<font color=\"info\">" + title + "</font>\n" +
                "> **流程类型**：" + procKey + "\n" +
                "> **申请人员**：" + applicantName + "\n" +
                "> **当前节点**：<font color=\"warning\">" + nodeName + "</font>\n" +
                "> **待办指派**：" + (assigneeName != null ? assigneeName : assigneeUsername) + "\n" +
                "> **到达时间**：" + timeStr + "\n\n" +
                "请尽快登录移动端 H5 或 PC 工作台完成审批办理。";

        for (SysNotifyConfig config : configs) {
            try {
                dispatchWebhook(config, subject, content);
            } catch (Exception e) {
                log.warn("发送待办审批机器人提醒失败: config={}, err={}", config.getName(), e.getMessage());
            }
        }
    }

    @Async
    @Override
    public void notifyProcessFinished(String title, String procKey, String applicantName, boolean approved) {
        List<SysNotifyConfig> configs = getActiveConfigsWithEvent("PROCESS_FINISHED");
        if (configs.isEmpty()) {
            return;
        }
        String timeStr = LocalDateTime.now().format(dtf);
        String statusText = approved ? "【审批通过·办结】" : "【审批驳回·未通过】";
        String color = approved ? "info" : "warning";
        String subject = "📋 流程办结通知：" + title + " " + statusText;
        String content = "### 📋 协同办公平台 · 审批办结通知\n" +
                "> **单据名称**：" + title + "\n" +
                "> **流程类型**：" + procKey + "\n" +
                "> **申请人员**：" + applicantName + "\n" +
                "> **最终结果**：<font color=\"" + color + "\">" + statusText + "</font>\n" +
                "> **办结时间**：" + timeStr + "\n\n" +
                "单据已形成审计归档与套打凭证，请知悉。";

        for (SysNotifyConfig config : configs) {
            try {
                dispatchWebhook(config, subject, content);
            } catch (Exception e) {
                log.warn("发送办结机器人提醒失败: config={}, err={}", config.getName(), e.getMessage());
            }
        }
    }

    @Async
    @Override
    public void notifyNoticePublished(String title, String category, String publisherName) {
        List<SysNotifyConfig> configs = getActiveConfigsWithEvent("NOTICE_PUBLISHED");
        if (configs.isEmpty()) {
            return;
        }
        String timeStr = LocalDateTime.now().format(dtf);
        String subject = "📢 公文通告发布：" + title;
        String content = "### 📢 集团公文通告发布提醒\n" +
                "> **通告标题**：<font color=\"info\">" + title + "</font>\n" +
                "> **公文类别**：" + category + "\n" +
                "> **发布单位**：" + publisherName + "\n" +
                "> **发布时间**：" + timeStr + "\n\n" +
                "已全员同步发布，请全员及时登录协同办公平台查阅并签收。";

        for (SysNotifyConfig config : configs) {
            try {
                dispatchWebhook(config, subject, content);
            } catch (Exception e) {
                log.warn("发送通告机器人提醒失败: config={}, err={}", config.getName(), e.getMessage());
            }
        }
    }

    private List<SysNotifyConfig> getActiveConfigsWithEvent(String event) {
        return this.list(new LambdaQueryWrapper<SysNotifyConfig>()
                .eq(SysNotifyConfig::getStatus, 1)
                .like(SysNotifyConfig::getNotifyEvents, event));
    }

    private String dispatchWebhook(SysNotifyConfig config, String title, String markdownContent) {
        try {
            String targetUrl = config.getWebhookUrl().trim();
            String platform = config.getPlatform() != null ? config.getPlatform().toUpperCase() : "WECHAT_WORK";

            // 钉钉机器人安全加签处理
            if ("DINGTALK".equals(platform) && config.getSecretKey() != null && !config.getSecretKey().isBlank()) {
                long timestamp = System.currentTimeMillis();
                String stringToSign = timestamp + "\n" + config.getSecretKey().trim();
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(config.getSecretKey().trim().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
                byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
                String sign = URLEncoder.encode(Base64.getEncoder().encodeToString(signData), StandardCharsets.UTF_8);
                targetUrl = targetUrl + (targetUrl.contains("?") ? "&" : "?") + "timestamp=" + timestamp + "&sign=" + sign;
            }

            String jsonPayload = buildPayload(platform, title, markdownContent);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(6))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            log.info("机器人提醒推送返回: platform={}, code={}, body={}", platform, response.statusCode(), response.body());
            if (response.statusCode() == 200) {
                return "推送成功: " + response.body();
            } else {
                return "推送响应异常 (HTTP " + response.statusCode() + "): " + response.body();
            }
        } catch (Exception e) {
            log.error("执行机器人推送发生异常: {}", e.getMessage(), e);
            return "推送失败: " + e.getMessage();
        }
    }

    private String buildPayload(String platform, String title, String content) {
        String escapedContent = escapeJson(content);
        String escapedTitle = escapeJson(title);

        if ("DINGTALK".equals(platform)) {
            return "{" +
                    "\"msgtype\": \"markdown\"," +
                    "\"markdown\": {" +
                    "\"title\": \"" + escapedTitle + "\"," +
                    "\"text\": \"" + escapedContent + "\"" +
                    "}" +
                    "}";
        } else if ("FEISHU".equals(platform)) {
            return "{" +
                    "\"msg_type\": \"interactive\"," +
                    "\"card\": {" +
                    "\"header\": {\"title\": {\"tag\": \"plain_text\", \"content\": \"" + escapedTitle + "\"}, \"template\": \"blue\"}," +
                    "\"elements\": [{\"tag\": \"div\", \"text\": {\"tag\": \"lark_md\", \"content\": \"" + escapedContent + "\"}}]" +
                    "}" +
                    "}";
        } else {
            // 默认企业微信 (WECHAT_WORK)
            return "{" +
                    "\"msgtype\": \"markdown\"," +
                    "\"markdown\": {" +
                    "\"content\": \"" + escapedContent + "\"" +
                    "}" +
                    "}";
        }
    }

    private String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private NotifyConfigVO convertVO(SysNotifyConfig entity) {
        NotifyConfigVO vo = new NotifyConfigVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}

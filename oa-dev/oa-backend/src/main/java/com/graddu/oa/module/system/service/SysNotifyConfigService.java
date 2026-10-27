package com.graddu.oa.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.graddu.oa.module.system.dto.NotifyConfigDTO;
import com.graddu.oa.module.system.entity.SysNotifyConfig;
import com.graddu.oa.module.system.vo.NotifyConfigVO;

import java.util.List;

/**
 * 提醒机器人配置业务接口
 */
public interface SysNotifyConfigService extends IService<SysNotifyConfig> {

    List<NotifyConfigVO> listConfigs();

    Long saveConfig(NotifyConfigDTO dto);

    boolean deleteConfig(Long id);

    String sendTestMessage(Long id);

    void notifyTaskAssigned(String title, String procKey, String applicantName, String nodeName, String assigneeUsername, String assigneeName);

    void notifyProcessFinished(String title, String procKey, String applicantName, boolean approved);

    void notifyNoticePublished(String title, String category, String publisherName);
}

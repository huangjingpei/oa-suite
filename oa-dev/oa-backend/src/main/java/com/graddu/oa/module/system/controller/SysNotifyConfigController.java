package com.graddu.oa.module.system.controller;

import com.graddu.oa.common.api.R;
import com.graddu.oa.module.system.dto.NotifyConfigDTO;
import com.graddu.oa.module.system.service.SysNotifyConfigService;
import com.graddu.oa.module.system.vo.NotifyConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 企微/钉钉/飞书 提醒机器人配置接口
 */
@RestController
@RequestMapping("/sys/notify-config")
@RequiredArgsConstructor
public class SysNotifyConfigController {

    private final SysNotifyConfigService notifyConfigService;

    @GetMapping("/list")
    public R<List<NotifyConfigVO>> listConfigs() {
        return R.ok(notifyConfigService.listConfigs());
    }

    @PostMapping("/save")
    public R<Long> saveConfig(@RequestBody NotifyConfigDTO dto) {
        return R.ok(notifyConfigService.saveConfig(dto));
    }

    @DeleteMapping("/{id}")
    public R<Boolean> deleteConfig(@PathVariable Long id) {
        return R.ok(notifyConfigService.deleteConfig(id));
    }

    @PostMapping("/test/{id}")
    public R<String> sendTestMessage(@PathVariable Long id) {
        return R.ok(notifyConfigService.sendTestMessage(id));
    }
}

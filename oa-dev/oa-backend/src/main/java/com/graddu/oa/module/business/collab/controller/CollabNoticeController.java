package com.graddu.oa.module.business.collab.controller;

import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.common.api.R;
import com.graddu.oa.module.business.collab.dto.NoticeSaveDTO;
import com.graddu.oa.module.business.collab.service.CollabNoticeService;
import com.graddu.oa.module.business.collab.vo.NoticeReadStatsVO;
import com.graddu.oa.module.business.collab.vo.NoticeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "协同通告与公文大厅接口", description = "通告列表、已读/未读状态追踪与全员催办提醒")
@RestController
@RequestMapping("/collab/notice")
@RequiredArgsConstructor
public class CollabNoticeController {

    private final CollabNoticeService noticeService;

    @GetMapping("/page")
    @Operation(summary = "分页查询通告公告列表")
    public R<PageResult<NoticeVO>> pageNotices(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "noticeType", required = false) String noticeType) {
        return R.ok(noticeService.pageNotices(pageNum, pageSize, keyword, noticeType));
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "查阅通告详情并自动标记已读")
    public R<NoticeVO> getDetail(@PathVariable("id") Long id) {
        return R.ok(noticeService.getNoticeDetailAndMarkRead(id));
    }

    @GetMapping("/read-stats/{id}")
    @Operation(summary = "查询指定公文/通告的全员已读未读统计矩阵")
    public R<NoticeReadStatsVO> getReadStats(@PathVariable("id") Long id) {
        return R.ok(noticeService.getNoticeReadStats(id));
    }

    @PostMapping
    @Operation(summary = "新增或编辑通告")
    public R<Boolean> saveNotice(@Valid @RequestBody NoticeSaveDTO dto) {
        noticeService.saveNotice(dto);
        return R.ok("通告保存成功", true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除通告")
    public R<Boolean> deleteNotice(@PathVariable("id") Long id) {
        noticeService.deleteNotice(id);
        return R.ok("通告已删除", true);
    }

    @PostMapping("/remind/{id}")
    @Operation(summary = "一键催办未读人员")
    public R<Boolean> remindUnread(@PathVariable("id") Long id) {
        noticeService.remindUnreadUsers(id);
        return R.ok("已成功向未读人员发送催办阅读提醒", true);
    }
}

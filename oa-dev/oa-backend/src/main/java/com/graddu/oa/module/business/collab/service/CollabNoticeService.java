package com.graddu.oa.module.business.collab.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.module.business.collab.dto.NoticeSaveDTO;
import com.graddu.oa.module.business.collab.entity.CollabNotice;
import com.graddu.oa.module.business.collab.vo.NoticeReadStatsVO;
import com.graddu.oa.module.business.collab.vo.NoticeVO;

/**
 * 协同通告服务接口
 */
public interface CollabNoticeService extends IService<CollabNotice> {

    /**
     * 分页查询企业通告列表
     */
    PageResult<NoticeVO> pageNotices(Integer pageNum, Integer pageSize, String keyword, String noticeType);

    /**
     * 查阅通告详情并自动标记当前登录人已读
     */
    NoticeVO getNoticeDetailAndMarkRead(Long id);

    /**
     * 查询指定通告的全员已读/未读状态统计矩阵
     */
    NoticeReadStatsVO getNoticeReadStats(Long id);

    /**
     * 新增或更新通告
     */
    void saveNotice(NoticeSaveDTO dto);

    /**
     * 删除通告
     */
    void deleteNotice(Long id);

    /**
     * 一键催办未读人员
     */
    void remindUnreadUsers(Long id);
}

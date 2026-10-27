package com.graddu.oa.module.business.collab.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.common.exception.BusinessException;
import com.graddu.oa.module.business.collab.dto.NoticeSaveDTO;
import com.graddu.oa.module.business.collab.entity.CollabNotice;
import com.graddu.oa.module.business.collab.entity.CollabNoticeRead;
import com.graddu.oa.module.business.collab.mapper.CollabNoticeMapper;
import com.graddu.oa.module.business.collab.mapper.CollabNoticeReadMapper;
import com.graddu.oa.module.business.collab.service.CollabNoticeService;
import com.graddu.oa.module.business.collab.vo.NoticeReadStatsVO;
import com.graddu.oa.module.business.collab.vo.NoticeVO;
import com.graddu.oa.module.system.entity.SysDept;
import com.graddu.oa.module.system.entity.SysUser;
import com.graddu.oa.module.system.mapper.SysDeptMapper;
import com.graddu.oa.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollabNoticeServiceImpl extends ServiceImpl<CollabNoticeMapper, CollabNotice> implements CollabNoticeService {

    private final CollabNoticeReadMapper noticeReadMapper;
    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;

    @Override
    public PageResult<NoticeVO> pageNotices(Integer pageNum, Integer pageSize, String keyword, String noticeType) {
        String currentUsername = StpUtil.isLogin() ? StpUtil.getLoginIdAsString() : "";
        Page<CollabNotice> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);

        LambdaQueryWrapper<CollabNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollabNotice::getStatus, 1);

        if (StringUtils.hasText(keyword)) {
            wrapper.like(CollabNotice::getTitle, keyword.trim());
        }
        if (StringUtils.hasText(noticeType)) {
            wrapper.eq(CollabNotice::getNoticeType, noticeType.trim());
        }

        wrapper.orderByDesc(CollabNotice::getIsTop)
                .orderByDesc(CollabNotice::getPublishTime)
                .orderByDesc(CollabNotice::getCreateTime);

        page(page, wrapper);

        if (page.getRecords().isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0L, page.getCurrent(), page.getSize());
        }

        List<Long> noticeIds = page.getRecords().stream().map(CollabNotice::getId).collect(Collectors.toList());

        // 查询当前用户在这些通告中的已读情况
        Set<Long> userReadNoticeIds = new HashSet<>();
        if (StringUtils.hasText(currentUsername)) {
            List<CollabNoticeRead> myReads = noticeReadMapper.selectList(
                    new LambdaQueryWrapper<CollabNoticeRead>()
                            .in(CollabNoticeRead::getNoticeId, noticeIds)
                            .eq(CollabNoticeRead::getUsername, currentUsername)
            );
            userReadNoticeIds = myReads.stream().map(CollabNoticeRead::getNoticeId).collect(Collectors.toSet());
        }

        // 查询每条通告的已读总人数
        List<CollabNoticeRead> allReads = noticeReadMapper.selectList(
                new LambdaQueryWrapper<CollabNoticeRead>().in(CollabNoticeRead::getNoticeId, noticeIds)
        );
        Map<Long, Long> readCountMap = allReads.stream()
                .collect(Collectors.groupingBy(CollabNoticeRead::getNoticeId, Collectors.counting()));

        long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 1));

        List<NoticeVO> voList = new ArrayList<>();
        for (CollabNotice n : page.getRecords()) {
            NoticeVO vo = new NoticeVO();
            BeanUtils.copyProperties(n, vo);
            vo.setHasRead(userReadNoticeIds.contains(n.getId()));
            int readCnt = readCountMap.getOrDefault(n.getId(), 0L).intValue();
            vo.setReadCount(readCnt);
            vo.setUnreadCount((int) Math.max(0, totalUsers - readCnt));
            voList.add(vo);
        }

        return new PageResult<>(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoticeVO getNoticeDetailAndMarkRead(Long id) {
        CollabNotice notice = getById(id);
        if (notice == null) {
            throw new BusinessException("通告不存在或已删除");
        }

        // 1. 浏览量 +1
        notice.setViewCount(notice.getViewCount() == null ? 1 : notice.getViewCount() + 1);
        updateById(notice);

        // 2. 当前登录人自动打标为已读
        if (StpUtil.isLogin()) {
            String username = StpUtil.getLoginIdAsString();
            Long count = noticeReadMapper.selectCount(
                    new LambdaQueryWrapper<CollabNoticeRead>()
                            .eq(CollabNoticeRead::getNoticeId, id)
                            .eq(CollabNoticeRead::getUsername, username)
            );
            if (count == 0) {
                SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
                if (user != null) {
                    CollabNoticeRead readRecord = CollabNoticeRead.builder()
                            .noticeId(id)
                            .userId(user.getId())
                            .username(user.getUsername())
                            .realName(user.getRealName())
                            .readStatus(1)
                            .readTime(LocalDateTime.now())
                            .build();
                    noticeReadMapper.insert(readRecord);
                }
            }
        }

        NoticeVO vo = new NoticeVO();
        BeanUtils.copyProperties(notice, vo);
        vo.setHasRead(true);

        long readCnt = noticeReadMapper.selectCount(new LambdaQueryWrapper<CollabNoticeRead>().eq(CollabNoticeRead::getNoticeId, id));
        long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 1));
        vo.setReadCount((int) readCnt);
        vo.setUnreadCount((int) Math.max(0, totalUsers - readCnt));

        return vo;
    }

    @Override
    public NoticeReadStatsVO getNoticeReadStats(Long id) {
        CollabNotice notice = getById(id);
        if (notice == null) {
            throw new BusinessException("通告不存在");
        }

        List<SysUser> allUsers = userMapper.selectList(new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 1));
        Map<Long, String> deptMap = deptMapper.selectList(new LambdaQueryWrapper<SysDept>())
                .stream().collect(Collectors.toMap(SysDept::getId, SysDept::getDeptName, (v1, v2) -> v1));

        List<CollabNoticeRead> reads = noticeReadMapper.selectList(
                new LambdaQueryWrapper<CollabNoticeRead>().eq(CollabNoticeRead::getNoticeId, id)
        );

        Map<String, CollabNoticeRead> readMap = reads.stream()
                .collect(Collectors.toMap(CollabNoticeRead::getUsername, r -> r, (r1, r2) -> r1));

        List<NoticeReadStatsVO.UserReadItem> readUsers = new ArrayList<>();
        List<NoticeReadStatsVO.UserReadItem> unreadUsers = new ArrayList<>();

        for (SysUser u : allUsers) {
            String deptName = u.getDeptId() != null ? deptMap.getOrDefault(u.getDeptId(), "未归属") : "未归属";
            CollabNoticeRead r = readMap.get(u.getUsername());
            if (r != null) {
                readUsers.add(NoticeReadStatsVO.UserReadItem.builder()
                        .userId(u.getId())
                        .username(u.getUsername())
                        .realName(u.getRealName())
                        .deptName(deptName)
                        .readTime(r.getReadTime())
                        .build());
            } else {
                unreadUsers.add(NoticeReadStatsVO.UserReadItem.builder()
                        .userId(u.getId())
                        .username(u.getUsername())
                        .realName(u.getRealName())
                        .deptName(deptName)
                        .build());
            }
        }

        int total = allUsers.size();
        int readCnt = readUsers.size();
        int unreadCnt = unreadUsers.size();
        double rate = total > 0 ? Math.round((readCnt * 100.0 / total) * 10.0) / 10.0 : 0.0;

        return NoticeReadStatsVO.builder()
                .noticeId(id)
                .title(notice.getTitle())
                .totalUsers(total)
                .readCount(readCnt)
                .unreadCount(unreadCnt)
                .readRate(rate)
                .readUsers(readUsers)
                .unreadUsers(unreadUsers)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveNotice(NoticeSaveDTO dto) {
        String currentUsername = StpUtil.getLoginIdAsString();
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, currentUsername));
        String publisherName = user != null ? user.getRealName() : "系统发布";

        if (dto.getId() == null) {
            CollabNotice notice = new CollabNotice();
            BeanUtils.copyProperties(dto, notice);
            notice.setPublisherName(publisherName);
            notice.setPublishTime(LocalDateTime.now());
            notice.setViewCount(0);
            if (notice.getIsTop() == null) notice.setIsTop(0);
            if (notice.getStatus() == null) notice.setStatus(1);
            save(notice);
        } else {
            CollabNotice exist = getById(dto.getId());
            if (exist == null) throw new BusinessException("通告不存在");
            BeanUtils.copyProperties(dto, exist);
            updateById(exist);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotice(Long id) {
        removeById(id);
    }

    @Override
    public void remindUnreadUsers(Long id) {
        NoticeReadStatsVO stats = getNoticeReadStats(id);
        log.info("向通告 [{}] 的未读人员 (共 {} 人) 发送催办阅读提醒通知", stats.getTitle(), stats.getUnreadCount());
    }
}

package com.graddu.oa.module.business.administrative.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.common.enums.ProcStatusEnum;
import com.graddu.oa.common.exception.BusinessException;
import com.graddu.oa.module.business.administrative.dto.RedheadDocDTO;
import com.graddu.oa.module.business.administrative.entity.AdmRedheadDoc;
import com.graddu.oa.module.business.administrative.mapper.AdmRedheadDocMapper;
import com.graddu.oa.module.business.administrative.service.AdmRedheadService;
import com.graddu.oa.module.business.administrative.vo.RedheadDocVO;
import com.graddu.oa.module.system.entity.SysDept;
import com.graddu.oa.module.system.entity.SysUser;
import com.graddu.oa.module.system.mapper.SysDeptMapper;
import com.graddu.oa.module.system.mapper.SysUserMapper;
import com.graddu.oa.module.workflow.dto.ProcessStartDTO;
import com.graddu.oa.module.workflow.service.WfEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdmRedheadServiceImpl extends ServiceImpl<AdmRedheadDocMapper, AdmRedheadDoc> implements AdmRedheadService {

    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final WfEngineService workflowService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long draftDoc(RedheadDocDTO docDTO) {
        String currentUsername = StpUtil.getLoginIdAsString();
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, currentUsername));
        if (user == null) {
            throw new BusinessException("拟稿人不存在");
        }

        String deptName = "";
        if (user.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) {
                deptName = dept.getDeptName();
            }
        }

        AdmRedheadDoc doc = new AdmRedheadDoc();
        BeanUtils.copyProperties(docDTO, doc);
        doc.setIssuerDept(deptName);
        doc.setDrafterName(user.getRealName());
        doc.setProcStatus(Boolean.TRUE.equals(docDTO.getIsDraft()) ? ProcStatusEnum.DRAFT.getCode() : ProcStatusEnum.RUNNING.getCode());

        save(doc);

        if (!Boolean.TRUE.equals(docDTO.getIsDraft())) {
            ProcessStartDTO startDTO = new ProcessStartDTO();
            startDTO.setProcKey("PROC_REDHEAD");
            startDTO.setBusinessTable("oa_adm_redhead_doc");
            startDTO.setBusinessId(doc.getId());
            startDTO.setTitle("公文拟办签发: " + doc.getTitle());
            startDTO.setAuditUsernames(docDTO.getAuditUsernames());

            Long instId = workflowService.startProcess(startDTO);
            doc.setProcInstId(instId);
            updateById(doc);
        }

        log.info("红头公文拟稿完成: docNo={}, title={}", doc.getDocNo(), doc.getTitle());
        return doc.getId();
    }

    @Override
    public RedheadDocVO getDocDetail(Long id) {
        AdmRedheadDoc doc = getById(id);
        if (doc == null) {
            throw new BusinessException("公文不存在");
        }
        RedheadDocVO vo = new RedheadDocVO();
        BeanUtils.copyProperties(doc, vo);
        return vo;
    }

    @Override
    public PageResult<RedheadDocVO> listDocs(int pageNum, int pageSize) {
        Page<AdmRedheadDoc> page = new Page<>(pageNum, pageSize);
        page(page, new LambdaQueryWrapper<AdmRedheadDoc>()
                .orderByDesc(AdmRedheadDoc::getCreateTime));

        List<RedheadDocVO> list = new ArrayList<>();
        for (AdmRedheadDoc d : page.getRecords()) {
            RedheadDocVO vo = new RedheadDocVO();
            BeanUtils.copyProperties(d, vo);
            list.add(vo);
        }
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize());
    }
}

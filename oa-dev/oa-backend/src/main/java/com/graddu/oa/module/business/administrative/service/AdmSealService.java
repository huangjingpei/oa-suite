package com.graddu.oa.module.business.administrative.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.module.business.administrative.dto.SealApplyDTO;
import com.graddu.oa.module.business.administrative.entity.AdmSealApply;
import com.graddu.oa.module.business.administrative.vo.SealApplyVO;

public interface AdmSealService extends IService<AdmSealApply> {

    Long applySeal(SealApplyDTO applyDTO);

    SealApplyVO getSealDetail(Long id);

    PageResult<SealApplyVO> getMySealApplies(int pageNum, int pageSize);
}

package com.graddu.oa.module.business.administrative.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.module.business.administrative.dto.RedheadDocDTO;
import com.graddu.oa.module.business.administrative.entity.AdmRedheadDoc;
import com.graddu.oa.module.business.administrative.vo.RedheadDocVO;

public interface AdmRedheadService extends IService<AdmRedheadDoc> {

    Long draftDoc(RedheadDocDTO docDTO);

    RedheadDocVO getDocDetail(Long id);

    PageResult<RedheadDocVO> listDocs(int pageNum, int pageSize);
}

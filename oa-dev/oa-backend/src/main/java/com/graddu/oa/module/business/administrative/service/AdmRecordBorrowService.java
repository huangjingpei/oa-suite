package com.graddu.oa.module.business.administrative.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.module.business.administrative.dto.RecordBorrowDTO;
import com.graddu.oa.module.business.administrative.entity.AdmRecordBorrow;
import com.graddu.oa.module.business.administrative.vo.RecordBorrowVO;

public interface AdmRecordBorrowService extends IService<AdmRecordBorrow> {

    Long applyBorrow(RecordBorrowDTO borrowDTO);

    RecordBorrowVO getBorrowDetail(Long id);

    PageResult<RecordBorrowVO> getMyBorrows(int pageNum, int pageSize);
}

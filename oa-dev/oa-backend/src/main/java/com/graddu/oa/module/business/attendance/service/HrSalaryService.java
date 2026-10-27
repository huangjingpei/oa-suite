package com.graddu.oa.module.business.attendance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.module.business.attendance.dto.SalaryVerifyDTO;
import com.graddu.oa.module.business.attendance.entity.HrSalary;
import com.graddu.oa.module.business.attendance.vo.SalaryVO;

/**
 * 薪资工资条业务服务接口
 */
public interface HrSalaryService extends IService<HrSalary> {

    /**
     * 校验工资条二级查询安全密码 (或个人登录密码)
     */
    boolean verifySalaryPassword(SalaryVerifyDTO dto);

    /**
     * 分页查询当前登录用户的历史工资条 (严格按当前登录人账号数据隔离)
     */
    PageResult<SalaryVO> getMySalaries(String month, int pageNum, int pageSize);

    /**
     * 获取指定月份工资条详情 (含完整明细)
     */
    SalaryVO getSalaryDetail(Long id);

    /**
     * 员工确认签收工资条
     */
    void confirmSalary(Long id);
}

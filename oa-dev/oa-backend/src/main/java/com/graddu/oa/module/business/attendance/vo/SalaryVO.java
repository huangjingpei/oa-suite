package com.graddu.oa.module.business.attendance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 员工月度工资条视图对象
 */
@Data
@Schema(description = "工资条视图展示对象")
public class SalaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "薪酬月份 (如 2026-10)")
    private String salaryMonth;

    @Schema(description = "员工用户ID")
    private Long userId;

    @Schema(description = "员工工号/账号")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "所属部门")
    private String deptName;

    @Schema(description = "职务岗位")
    private String postName;

    @Schema(description = "基本薪资")
    private BigDecimal baseSalary;

    @Schema(description = "岗位工资/津贴")
    private BigDecimal postSalary;

    @Schema(description = "绩效奖金")
    private BigDecimal performanceSalary;

    @Schema(description = "加班工时补贴")
    private BigDecimal overtimeSalary;

    @Schema(description = "全勤/专项激励奖")
    private BigDecimal bonus;

    @Schema(description = "应发总计 (Gross)")
    private BigDecimal grossSalary;

    @Schema(description = "个人社保五险")
    private BigDecimal pensionFee;

    @Schema(description = "个人住房公积金")
    private BigDecimal housingFund;

    @Schema(description = "个人所得税扣缴")
    private BigDecimal taxFee;

    @Schema(description = "迟到旷工/其他扣除")
    private BigDecimal otherDeduction;

    @Schema(description = "税后实发金额 (Net Pay)")
    private BigDecimal netSalary;

    @Schema(description = "发放状态 (1已发放, 0核算中)")
    private Integer status;

    @Schema(description = "员工签收状态 (0待签收, 1已核对签收)")
    private Integer isConfirmed;

    @Schema(description = "签收确认时间")
    private LocalDateTime confirmTime;

    @Schema(description = "发放备注说明")
    private String remark;

    @Schema(description = "核算时间")
    private LocalDateTime createTime;
}

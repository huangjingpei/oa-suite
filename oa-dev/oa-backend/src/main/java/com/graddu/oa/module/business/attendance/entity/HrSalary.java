package com.graddu.oa.module.business.attendance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 员工月度工资条实体
 */
@Data
@TableName("oa_hr_salary")
public class HrSalary implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String salaryMonth;

    private Long userId;

    private String username;

    private String realName;

    private String deptName;

    private String postName;

    private BigDecimal baseSalary;

    private BigDecimal postSalary;

    private BigDecimal performanceSalary;

    private BigDecimal overtimeSalary;

    private BigDecimal bonus;

    private BigDecimal grossSalary;

    private BigDecimal pensionFee;

    private BigDecimal housingFund;

    private BigDecimal taxFee;

    private BigDecimal otherDeduction;

    private BigDecimal netSalary;

    private Integer status;

    private Integer isConfirmed;

    private LocalDateTime confirmTime;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDeleted;
}

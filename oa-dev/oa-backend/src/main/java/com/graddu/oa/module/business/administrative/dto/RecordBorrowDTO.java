package com.graddu.oa.module.business.administrative.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 档案与资料借阅申请提交对象
 */
@Data
@Schema(description = "档案借阅申请参数")
public class RecordBorrowDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "档案名称不能为空")
    @Schema(description = "档案/卷册名称", example = "2025年度第三季度财务审计报告", requiredMode = Schema.RequiredMode.REQUIRED)
    private String recordTitle;

    @Schema(description = "借阅形式 (READ_ONLY 现场查阅, BORROW_PHYSICAL 实体借出, COPY 复印)", example = "READ_ONLY")
    private String borrowType = "READ_ONLY";

    @Schema(description = "借阅卷数/份数", example = "1")
    private Integer copies = 1;

    @NotNull(message = "借出时间不能为空")
    @Schema(description = "计划借出时间", example = "2026-10-28 10:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startTime;

    @NotNull(message = "预计归还时间不能为空")
    @Schema(description = "预计归还时间", example = "2026-10-28 17:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime expectedReturnTime;

    @NotBlank(message = "借阅事由说明不能为空")
    @Schema(description = "借阅事由及用途", example = "筹备年度税务申报，需调阅原始财务报告", requiredMode = Schema.RequiredMode.REQUIRED)
    private String purpose;

    @Schema(description = "附件材料URL")
    private String files;

    @Schema(description = "是否保存为草稿", defaultValue = "false")
    private Boolean isDraft = false;

    @Schema(description = "审批人账号列表")
    private List<String> auditUsernames;
}

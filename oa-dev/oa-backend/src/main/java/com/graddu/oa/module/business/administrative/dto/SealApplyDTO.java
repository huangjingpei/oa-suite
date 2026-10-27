package com.graddu.oa.module.business.administrative.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 企业用印申请提交对象
 */
@Data
@Schema(description = "企业用印申请参数")
public class SealApplyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "印章类型不能为空")
    @Schema(description = "印章类型 (COMMON 公章, CONTRACT 合同章, LEGAL 法人章, FINANCE 财务章, DECLARE 申报章)", example = "COMMON", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sealType;

    @NotBlank(message = "用印文件名称不能为空")
    @Schema(description = "待盖章文件名称", example = "企业战略合作框架协议", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileName;

    @NotNull(message = "用印份数不能为空")
    @Schema(description = "用印份数", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sealCopies;

    @NotBlank(message = "用印用途说明不能为空")
    @Schema(description = "详细事由及用途", example = "与合作方签署保密协议与战略框架协议", requiredMode = Schema.RequiredMode.REQUIRED)
    private String purpose;

    @Schema(description = "经办人姓名")
    private String agentName;

    @Schema(description = "盖章材料附件URL")
    private String files;

    @Schema(description = "关联合同金额 (合同章时使用)")
    private BigDecimal contractAmount;

    @Schema(description = "相对方合作单位 (合同章时使用)")
    private String counterparty;

    @Schema(description = "是否保存为草稿", defaultValue = "false")
    private Boolean isDraft = false;

    @Schema(description = "动态审批人账号列表")
    private List<String> auditUsernames;
}

package com.graddu.oa.module.business.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 考勤打卡请求传输对象
 */
@Data
@Schema(description = "员工考勤打卡参数")
public class ClockInDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "打卡类型不能为空")
    @Schema(description = "打卡类型 (ON 上班打卡, OFF 下班打卡)", example = "ON", requiredMode = Schema.RequiredMode.REQUIRED)
    private String clockType;

    @Schema(description = "打卡设备 (MOBILE 手机端, WEB 网页端)", example = "WEB")
    private String deviceType = "WEB";

    @Schema(description = "地理位置名称", example = "北京市海淀区中关村南大街1号")
    private String locationAddress;

    @Schema(description = "经度")
    private BigDecimal latitude;

    @Schema(description = "纬度")
    private BigDecimal longitude;

    @Schema(description = "打卡备注/说明")
    private String remark;
}

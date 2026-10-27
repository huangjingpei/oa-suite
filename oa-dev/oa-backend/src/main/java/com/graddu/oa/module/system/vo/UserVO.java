package com.graddu.oa.module.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 员工用户信息视图展示对象
 */
@Data
@Schema(description = "员工用户视图对象")
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String realName;

    private String workNo;

    private Long deptId;

    private String deptName;

    private String postName;

    private String email;

    private String phone;

    private Integer gender;

    private String avatar;

    private Integer status;

    private LocalDateTime createTime;

    private List<Long> roleIds;

    private List<String> roleNames;
}

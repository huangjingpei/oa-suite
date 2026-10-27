package com.graddu.oa.common.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页响应包装对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一分页结果实体")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "数据记录列表")
    private List<T> records = Collections.emptyList();

    @Schema(description = "总记录数", example = "100")
    private long total = 0L;

    @Schema(description = "当前页码", example = "1")
    private long pageNum = 1L;

    @Schema(description = "每页条数", example = "10")
    private long pageSize = 10L;

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
    }

    public static <T> PageResult<T> of(List<T> records, long total, long pageNum, long pageSize) {
        return new PageResult<>(records, total, pageNum, pageSize);
    }
}

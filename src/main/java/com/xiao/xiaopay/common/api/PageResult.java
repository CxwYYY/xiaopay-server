package com.xiao.xiaopay.common.api;

import java.util.List;

/**
 * 管理后台分页查询统一返回结构。
 */
public record PageResult<T>(
        long total,
        long pageNo,
        long pageSize,
        List<T> records
) {
}

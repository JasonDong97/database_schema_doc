package com.djx.entity;

import lombok.Data;

/**
 * @author dongjingxiang
 * @date 2021/7/27 14:43
 */
@Data
public class ColumnInfo {
    private String columnName;
    private String columnType;
    private String columnComment;
}

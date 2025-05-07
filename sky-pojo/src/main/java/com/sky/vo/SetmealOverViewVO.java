package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 套餐总览
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetmealOverViewVO implements Serializable {
    // 建议显式声明 serialVersionUID
    private static final long serialVersionUID = 1L;
    // 已启售数量
    private Integer sold;

    // 已停售数量
    private Integer discontinued;
}

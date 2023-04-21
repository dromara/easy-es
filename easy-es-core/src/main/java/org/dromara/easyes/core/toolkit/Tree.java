package org.dromara.easyes.core.toolkit;

import lombok.Data;

import java.util.List;

/**
 * 抽象树基类
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@Data
public class Tree {
    /**
     * id
     */
    private String id;
    /**
     * 层级
     */
    private int level;
    /**
     * 父节点id
     */
    private String parentId;
    /**
     * 子节点列表
     */
    private List<? extends Tree> children;
}

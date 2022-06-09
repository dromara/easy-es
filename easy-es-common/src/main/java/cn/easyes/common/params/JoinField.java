package cn.easyes.common.params;

import lombok.Data;

/**
 * 父子类型统一关系字段,推荐直接使用此类,不要重复造轮子
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@Data
public class JoinField {
    /**
     * 字段名
     */
    private String name;
    /**
     * 父文档id
     */
    private String parent;
}

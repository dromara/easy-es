package com.xpc.easyes.core.params;

import lombok.Data;

/**
 * or出现次数统计类
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@Data
public class OrCount {
    /**
     * or出现的总数
     */
    private int orAllCount = 0;
    /**
     * or在and及or内层出现的次数
     */
    private int orInnerCount = 0;
}

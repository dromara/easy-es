package com.xpc.easyes.core.toolkit;

import com.xpc.easyes.core.common.PageInfo;
import com.xpc.easyes.core.constants.BaseEsConstants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页工具
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PageHelper {
    /**
     * @param list     数据列表
     * @param total    总数
     * @param pageNum  当前页
     * @param pageSize 总页数
     * @param <T>      数据类型
     * @return 分页信息
     */
    public static <T> PageInfo<T> getPageInfo(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        PageInfo<T> pageInfo = new PageInfo<>();
        int pages = (int) (total / pageSize + ((total % pageSize == 0) ? 0 : 1));
        pageInfo.setList(list);
        pageInfo.setSize(list.size());
        pageInfo.setTotal(total);
        pageInfo.setPages(pages);
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        return pageInfo;
    }
}

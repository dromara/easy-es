package org.dromara.easyes.core.biz;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * 分页参数 来源:<a href="https://github.com/pagehelper/Mybatis-PageHelper">Mybatis-PageHelper</a>
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EsPageInfo<T> extends PageSerializable<T> {
    /**
     * 当前页
     */
    private int pageNum;
    /**
     * 每页的数量
     */
    private int pageSize;
    /**
     * 当前页的数量
     */
    private int size;

    /**
     * 当前页面第一个元素在数据库中的行号
     */
    private int startRow;
    /**
     * 当前页面最后一个元素在数据库中的行号
     */
    private int endRow;
    /**
     * 总页数
     */
    private int pages;

    /**
     * 前一页
     */
    private int prePage;
    /**
     * 下一页
     */
    private int nextPage;

    /**
     * 是否为第一页
     */
    private boolean isFirstPage = false;
    /**
     * 是否为最后一页
     */
    private boolean isLastPage = false;
    /**
     * 是否有前一页
     */
    private boolean hasPreviousPage = false;
    /**
     * 是否有下一页
     */
    private boolean hasNextPage = false;
    /**
     * 导航页码数
     */
    private int navigatePages;
    /**
     * 所有导航页号
     */
    private int[] navigatePageNums;
    /**
     * 导航条上的第一页
     */
    private int navigateFirstPage;
    /**
     * 导航条上的最后一页
     */
    private int navigateLastPage;

    /**
     * 包装Page对象
     *
     * @param list 数据
     */
    public EsPageInfo(List<T> list) {
        this(list, 8);
    }

    /**
     * 包装Page对象
     *
     * @param list          数据
     * @param navigatePages 导航页
     */
    public EsPageInfo(List<T> list, int navigatePages) {
        super(list);
        this.pageNum = 1;
        this.pageSize = list.size();

        this.pages = this.pageSize > 0 ? 1 : 0;
        this.size = list.size();
        this.startRow = 0;
        this.endRow = !list.isEmpty() ? list.size() - 1 : 0;

        this.navigatePages = navigatePages;
    }

    public static <T> EsPageInfo<T> of(List<T> list) {
        return new EsPageInfo<>(list);
    }

    public static <T> EsPageInfo<T> of(List<T> list, int navigatePages) {
        return new EsPageInfo<>(list, navigatePages);
    }

    /**
     * 计算导航页
     */
    public void calcNavigatePageNums() {
        //当总页数小于或等于导航页码数时
        if (pages <= navigatePages) {
            navigatePageNums = new int[pages];
            for (int i = 0; i < pages; i++) {
                navigatePageNums[i] = i + 1;
            }
        } else { //当总页数大于导航页码数时
            navigatePageNums = new int[navigatePages];
            int startNum = pageNum - navigatePages / 2;
            int endNum = pageNum + navigatePages / 2;

            if (startNum < 1) {
                startNum = 1;
                //(最前navigatePages页
                for (int i = 0; i < navigatePages; i++) {
                    navigatePageNums[i] = startNum++;
                }
            } else if (endNum > pages) {
                endNum = pages;
                //最后navigatePages页
                for (int i = navigatePages - 1; i >= 0; i--) {
                    navigatePageNums[i] = endNum--;
                }
            } else {
                //所有中间页
                for (int i = 0; i < navigatePages; i++) {
                    navigatePageNums[i] = startNum++;
                }
            }
        }
    }

    /**
     * 计算前后页，第一页，最后一页
     */
    public void calcPage() {
        if (navigatePageNums != null && navigatePageNums.length > 0) {
            navigateFirstPage = navigatePageNums[0];
            navigateLastPage = navigatePageNums[navigatePageNums.length - 1];
            if (pageNum > 1) {
                prePage = pageNum - 1;
            }
            if (pageNum < pages) {
                nextPage = pageNum + 1;
            }
        }
    }

    /**
     * 判定页面边界
     */
    public void judgePageBoundary() {
        isFirstPage = pageNum == 1;
        isLastPage = pageNum == pages || pages == 0;
        hasPreviousPage = pageNum > 1;
        hasNextPage = pageNum < pages;
    }

    @Override
    public String toString() {
        return "PageInfo{" +
                "pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", size=" + size +
                ", startRow=" + startRow +
                ", endRow=" + endRow +
                ", pages=" + pages +
                ", prePage=" + prePage +
                ", nextPage=" + nextPage +
                ", isFirstPage=" + isFirstPage +
                ", isLastPage=" + isLastPage +
                ", hasPreviousPage=" + hasPreviousPage +
                ", hasNextPage=" + hasNextPage +
                ", navigatePages=" + navigatePages +
                ", navigatepageNums=" + Arrays.toString(navigatePageNums) +
                ", navigateFirstPage=" + navigateFirstPage +
                ", navigateLastPage=" + navigateLastPage +
                ", total=" + total +
                ", list=" + list +
                '}';
    }
}

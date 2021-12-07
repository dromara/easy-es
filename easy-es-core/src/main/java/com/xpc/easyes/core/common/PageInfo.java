package com.xpc.easyes.core.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * 分页参数 来源:https://github.com/pagehelper/Mybatis-PageHelper
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Data
@NoArgsConstructor
public class PageInfo<T> extends PageSerializable<T> {
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
    private int[] navigatepageNums;
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
     * @param list 数据
     */
    public PageInfo(List<T> list) {
        this(list, 8);
    }

    /**
     * 包装Page对象
     * @param list 数据
     * @param navigatePages 导航页
     */
    public PageInfo(List<T> list, int navigatePages) {
        super(list);
        this.pageNum = 1;
        this.pageSize = list.size();

        this.pages = this.pageSize > 0 ? 1 : 0;
        this.size = list.size();
        this.startRow = 0;
        this.endRow = list.size() > 0 ? list.size() - 1 : 0;

        this.navigatePages = navigatePages;
        //计算导航页
        calcNavigatepageNums();
        //计算前后页，第一页，最后一页
        calcPage();
        //判断页面边界
        judgePageBoudary();
    }

    public static <T> PageInfo<T> of(List<T> list) {
        return new PageInfo<T>(list);
    }

    public static <T> PageInfo<T> of(List<T> list, int navigatePages) {
        return new PageInfo<T>(list, navigatePages);
    }

    /**
     * 计算导航页
     */
    private void calcNavigatepageNums() {
        //当总页数小于或等于导航页码数时
        if (pages <= navigatePages) {
            navigatepageNums = new int[pages];
            for (int i = 0; i < pages; i++) {
                navigatepageNums[i] = i + 1;
            }
        } else { //当总页数大于导航页码数时
            navigatepageNums = new int[navigatePages];
            int startNum = pageNum - navigatePages / 2;
            int endNum = pageNum + navigatePages / 2;

            if (startNum < 1) {
                startNum = 1;
                //(最前navigatePages页
                for (int i = 0; i < navigatePages; i++) {
                    navigatepageNums[i] = startNum++;
                }
            } else if (endNum > pages) {
                endNum = pages;
                //最后navigatePages页
                for (int i = navigatePages - 1; i >= 0; i--) {
                    navigatepageNums[i] = endNum--;
                }
            } else {
                //所有中间页
                for (int i = 0; i < navigatePages; i++) {
                    navigatepageNums[i] = startNum++;
                }
            }
        }
    }

    /**
     * 计算前后页，第一页，最后一页
     */
    private void calcPage() {
        if (navigatepageNums != null && navigatepageNums.length > 0) {
            navigateFirstPage = navigatepageNums[0];
            navigateLastPage = navigatepageNums[navigatepageNums.length - 1];
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
    private void judgePageBoudary() {
        isFirstPage = pageNum == 1;
        isLastPage = pageNum == pages || pages == 0;
        ;
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
                ", navigatepageNums=" + Arrays.toString(navigatepageNums) +
                ", navigateFirstPage=" + navigateFirstPage +
                ", navigateLastPage=" + navigateLastPage +
                ", total=" + total +
                ", list=" + list +
                '}';
    }
}

package cn.easyes.core.biz;

import lombok.Data;

import java.util.List;

/**
 * searchAfter 分页参数
 **/
@Data
public class SAPageInfo<T> extends PageSerializable<T> {
    /**
     * 当前sort
     */
    private List<Object> searchAfter;
    /**
     * 每页的数量
     */
    private int pageSize;
    /**
     * 总页数
     */
    private int pages;
    /**
     * 下一页sort
     */
    private List<Object> nextSearchAfter;

    @Override
    public String toString() {
        return "SAPageInfo{" +
                "searchAfter=" + searchAfter +
                ", pageSize=" + pageSize +
                ", pages=" + pages +
                ", nextSearchAfter=" + nextSearchAfter +
                ", total=" + total +
                ", list=" + list +
                '}';
    }
}

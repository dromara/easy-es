package org.dromara.easyes.core.biz;

import co.elastic.clients.elasticsearch._types.FieldValue;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * searchAfter 分页参数
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class SAPageInfo<T> extends PageSerializable<T> {
    /**
     * 当前sort
     */
    private List<FieldValue> searchAfter;
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
    private List<FieldValue> nextSearchAfter;

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

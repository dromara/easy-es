package org.dromara.easyes.common.join;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
public class BaseJoin {

    @JsonIgnore
    public Map<String, Map<String, String>> joinField;

    @JsonAnyGetter
    public Map<String, Map<String, String>> getJoinField() {
        return joinField;
    }

    /**
     * 添加join字段信息. json示例如下:
     * "fieldName": {
     *     "name": xxx,
     *     "parentId": xxx
     * }
     *
     * @param fieldName join字段名称(json中的key)
     * @param name      名称
     * @param parentId  父id
     */
    public void addJoinField(String fieldName, String name, String parentId) {
        joinField = new HashMap<String, Map<String, String>>() {{
            put(fieldName, new HashMap<String, String>() {{
                put("name", name);
                if (parentId != null) {
                    put("parent", parentId);
                }
            }});
        }};
    }
}
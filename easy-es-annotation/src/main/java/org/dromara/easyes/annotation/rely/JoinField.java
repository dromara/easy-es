package org.dromara.easyes.annotation.rely;

/**
 * 父子类型统一关系字段,推荐直接使用此类,不要重复造轮子
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
public class JoinField {
    /**
     * 字段名
     */
    private String name;
    /**
     * 父文档id
     */
    private String parent;

    public JoinField() {
    }

    public JoinField(String name, String parent) {
        this.name = name;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return "JoinField{" +
                "name='" + name + '\'' +
                ", parent='" + parent + '\'' +
                '}';
    }
}

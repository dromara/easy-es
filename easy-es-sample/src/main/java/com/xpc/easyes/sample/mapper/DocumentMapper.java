package com.xpc.easyes.sample.mapper;

import com.xpc.easyes.core.conditions.interfaces.BaseEsMapper;
import com.xpc.easyes.sample.entity.Document;

/**
 * mapper 相当于Mybatis-plus的mapper
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 用于CRUD, 可继承父类BaseMapper中封装的方法
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface DocumentMapper extends BaseEsMapper<Document> {
}

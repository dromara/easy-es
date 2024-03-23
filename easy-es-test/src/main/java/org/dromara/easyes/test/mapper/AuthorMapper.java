package org.dromara.easyes.test.mapper;


import org.dromara.easyes.annotation.EsDS;
import org.dromara.easyes.core.core.BaseEsMapper;
import org.dromara.easyes.test.entity.Author;

/**
 * 父子类型-子文档的mapper
 * <p>
 * Copyright © 2024 xpc1024 All Rights Reserved
 **/
@EsDS("ds1")
public interface AuthorMapper extends BaseEsMapper<Author> {
}

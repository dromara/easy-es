package cn.easyes.common.constants;

/**
 * EasyEs的常量
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface BaseEsConstants {
    /**
     * 是否启用本框架前缀, 默认启用
     */
    String ENABLE_PREFIX = "easy-es.enable";
    /**
     * 是否打印本框架Banner
     */
    String ENABLE_BANNER = "easy-es.banner";
    /**
     * 默认主键名称
     */
    String DEFAULT_ID_NAME = "id";
    /**
     * Es 默认的主键名称
     */
    String DEFAULT_ES_ID_NAME = "_id";
    /**
     * 数字0
     */
    Integer ZERO = 0;
    /**
     * 数字1
     */
    Integer ONE = 1;
    /**
     * 数字-1
     */
    Integer MINUS_ONE = -1;
    /**
     * 默认索引别名
     */
    String DEFAULT_ALIAS = "ee_default_alias";
    /**
     * 高亮默认前缀标签
     */
    String HIGH_LIGHT_PRE_TAG = "<em>";
    /**
     * 高亮默认后缀标签
     */
    String HIGH_LIGHT_POST_TAG = "</em>";
    /**
     * 默认的当前页码
     */
    Integer PAGE_NUM = 1;
    /**
     * 默认的每页显示条目数
     */
    Integer PAGE_SIZE = 10;
    /**
     * 默认字段boost权重
     */
    Float DEFAULT_BOOST = 1.0F;
    /**
     * 空字符串
     */
    String EMPTY_STR = "";
    /**
     * 冒号
     */
    String COLON = ":";
    /**
     * 逗号分隔符
     */
    String COMMA_SEPARATOR = ",";
    /**
     * ee内置es分布式锁索引名称
     */
    String LOCK_INDEX = "ee-distribute-lock";
    /**
     * 当前激活索引key
     */
    String ACTIVE_INDEX_KEY = "ee_active_index_key";
    /**
     * 索引更新时间key
     */
    String GMT_MODIFIED = "gmt_modified";
    /**
     * get 方法前缀
     */
    String GET_FUNC_PREFIX = "get";
    /**
     * set 方法前缀
     */
    String SET_FUNC_PREFIX = "set";
    /**
     * 基本数据类型的get方法前缀
     */
    String IS_FUNC_PREFIX = "is";
    /**
     * 分片数量字段
     */
    String SHARDS_FIELD = "index.number_of_shards";
    /**
     * 副本数量字段
     */
    String REPLICAS_FIELD = "index.number_of_replicas";
    /**
     * 索引特性
     */
    String PROPERTIES = "properties";
    /**
     * 字段类型
     */
    String TYPE = "type";
    /**
     * 日期格式化
     */
    String FORMAT = "format";
    /**
     * 分词器
     */
    String ANALYZER = "analyzer";
    /**
     * 查询分词器
     */
    String SEARCH_ANALYZER = "search_analyzer";
    /**
     * 字段关系
     */
    String RELATIONS = "relations";
    /**
     * 父子类型-父id字段
     */
    String PARENT = "parent";
    /**
     * 通配符
     */
    String WILDCARD_SIGN = "*";
    /**
     * es默认schema
     */
    String DEFAULT_SCHEMA = "http";
    /**
     * 默认返回数
     */
    Integer DEFAULT_SIZE = 10000;
    /**
     * es默认得分字段
     */
    String SCORE_FIELD = "_score";
    /**
     * DSL语句前缀
     */
    String DSL_PREFIX = "===> Execute By Easy-Es: ";
    /**
     * count DSL语句前缀
     */
    String COUNT_DSL_PREFIX = "===> Execute Count By Easy-Es(Note the size specified in wrapper won't affect the total count): ";
    /**
     * 分片数key
     */
    String SHARDS_NUM_KEY = "index.number_of_shards";
    /**
     * 副本数key
     */
    String REPLICAS_NUM_KEY = "index.number_of_replicas";
    /**
     * 默认迁移操作规则
     */
    String DEFAULT_DEST_OP_TYPE = "create";
    /**
     * 默认冲突处理
     */
    String DEFAULT_CONFLICTS = "proceed";
    /**
     * 更新索引时自动创建的索引后缀s 灵感来源于jvm young区s0,s1垃圾回收
     */
    String S_SUFFIX = "_s";
    /**
     * 更新索引时自动创建的索引后缀s0
     */
    String SO_SUFFIX = "_s0";
    /**
     * 更新索引时自动创建的索引后缀s1
     */
    String S1_SUFFIX = "_s1";
    /**
     * 分布式锁提示内容
     */
    String DISTRIBUTED_LOCK_TIP_JSON = "{\"tip\":\"Do not delete unless deadlock occurs\"}";
    /**
     * 获取/释放 分布式锁 最大失败重试次数
     */
    Integer LOCK_MAX_RETRY = 3;
    /**
     * 初始任务执行延迟
     */
    int INITIAL_DELAY = 30;
    /**
     * 默认分片数
     */
    int DEFAULT_SHARDS = 1;
    /**
     * 默认副本数
     */
    int DEFAULT_REPLICAS = 1;
    /**
     * 被折叠的重复数据数量的key
     */
    String REPEAT_NUM_KEY = "repeat_num";
    /**
     * 默认最大拓展数
     */
    int DEFAULT_MAX_EXPANSIONS = 50;
    /**
     * 默认最小匹配百分比
     */
    int DEFAULT_MIN_SHOULD_MATCH = 60;
    /**
     * 百分比符号
     */
    String PERCENT = "%";
    /**
     * 嵌套类型 path和field连接符
     */
    String PATH_FIELD_JOIN = ".";
    /**
     * 父子类型索引-eager_global_ordinals
     */
    String EAGER_GLOBAL_ORDINALS_KEY = "eager_global_ordinals";
    /**
     * 索引权重key
     */
    String BOOST_KEY = "boost";
    /**
     * 高亮截取默认长度
     */
    int DEFAULT_FRAGMENT_SIZE = 100;
    /**
     * 针对text进行聚合
     */
    String FIELD_DATA = "fielddata";

}

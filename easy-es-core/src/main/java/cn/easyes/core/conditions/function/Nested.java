package cn.easyes.core.conditions.function;

import org.apache.lucene.search.join.ScoreMode;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * 嵌套关系
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface Nested<Param, Children> extends Serializable {
    /**
     * AND 嵌套 保留mp用户习惯
     *
     * @param consumer 嵌套条件函数
     * @return wrapper
     */
    default Children and(Consumer<Param> consumer) {
        return and(true, consumer);
    }

    /**
     * AND 嵌套 保留mp用户习惯
     *
     * @param condition 条件
     * @param consumer  嵌套条件函数
     * @return wrapper
     */
    Children and(boolean condition, Consumer<Param> consumer);

    /**
     * OR 嵌套 保留mp用户习惯
     *
     * @param consumer 嵌套条件函数
     * @return wrapper
     */
    default Children or(Consumer<Param> consumer) {
        return or(true, consumer);
    }

    /**
     * OR 嵌套 保留mp用户习惯
     *
     * @param condition 条件
     * @param consumer  嵌套条件函数
     * @return wrapper
     */
    Children or(boolean condition, Consumer<Param> consumer);

    /**
     * must 嵌套 等价于and
     *
     * @param consumer 嵌套条件函数
     * @return wrapper
     */
    default Children must(Consumer<Param> consumer) {
        return must(true, consumer);
    }

    /**
     * must 嵌套 等价于and
     *
     * @param condition 条件
     * @param consumer  嵌套条件函数
     * @return wrapper
     */
    Children must(boolean condition, Consumer<Param> consumer);

    /**
     * should 嵌套 等价于or
     *
     * @param consumer 嵌套条件函数
     * @return wrapper
     */
    default Children should(Consumer<Param> consumer) {
        return should(true, consumer);
    }

    /**
     * should 嵌套 等价于or
     *
     * @param condition 条件
     * @param consumer  嵌套条件函数
     * @return wrapper
     */
    Children should(boolean condition, Consumer<Param> consumer);

    /**
     * filter 嵌套 和and及must功能基本一致，但filter不返回得分，效率更高
     *
     * @param consumer 嵌套条件函数
     * @return wrapper
     */
    default Children filter(Consumer<Param> consumer) {
        return filter(true, consumer);
    }

    /**
     * filter 嵌套 和and及must功能基本一致，但filter不返回得分，效率更高
     *
     * @param condition 条件
     * @param consumer  条件函数
     * @return 泛型
     */
    Children filter(boolean condition, Consumer<Param> consumer);

    /**
     * must not 嵌套 等价于 must条件取反，即 非must
     *
     * @param consumer 嵌套条件函数
     * @return wrapper
     */
    default Children not(Consumer<Param> consumer) {
        return not(true, consumer);
    }

    /**
     * must not 嵌套 等价于 must条件取反，即 非must
     *
     * @param condition 条件
     * @param consumer  嵌套条件函数
     * @return wrapper
     */
    Children not(boolean condition, Consumer<Param> consumer);

    /**
     * @param path     上级路径
     * @param consumer 嵌套条件函数
     * @return wrapper
     */
    default Children nested(String path, Consumer<Param> consumer) {
        return nested(true, path, consumer);
    }

    /**
     * 嵌套类型查询
     *
     * @param path      上级路径
     * @param consumer  嵌套条件函数
     * @param scoreMode 得分模式
     * @return wrapper
     */
    default Children nested(String path, Consumer<Param> consumer, ScoreMode scoreMode) {
        return nested(true, path, consumer, scoreMode);
    }

    /**
     * 嵌套类型查询
     *
     * @param condition 执行条件
     * @param path      上级路径
     * @param consumer  嵌套条件函数
     * @return wrapper
     */
    default Children nested(boolean condition, String path, Consumer<Param> consumer) {
        return nested(condition, path, consumer, ScoreMode.None);
    }

    /**
     * 嵌套类型查询
     *
     * @param condition 执行条件
     * @param path      上级路径
     * @param consumer  嵌套条件函数
     * @param scoreMode 得分模式
     * @return wrapper
     */
    Children nested(boolean condition, String path, Consumer<Param> consumer, ScoreMode scoreMode);
}

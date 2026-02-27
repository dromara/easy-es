package org.dromara.easyes.core.toolkit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dromara.easyes.annotation.IndexField;
import org.dromara.easyes.common.params.SFunction;
import org.dromara.easyes.common.utils.DateUtil;
import org.dromara.easyes.common.utils.ReflectionKit;
import org.dromara.easyes.common.utils.StringUtils;
import org.dromara.easyes.core.cache.GlobalConfigCache;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.dromara.easyes.common.constants.BaseEsConstants.*;

/**
 * 根据字段上的注解，格式化时间为
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EEDateUtils {
    private static final Map<SFunction<?, ?>, String> DATE_FORMAT_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取字段上的 @IndexField 注解中的 dateFormat 值
     *
     * @param func 方法引用
     * @return 注解中的 dateFormat 值，若无注解或未设置则返回默认值
     */
    public static <T> String getDateFormat(SFunction<T, ?> func) {
        return DATE_FORMAT_CACHE.computeIfAbsent(func, k -> {
            try {
                SerializedLambda lambda = FieldUtils.getSerializedLambda(func);

                // 获取方法类和方法名
                String implMethodName = lambda.getImplMethodName();
                Class<?> targetClass = Class.forName(lambda.getImplClass().replace("/", "."));

                // 提取字段名（如 getLdt -> ldt）
                String fieldName = FieldUtils.resolveFieldName(implMethodName);

                // 获取字段对象
                Field field = ReflectionKit.getField(targetClass, fieldName);

                String dateFormat = null;
                if (field.isAnnotationPresent(IndexField.class)) {
                    dateFormat = field.getAnnotation(IndexField.class).dateFormat();
                }
                if (StringUtils.isBlank(dateFormat)) {
                    dateFormat = GlobalConfigCache.getGlobalConfig().getDbConfig().getDefaultDateFormat();
                }
                if (ES_DEFAULT_DATE_TIME_FORMAT.equals(dateFormat)) {
                    dateFormat = UTC_WITH_XXX_OFFSET_TIME_FORMAT;
                }
                return dateFormat;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static <T> String format(SFunction<T, ?> func, Date val) {
        String format = getDateFormat(func);
        return DateUtil.format(val, format);
    }

    public static <T> String format(SFunction<T, ?> func, LocalDate val) {
        String format = getDateFormat(func);
        return DateUtil.format(val, format);
    }

    public static <T> String format(SFunction<T, ?> func, LocalDateTime val) {
        String format = getDateFormat(func);
        return DateUtil.format(val, format);
    }
}

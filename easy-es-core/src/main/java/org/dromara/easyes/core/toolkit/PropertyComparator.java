package org.dromara.easyes.core.toolkit;

import co.elastic.clients.elasticsearch._types.mapping.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 索引属性变化比较器
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
public class PropertyComparator {

    public static boolean isPropertyMapEqual(Map<String, Property> map1, Map<String, Property> map2) {
        // 直接引用相等性检查
        if (map1 == map2) {
            return true;
        }

        // 基础检查：大小是否一致
        if (map1.size() != map2.size()) {
            return false;
        }

        // 遍历所有键进行深度比较
        for (String key : map1.keySet()) {
            Property prop1 = map1.get(key);
            // 检查键是否存在
            if (!map2.containsKey(key)) {
                return false;
            }
            Property prop2 = map2.get(key);
            // 深度比较属性
            if (!deepCompareProperties(prop1, prop2)) {
                return false;
            }
        }

        return true;
    }

    private static boolean deepCompareProperties(Property prop1, Property prop2) {
        // 快速引用相等检查
        if (prop1 == prop2) return true;

        // 类型基础检查
        if (prop1._kind() != prop2._kind()) return false;

        // 按类型分发处理逻辑
        switch (prop1._kind()) {
            case Nested:
                return compareNested(prop1.nested(), prop2.nested());
            case Object:
                return compareObject(prop1.object(), prop2.object());
            case Text:
                return compareText(prop1.text(), prop2.text());
            case Keyword:
                return compareKeyword(prop1.keyword(), prop2.keyword());
            case Date:
                return compareDate(prop1.date(), prop2.date());
            case GeoPoint:
                return compareGeoPoint(prop1.geoPoint(), prop2.geoPoint());
            case Ip:
                return compareIp(prop1.ip(), prop2.ip());
            case ScaledFloat:
                return compareScaledFloat(prop1.scaledFloat(), prop2.scaledFloat());
            case Completion:
                return compareCompletion(prop1.completion(), prop2.completion());
            case Flattened:
                return compareFlattened(prop1.flattened(), prop2.flattened());
            case DenseVector:
                return compareDenseVector(prop1.denseVector(), prop2.denseVector());
            case GeoShape:
                return compareGeoShape(prop1.geoShape(), prop2.geoShape());
            case Wildcard:
                return compareWildcard(prop1.wildcard(), prop2.wildcard());
            case Byte:
            case Short:
            case Float:
            case HalfFloat:
            case Double:
            case Integer:
            case Long:
                return compareNumberType((NumberPropertyBase) prop1._get(), (NumberPropertyBase) prop2._get());
            default:
                // 其他非常用类型 比较了key和type相同即认为相同 后续会随对应功能迭代继续完善
                return true;
        }
    }

    // 新增GeoShape和Wildcard的比较方法
    private static boolean compareGeoShape(GeoShapeProperty p1, GeoShapeProperty p2) {
        return Objects.equals(p1.ignoreMalformed(), p2.ignoreMalformed()) &&
                Objects.equals(p1.ignoreZValue(), p2.ignoreZValue()) &&
                Objects.equals(p1.coerce(), p2.coerce()) &&
                Objects.equals(p1.orientation(), p2.orientation());
    }

    private static boolean compareNumberType(NumberPropertyBase p1, NumberPropertyBase p2) {
        return Objects.equals(p1.docValues(), p2.docValues()) &&
                Objects.equals(p1.ignoreMalformed(), p2.ignoreMalformed());
    }

    private static boolean compareWildcard(WildcardProperty p1, WildcardProperty p2) {
        return Objects.equals(p1.ignoreAbove(), p2.ignoreAbove()) &&
                unorderedEquals(p1.copyTo(), p2.copyTo());
    }


    // 各类型详细比较逻辑
    private static boolean compareNested(NestedProperty p1, NestedProperty p2) {
        return Objects.equals(p1.dynamic(), p2.dynamic()) &&
                isPropertyMapEqual(p1.properties(), p2.properties());
    }

    private static boolean compareObject(ObjectProperty p1, ObjectProperty p2) {
        return Objects.equals(p1.dynamic(), p2.dynamic()) &&
                isPropertyMapEqual(p1.properties(), p2.properties());
    }

    private static boolean compareText(TextProperty p1, TextProperty p2) {
        return Objects.equals(p1.analyzer(), p2.analyzer()) &&
                Objects.equals(p1.searchAnalyzer(), p2.searchAnalyzer()) &&
                unorderedEquals(p1.copyTo(), p2.copyTo()) &&
                isPropertyMapEqual(p1.fields(), p2.fields());
    }

    private static boolean compareKeyword(KeywordProperty p1, KeywordProperty p2) {
        return Objects.equals(p1.ignoreAbove(), p2.ignoreAbove()) &&
                unorderedEquals(p1.copyTo(), p2.copyTo());
    }

    private static boolean compareDate(DateProperty p1, DateProperty p2) {
        return Objects.equals(p1.format(), p2.format()) &&
                Objects.equals(p1.ignoreMalformed(), p2.ignoreMalformed());
    }

    private static boolean compareGeoPoint(GeoPointProperty p1, GeoPointProperty p2) {
        return Objects.equals(p1.ignoreMalformed(), p2.ignoreMalformed()) &&
                Objects.equals(p1.ignoreZValue(), p2.ignoreZValue());
    }

    private static boolean compareIp(IpProperty p1, IpProperty p2) {
        return Objects.equals(p1.boost(), p2.boost()) &&
                Objects.equals(p1.docValues(), p2.docValues());
    }

    private static boolean compareScaledFloat(ScaledFloatNumberProperty p1,
                                              ScaledFloatNumberProperty p2) {
        return Objects.equals(p1.scalingFactor(), p2.scalingFactor()) &&
                Objects.equals(p1.coerce(), p2.coerce());
    }

    private static boolean compareCompletion(CompletionProperty p1,
                                             CompletionProperty p2) {
        return Objects.equals(p1.preserveSeparators(), p2.preserveSeparators()) &&
                Objects.equals(p1.preservePositionIncrements(),
                        p2.preservePositionIncrements());
    }

    private static boolean compareFlattened(FlattenedProperty p1,
                                            FlattenedProperty p2) {
        return Objects.equals(p1.boost(), p2.boost()) &&
                Objects.equals(p1.depthLimit(), p2.depthLimit());
    }


    private static boolean compareDenseVector(DenseVectorProperty p1,
                                              DenseVectorProperty p2) {
        return Objects.equals(p1.dims(), p2.dims()) &&
                Objects.equals(p1.similarity(), p2.similarity());
    }

    // 辅助方法：处理无序集合比较
    private static <T> boolean unorderedEquals(List<T> list1, List<T> list2) {
        if (list1 == list2) return true;
        if (list1 == null || list2 == null) return false;
        return new HashSet<>(list1).equals(new HashSet<>(list2));
    }

}

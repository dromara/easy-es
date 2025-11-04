package org.dromara.easyes.test.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.dromara.easyes.annotation.*;
import org.dromara.easyes.annotation.rely.*;
import org.dromara.easyes.common.join.BaseJoin;
import org.dromara.easyes.test.settings.MySettingsProvider;

import java.math.BigDecimal;
import java.util.List;

/**
 * es 数据模型 其中Join父子类型结构如下所示
 * <pre>
 *         Document
 *       /          \
 *    Comment       Author
 *                      \
 *                    Contact
 * </pre>
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@EqualsAndHashCode(callSuper = false)
@Data
@Accessors(chain = true)
@Settings(shardsNum = 3, replicasNum = 2, settingsProvider = MySettingsProvider.class)
@IndexName(value = "easyes_solon_document_8", keepGlobalPrefix = true, refreshPolicy = RefreshPolicy.IMMEDIATE)
@Join(nodes = {@Node(parentClass = Document.class, childClasses = {Author.class, Comment.class}), @Node(parentClass = Author.class, childClasses = Contact.class)})
public class Document extends BaseJoin {
    /**
     * es中的唯一id,字段名随便起,我这里演示用esId,你也可以用id(推荐),bizId等.
     * 如果你想自定义es中的id为你提供的id,比如MySQL中的id,请将注解中的type指定为customize或直接在全局配置文件中指定,如此id便支持任意数据类型)
     */
    @IndexId(type = IdType.CUSTOMIZE, writeToSource = true)
    private String esId;
    /**
     * 文档标题,不指定类型默认被创建为keyword类型,可进行精确查询
     */
    private String title;
    /**
     * 副标题
     */
    private String subTitle;
    /**
     * 文档内容,指定了类型及存储/查询分词器
     */
    @HighLight(mappingField = "highlightContent", fragmentSize = 10, numberOfFragments = 2, requireFieldMatch = false, preTag = "<em>", postTag = "</em>")
    @IndexField(fieldType = FieldType.TEXT, analyzer = Analyzer.IK_SMART, searchAnalyzer = Analyzer.IK_SMART)
    private String content;
    /**
     * 作者 加@TableField注解,并指明strategy = FieldStrategy.NOT_EMPTY 表示更新的时候的策略为 创建者不为空字符串时才更新
     */
    @IndexField(strategy = FieldStrategy.NOT_EMPTY, fieldType = FieldType.KEYWORD_TEXT, analyzer = Analyzer.IK_SMART)
    private String creator;
    /**
     * 可以聚合的text类型,字段名字随便取,注解中指定fieldData=true后text类型也可以支持聚合
     */
    @IndexField(fieldType = FieldType.TEXT, fieldData = true)
    private String filedData;
    /**
     * ip字段
     */
    @IndexField(fieldType = FieldType.IP)
    private String ipAddress;
    /**
     * 创建时间
     */
    @IndexField(fieldType = FieldType.DATE, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private String gmtCreate;
    /**
     * es中实际不存在的字段,但模型中加了,为了不和es映射,可以在此类型字段上加上 注解@TableField,并指明exist=false
     */
    @IndexField(exist = false)
    private String notExistsField;
    /**
     * 地理位置经纬度坐标 例如: "40.13933715136454,116.63441990026217"
     */
    @IndexField(fieldType = FieldType.GEO_POINT)
    private String location;
    /**
     * 图形(例如圆心,矩形)
     */
    @IndexField(fieldType = FieldType.GEO_SHAPE)
    private String geoLocation;
    /**
     * 自定义字段名称
     */
    @HighLight(preTag = "<nb>", postTag = "</nb>")
    @IndexField(value = "wu-la", fieldType = FieldType.TEXT, analyzer = Analyzer.IK_SMART, searchAnalyzer = Analyzer.IK_SMART)
    private String customField;

    /**
     * 忽略大小写字段，此字段的值用eq查询(termQuery)时，不区分大小写，值得注意的是es中keyword类型字段才支持忽略大小写
     */
    @IndexField(fieldType = FieldType.KEYWORD, ignoreCase = true)
    private String caseTest;

    /**
     * 高亮返回值被映射的字段
     */
    private String highlightContent;
    /**
     * 文档点赞数
     */
    private Integer starNum;
    /**
     * 此字段存null值,测试isNull时用
     */
    private String nullField;
    /**
     * 嵌套类型 注意,务必像下面示例一样指定类型为nested及其nested class,否则会导致框架无法正常运行
     */
    @IndexField(fieldType = FieldType.NESTED, nestedOrObjectClass = User.class)
    private List<User> users;

    /**
     * es返回的得分字段,字段名字随便取,只要加了@Score注解即可
     */
    @Score(decimalPlaces = 2)
    private Double score;
    /**
     * es返回的距离,字段名字随便取,距离单位以用户在序器中指定的为准,不指定es默认为:米
     */
    @Distance(decimalPlaces = 1)
    private Double distance;

    /**
     * es返回的距离2,有N个排序器就有N个排序返回的距离 参考cn.easyes.test.all.AllTest#testOrderByDistanceMulti
     */
    @Distance(decimalPlaces = 2)
    private Double distance2;

    /**
     * 浮点数,可指定缩放因子,不指定默认值为100
     */
    @IndexField(fieldType = FieldType.SCALED_FLOAT, scalingFactor = 101)
    private BigDecimal bigNum;

    /**
     * 复合字段,此注解和SpringData中的MultiField用法类似 适用于对同一个字段通过多种分词器检索的场景
     */
    @HighLight
    @MultiIndexField(mainIndexField = @IndexField(fieldType = FieldType.KEYWORD),
            otherIndexFields = {@InnerIndexField(suffix = "zh", fieldType = FieldType.TEXT, analyzer = Analyzer.IK_SMART),
                    @InnerIndexField(suffix = "pinyin", fieldType = FieldType.TEXT, analyzer = Analyzer.PINYIN)})
    private String multiField;
    /**
     * 英文名
     */
    private String english;

    /**
     * 稠密向量类型，dims 非负 最大为2048
     */
    @IndexField(fieldType = FieldType.DENSE_VECTOR, dims = 3)
    private double[] vector;
}

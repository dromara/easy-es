package cn.easyes.test.entity;

import cn.easyes.annotation.HighLight;
import cn.easyes.annotation.TableField;
import cn.easyes.annotation.TableId;
import cn.easyes.annotation.TableName;
import cn.easyes.common.constants.Analyzer;
import cn.easyes.common.enums.FieldStrategy;
import cn.easyes.common.enums.FieldType;
import cn.easyes.common.enums.IdType;
import cn.easyes.common.params.JoinField;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * es 数据模型
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Data
@Accessors(chain = true)
@TableName(shardsNum = 3, replicasNum = 2, keepGlobalPrefix = true, childClass = Comment.class)
public class Document {
    /**
     * es中的唯一id,如果你想自定义es中的id为你提供的id,比如MySQL中的id,请将注解中的type指定为customize或直接在全局配置文件中指定,如此id便支持任意数据类型)
     */
    @TableId(type = IdType.CUSTOMIZE)
    private String id;
    /**
     * 文档标题,不指定类型默认被创建为keyword类型,可进行精确查询
     */
    private String title;
    /**
     * 文档内容,指定了类型及存储/查询分词器
     */
    @HighLight(mappingField = "highlightContent",fragmentSize = 2)
    @TableField(fieldType = FieldType.TEXT, analyzer = Analyzer.IK_SMART, searchAnalyzer = Analyzer.IK_SMART)
    private String content;
    /**
     * 作者 加@TableField注解,并指明strategy = FieldStrategy.NOT_EMPTY 表示更新的时候的策略为 创建者不为空字符串时才更新
     */
    @TableField(strategy = FieldStrategy.NOT_EMPTY, fieldType = FieldType.KEYWORD_TEXT)
    private String creator;
    /**
     * 创建时间
     */
    @TableField(fieldType = FieldType.DATE, dateFormat = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
    private String gmtCreate;
    /**
     * es中实际不存在的字段,但模型中加了,为了不和es映射,可以在此类型字段上加上 注解@TableField,并指明exist=false
     */
    @TableField(exist = false)
    private String notExistsField;
    /**
     * 地理位置经纬度坐标 例如: "40.13933715136454,116.63441990026217"
     */
    @TableField(fieldType = FieldType.GEO_POINT)
    private String location;
    /**
     * 图形(例如圆心,矩形)
     */
    @TableField(fieldType = FieldType.GEO_SHAPE)
    private String geoLocation;
    /**
     * 自定义字段名称
     */
    @TableField(value = "wu-la", fieldType = FieldType.TEXT, analyzer = Analyzer.IK_SMART, searchAnalyzer = Analyzer.IK_SMART)
    private String customField;

    /**
     * 高亮返回值被映射的字段
     */
    private String highlightContent;
    /**
     * 文档点赞数
     */
    private Integer starNum;
    /**
     * 嵌套类型 注意,务必像下面示例一样指定类型为nested及其nested class,否则会导致框架无法正常运行
     */
    @TableField(fieldType = FieldType.NESTED, nestedClass = User.class)
    private List<User> users;

    /**
     * 父子类型 须通过注解在父文档及子文档的实体类中指明其类型为Join,及其父名称和子名称
     */
    @TableField(fieldType = FieldType.JOIN, parentName = "document", childName = "comment")
    private JoinField joinField;
}

> The field annotation @TableField  has the same function as MP, but compared with MP, some low-frequency usage functions have been castrated. According to user feedback, it can be gradually added with iterations. Up to now, the latest version has supported the following scenarios: 
> 
> 1. The field in the entity class is not the actual field in ES. For example, the entity class is directly used as DTO, and some extraneous fields that do not exist in ES are added. At this time, this field can be marked so that the EE framework can skip this Field, this field is not processed.
> 1. The update strategy of the field, for example, when the update interface is called, the field of the entity class is not updated until it is not Null or a non-empty string. At this time, you can add field annotations and mark the update strategy for the specified field.
> 1. Customize the name of the specified field. For example, the field is called wu-la in es, but it is called ula in the entity model. At this time, you can specify value="wu-la" in value. (version 0.9.8+ support).
> 1. In the automatic managed index mode, the index tokenizer and index field type can be specified (version 0.9.10+ support)
> 1. In the automatic managed index mode, you can specify the format format of the date in the index (version 0.9.11+ support)

Example of use:
```java
    public class Document {
    // Other fields are omitted here... 
        
    // Scenario 1: mark fields that do not exist in es
    @TableField(exist = false)
    private String notExistsField;
        
    // Scenario 2: When updating, the non-empty string in this field will be updated
    @TableField(strategy = FieldStrategy.NOT_EMPTY)
    private String creator;
    
    // Scenario 3: Custom field name    
    @TableField("wu-la")    
    private String ula;  
    
    // Scenario 4: Support the format type of the date field in the es index
    @TableField(fieldType = FieldType.DATE, dateFormat = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
    private String gmtCreate;

    // Scenario 5: Support the tokenizer type of the specified field in the es index
    @TableField(fieldType = FieldType.TEXT, analyzer = Analyzer.IK_SMART, searchAnalyzer = Analyzer.IK_MAX_WORD)
    private String content;
    }
```
> **Tips:**
> - There are 3 types of update strategies:
>    - NOT_NULL: Non-Null judgment, only when the field value is non-Null, it will be updated
>    - NOT_EMPTY: non-empty judgment, will be updated only when the field value is a non-empty string
>    - IGNORE: Ignore the judgment, no matter what the field value is, it will be updated
> - Priority: The update strategy specified in the field annotations> the update strategy specified in the global configuration


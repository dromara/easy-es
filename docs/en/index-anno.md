> - Index If the user does not configure or specify an annotation, the lowercase letters of the model name will be used as the index. For example, if the model is called Document, then the index will be document.
> - We also support specifying the index name according to the @TableName annotation.In order to maintain the same syntax as MP, the annotation naming here will temporarily retain @TableName, but it actually represents the index name.

Usage example: Assuming that my index name is: dev_document, then we can add this annotation to the model
```java
@TableName(value="daily_document",shardsNum = 3,replicasNum = 2) // 0.9.11 + version, the number of shards and replicas of the index can also be set through this annotation in the index automatic hosting mode
public class Document {
    ...
}
```
> Tips:
> - The index name specified by the annotation has the highest priority. If the annotation index is specified, the global configuration and automatically generated index will not take effect, and the index name specified in the annotation will be used. Priority order: Annotation index>Global configuration index prefix>Auto generated
> - The keepGlobalPrefix option, (only supported in version 0.9.4+), the default value is false, whether to keep using the global tablePrefix value:
>    - This annotation option will only take effect when the global tablePrefix is configured, the @TableName annotation and the value value are specified. If its value is true, the index name finally used by the framework is: global tablePrefix + the value of this annotation, for example: dev_document.
>    - The usage of this annotation tab is the same as in Mybatis-Plus.
> 


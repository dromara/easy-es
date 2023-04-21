- 高亮字段通过自定义注解@HighLight即可实现,将该注解添加在需要被高亮的字段上即可
```java
public class Document{
    /**
     * 需要被高亮的字段
     */
    @HighLight
    private String content;
    // 省略其它无关字段...
}
```
> **Tips:**
> - 如果你不想原来的字段值被高亮字段覆盖,那么你需要在@HighLight注解中指定mappingField,并将该字段添加至对应实体类中,这样配置以后,高亮内容在highlightContent字段中返回,原content字段的值依旧返回它本身的值.

例如:
```java
public class Document{
    /**
     * 需要被高亮的字段
     */
    @HighLight(mappingField = "highlightContent")
    private String content;
    /**
     * 高亮返回值被映射的字段
     */
    private String highlightContent;
    // 省略其它无关字段...
}
```
> **Tips:**
> - 高亮注解支持设置高亮返回内容截取的长度fragmentSize,默认值为100
> - 高亮注解支持设置高亮内容的标签,默认为<em></em>




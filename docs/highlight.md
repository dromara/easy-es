语法:
```java
// 不指定高亮标签,默认采用<em></em>返回高亮内容
highLight(高亮字段);
// 指定高亮标签
highLight(高亮字段,开始标签,结束标签)
```
```java
    @Test
    public void testHighlight() throws IOException {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String keyword = "过硬";
        wrapper.match(Document::getContent,keyword);
        wrapper.highLight(Document::getContent);
        SearchResponse response = documentMapper.search(wrapper);
        System.out.println(response);
    }
```
0.9.7+版本,根据用户反馈,对此写法做了进一步优化:

- 不需要再使用半原生查询即可完成查询
- 高亮返回的字段通过自定义注解@HighLightMappingField("content")在被查询实体类(Document)中来指定
```java
    @Test
    public void testHighlight() throws IOException {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String keyword = "过硬";
        wrapper.match(Document::getContent,keyword);
        wrapper.highLight(Document::getContent);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
```
```java
public class Document{
    /**
     * 高亮返回值被映射的字段
     */
    @HighLightMappingField("content")
    private String highlightContent;
}
```
> **Tips:**
> - 如果需要多字段高亮,则字段与字段之间可以用逗号隔开
> - 必须使用SearchResponse接收,否则返回体中无高亮字段 0.9.7+版本可通过自定义注解实现.





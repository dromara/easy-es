**syntax:**
```java
// Do not specify the highlight tag, and use <em>your highlight content</em> to return the highlighted content by default
highLight(highlightField);
// Specify highlight label
highLight(highlightField,startTag, endTag)
```
```java
    @Test
    public void testHighlight() throws IOException {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String keyword = "World";
        wrapper.match(Document::getContent,keyword);
        wrapper.highLight(Document::getContent);
        SearchResponse response = documentMapper.search(wrapper);
        System.out.println(response);
    }
```
> **Tips:**
> If you need to highlight multiple fields, you can separate the fields with commas
> Must use SearchResponse to receive, otherwise there is no highlighted field in the return body


Version 0.9.7+, according to user feedback, this writing method has been further optimized:
● No need to use semi-native query to complete query
● The field returned by highlighting is specified in the queried entity class (Document) through the custom annotation @HighLightMappingField("content")

```java
    @Test
    public void testHighlight() throws IOException {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String keyword = "hello";
        wrapper.match(Document::getContent,keyword);
        wrapper.highLight(Document::getContent);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
```
```java
public class Document{
    /**
     * Highlight the field whose return value is mapped
     */
    @HighLightMappingField("content")
    private String highlightContent;
}
```
> **Tips:**
> - If you need to highlight multiple fields, you can separate fields with commas
> - It must be received with SearchResponse, otherwise there is no highlighted field in the return body. Version 0.9.7+ can be implemented through custom annotations.


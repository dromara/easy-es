> For field sorting, ascending sorting and descending sorting are supported:

```java
// desc
wrapper.orderByDesc(Sort fields, support multiple fields)
// asc
wrapper.orderByAsc(Sort fields, support multiple fields)

// Sort by score (this function is supported in version 0.9.7+; the default descending order when SortOrder is not specified, the highest score is first, and ascending/descending order is supported)
wrapper.sortByScore(boolean condition,SortOrder sortOrder)
    
// The sorting input is passed in from the front end, and the string format is similar to that of MySQL before.
wrapper.orderBy(boolean condition, OrderByParam orderByParam);

// The sorting input is passed in from the front end, in the case of multiple fields
wrapper.orderBy(boolean condition, List<OrderByParam> orderByParams);   
```
Example of use:
```java
    @Test
    public void testSort(){
        // To test the sorting, we added a creation time field to the Document object, updated the index, and added two pieces of data
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.like(Document::getContent,"Hello");
        wrapper.select(Document::getTitle,Document::getGmtCreate);
        List<Document> before = documentMapper.selectList(wrapper);
        System.out.println("before:"+before);
        wrapper.orderByDesc(Document::getGmtCreate);
        List<Document> desc = documentMapper.selectList(wrapper);
        System.out.println("desc:"+desc);
    }
```
```java
    @Test
    public void testSortByScore(){
        // Tests are sorted by score in ascending order (low score first)
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent,"hello");
        wrapper.sortByScore(SortOrder.ASC);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
```
```java
    @Test
    public void testOrderByParams(){
        // Here, the simulation parameters are passed in by the front end through the xxQuery class, and the sorting is in descending order according to the title and ascending order according to the content.
        String jsonParam = "[{\"order\":\"title\",\"sort\":\"DESC\"},{\"order\":\"creator\",\"sort\":\"ASC\"}]";
        List<OrderByParam> orderByParams = JSON.parseArray(jsonParam, OrderByParam.class);
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent,"hello")
                .orderBy(orderByParams);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
```

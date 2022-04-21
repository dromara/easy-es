针对字段的排序,支持升序排序和降序排序:
```java
// 降序排列
wrapper.orderByDesc(排序字段,支持多字段)
    
// 升序排列
wrapper.orderByAsc(排序字段,支持多字段)
    
// 根据得分排序(此功能0.9.7+版本支持;不指定SortOrder时默认降序,得分高的在前,支持升序/降序)
wrapper.sortByScore(boolean condition,SortOrder sortOrder)
    
// 排序入参由前端传入, 字符串格式,有点类似之前MySQL那种
wrapper.orderBy(boolean condition, OrderByParam orderByParam);

// 排序入参由前端传入,多字段情形
wrapper.orderBy(boolean condition, List<OrderByParam> orderByParams);

```
使用示例:
```java
    @Test
    public void testSort(){
        // 测试排序 为了测试排序,我们在Document对象中新增了创建时间字段,更新了索引,并新增了两条数据
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.likeRight(Document::getContent,"推");
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
        // 测试根据得分升序排列(分数低的排前面)
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent,"技术");
        wrapper.sortByScore(SortOrder.ASC);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
```
```java
    @Test
    public void testOrderByParams(){
        // 此处模拟参数由前端通过xxQuery类传入,排序根据标题降序,根据内容升序
        String jsonParam = "[{\"order\":\"title\",\"sort\":\"DESC\"},{\"order\":\"creator\",\"sort\":\"ASC\"}]";
        List<OrderByParam> orderByParams = JSON.parseArray(jsonParam, OrderByParam.class);
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent,"技术")
                .orderBy(orderByParams);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
```

效果:
![image.png](https://iknow.hs.net/8730de70-29af-4279-9d40-43baa363a95b.png)
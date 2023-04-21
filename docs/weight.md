权重查询也是Es有MySQL无的一种查询,语法如下:
```java
function(字段, 值, Float 权重值)
```
```java
    @Test
    public void testWeight() throws IOException {
      	// 测试权重
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String keyword = "过硬";
        float contentBoost = 5.0f;
        wrapper.match(Document::getContent,keyword,contentBoost);
        String creator = "老汉";
        float creatorBoost = 2.0f;
        wrapper.eq(Document::getCreator,creator,creatorBoost);
        SearchResponse response = documentMapper.search(wrapper);
        System.out.println(response);
    }
```
> **Tips:**
> 如果你需要得分,则通过SearchResponse返回,如果不需要得分,只需要按照得分高的排名靠前返回,则直接用List<T>接收即可.


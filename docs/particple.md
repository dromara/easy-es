分词查询是ES独有的,MySQL中不支持的一种查询,也就是你可以根据关键词进行匹配,关于分词查询这里不多介绍,不会的同学请自行百度了解概念,这里只介绍用法:
```json
    @Test
    public void testMatch(){
        // 测试分词查询
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String keyword = "过硬";
        wrapper.match(Document::getContent,keyword);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
```

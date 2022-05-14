```java
    // Physical paging
    PageInfo<T> pageQuery(LambdaEsQueryWrapper<T> wrapper, Integer pageNum, Integer pageSize);
```
> **Tips:**
> - No need to integrate any plug-ins, you can use paging query, this query belongs to physical paging.
> - In some high-level syntax usage scenarios, there are currently known aggregate field returns, which our pager does not yet support. You need to encapsulate the paging by yourself. Other scenarios can basically be perfectly supported, and it is extremely easy to use.
> - Note that PageInfo is provided by this framework. If you already have the most popular open source paging plugin PageHelper in your project, please be careful not to introduce errors when importing the package. EE uses the same return fields as PageHelper, so you don't need to worry about it Additional work due to inconsistent field names.

> Example of use:

```java
    @Test
    public void testPageQuery() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getTitle, "hello");
        PageInfo<Document> documentPageInfo = documentMapper.pageQuery(wrapper,1,10);
        System.out.println(documentPageInfo);
    }
```


> The EE index module provides the following APIs for users to make convenient calls
> - indexName needs to be manually specified by the user
> - Object Wrapper is conditional constructor

```yaml
Boolean existsIndex(String indexName);
Boolean createIndex(LambdaEsIndexWrapper<T> wrapper);
Boolean updateIndex(LambdaEsIndexWrapper<T> wrapper);
Boolean deleteIndex(String indexName);
```
Please click the outline according to the name to enter the specific page to view the corresponding code of the interface

```java
	// Semi-native query
    SearchResponse search(LambdaEsQueryWrapper<T> wrapper) throws IOException;
	
	// Standard native query can specify RequestOptions
    SearchResponse search(SearchRequest searchRequest, RequestOptions requestOptions) throws IOException;
```
> **Tips:**
> - In some high-level syntax, such as specifying the highlighted field, if our return type is the entity object itself, but there is usually no highlighted field in the entity, the highlighted field cannot be received. At this time, RestClietn's native return object SearchResponse can be used.
> - Although EE covers most of the scenarios where we use ES, there may still be scenarios that are not covered. At this time, you can still query through the native grammar provided by RestClient and call the standard native query method. Both input and return are RestClient native


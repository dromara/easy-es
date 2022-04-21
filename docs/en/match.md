```java
match(R column, Object val)
match(boolean condition, R column, Object val)
```
> ● Word segment matching
> ● Example: match("content", "someone")--->content contains the keyword "someone". If the word segmentation granularity is set finer, someone may be split into "some" and "one". As long as the content contains "some" or "one", it can be searched out


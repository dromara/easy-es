```java
or()
or(boolean condition)
```
● Note for splicing OR: Actively calling or means that the next method is not connected with and! (If you don't call or, the default is to connect with and)<br />● Example: eq("Document::getId",1).or().eq(Document::getTitle,"Hello")--->id = 1 or title ='Hello'
```java
or(Consumer<Param> consumer)
or(boolean condition, Consumer<Param> consumer)
```
● OR nesting<br />● Example: or(i -> i.eq(Document::getTitle, "Hello").ne(Document::getCreator, "Guy"))--->or (title ='Hello' and status != 'Guy' )

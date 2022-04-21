```java
set(String column, Object val)
set(boolean condition, String column, Object val)
```
● SQL SET field<br />● Example: set(Document::getTitle, "new value")<br />● Example: set(Document::getTitle, "")--->database field value becomes an empty string<br />● Example: set(Document::getTitle, null)--->database field value becomes null

> The parameters of ES are actually encapsulated in a tree-like data structure, so there is no way for AND and OR in ES to be consistent with MySQL. In order to achieve a syntax that is almost the same as MP, the author has lost his mind. ...

> Fortunately, in the end, the grammar of 99% similarity is basically achieved, and there is still 1% that needs to be learned by users.

The correspondence table between MySQL and ES syntax is as follows
| MySQL | ES |
| ---- | ---- |
| and(condition1, condition2...) | must BoolQueryBuilder |
| or(condition1, condition2...) | should BoolQueryBuilder |
| or connection | should |


> AND introduction, AND in EE actually encapsulates multiple query conditions in AND brackets into a BoolQueryBuilder as a whole, and then the whole and other parameters are encapsulated in Must by default, and the function is similar to AND in MySQL (condition 1, condition 2 , condition 3...)
AND API
````java
and(Consumer<Param> consumer)
and(boolean condition, Consumer<Param> consumer)
````

- AND
- Example: and(i -> i.eq(Document::getTitle, "Hello").ne(Document::getCreator, "Guy"))--->and (title ='Hello' and creator != ' Guy' )

> OR introduction, OR in EE is the same as OR in MP, and supports 2 kinds, one is or(), which is used as a connector, and the other is or (condition 1, condition 2, condition 3).
- The first or(): used to reset the must conditions before and after the or() connector to the should query conditions
- The second or (condition 1, condition 2, condition 3...): It is used to encapsulate multiple query conditions in parentheses into a BoolQueryBuilder as a whole, and then the whole and other parameters are encapsulated in Should by default, with similar functions OR(Condition 1, Condition 2, Condition 3...) in MySQL
- The third special case is that the first or() connector appears in and(condition1.or().condition2...) or or(condition1.or().condition2...) , at this time, or() resets the must condition to the should condition, and the scope is limited to the parentheses, and the query conditions outside the parentheses are not affected.

````java
or()
or(boolean condition)
````

- Splicing OR** Notes: **Actively calling or means that the next **method** is not connected with and! (If you do not call or, the default is to use and connection)
- Example: eq("Document::getId",1).or().eq(Document::getTitle,"Hello")--->id = 1 or title ='Hello'

````java
or(Consumer<Param> consumer)
or(boolean condition, Consumer<Param> consumer)
````

- OR
- Example: or(i -> i.eq(Document::getTitle, "Hello").ne(Document::getCreator, "Guy"))--->or (title ='Hello' and status != ' Guy' )

- Special case
- Example: eq(Document::getTitle,"Hello")
     .and(i->i.eq(Document::getCreator,"Bob").or().eq(Document::getCreator,"Tom"))---> title="Hello" and(creator=" Bob" or creator="Tom")

---

In addition, some usage scenarios are as shown in the figure below. All query fields, query types, matching rules, etc. are not fixed and can be freely selected by the user. In this case, it will be very difficult to use the above syntax code. Write, you might as well use the queryStringQuery API to solve, use it to solve, the whole syntax is more like MySQL, and the flexibility and efficiency are very high.


>Preliminary knowledge learning: Before we officially enter the topic, let's first understand the ES index, because many novice do not understand ES index, so here is a brief introduction to the ES keyword type and text type, so as not to step on the pit, already understood You can skip this introduction directly.
The keyword type in ES is basically the same as the field in MySQL. When we need to perform exact matching, left fuzzy, right fuzzy, full fuzzy, sorting aggregation and other operations on the query field, the index type of the field needs to be keyword type. When we need to perform a word segmentation query on a field, we need the type of the field to be text type, and specify the tokenizer (use the ES default tokenizer if not specified, the effect is usually not ideal). When the same field, we need to treat it as When the keyword type is used, and it needs to be used as the text type, our index type is the keyword_text type. In EE, you can add the annotation @TableField(fieldType = FieldType.KEYWORD_TEXT) to the field, so that the field will be created as keyword The +text double type is shown in the figure below. It is worth noting that when we query the field as a keyword type, ES requires the incoming field name to be "field name.keyword". When the field is queried as a text type, Just use the original field name directly.


![image2](https://iknow.hs.net/72818af6-7cc3-4833-b7a7-dbff845ce73e.png)

It should also be noted that if the index type of a field is created to query only the keyword type (as shown in the figure below), you do not need to append .keyword to its name, and you can query it directly.

![image3](https://iknow.hs.net/87335e55-1fe3-44ed-920b-61354383e85a.png)

---

After the long-winded, officially entered the topic, queryStringQuery API:

````java
queryStringQuery(String queryString);
````

The queryString string is our query condition. We can use StringBuilder to splicing query fields and values ​​into the final query statement.
The above picture is taken as an example, I will demonstrate a scenario, please ignore the rationality of the scenario, because I chose it blindly: Suppose my query condition is: field: the creator is equal to Lao Wang, and the creator participle matches "next door" (for example: The old man next door, the old king next door), or the creator contains a big pig's hoof, the corresponding code is as follows:
````java
    @Test
    public void testQueryString() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        StringBuilder sb = new StringBuilder();
        sb.append("(")
                .append("(")
                .append("creator.keyword")
                .append(":")
                .append("Pharaoh")
                .append(")")
                .append("AND")
                .append("(")
                .append("creator")
                .append(":")
                .append("Next door")
                .append(")")
                .append(")")
                .append("OR")
                .append("(")
                .append("creator.keyword")
                .append(":")
                .append("*big pig's hoof*")
                .append(")");
        // The final splicing of sb is: ((creator.keyword: Pharaoh)AND(creator: next door))OR(creator.keyword:*big pig's hoof*), which can be said to be very similar to MySQL syntax
        wrapper.queryStringQuery(sb.toString());
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
````
Every time a line of query parameters is passed on the front-end parameter page, we append the corresponding parameters to sb and we are done. class, its full path is: QueryUtils
We refactor the above code using this utility class as follows:
````java
    @Test
    public void testQueryStringQueryMulti() {
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        String queryStr = QueryUtils.combine(Link.OR,
                QueryUtils.buildQueryString(Document::getCreator, "Pharaoh", Query.EQ, Link.AND),
                QueryUtils.buildQueryString(Document::getCreator, "Next door", Query.MATCH))
                + QueryUtils.buildQueryString(Document::getCreator, "*Big Pig's Hoof*", Query.EQ);
        wrapper.queryStringQuery(queryStr);
        List<Document> documents = documentMapper.selectList(wrapper);
        System.out.println(documents);
    }
````
Is it a lot more elegant? I have encapsulated the enumeration Query and Link for you, and you can use it directly. If you don't understand the meaning of the enumeration, you can click it directly to view it. I have detailed comments in the source code.
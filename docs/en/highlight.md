- The highlighted field can be achieved by custom annotation @HighLight, which can be added to the field that needs to be highlighted
````java
public class Document{
     /**
      * Fields that need to be highlighted
      */
     @HighLight
     private String content;
     // Omit other extraneous fields...
}
````
> **Tips:**
> - If you don't want the original field value to be overwritten by the highlighted field, then you need to specify the mappingField in the @HighLight annotation and add the field to the corresponding entity class, so that after configuration, the highlighted content is returned in the highlightContent field, The value of the original content field still returns its own value.

E.g:
````java
public class Document{
     /**
      * Fields that need to be highlighted
      */
     @HighLight(mappingField = "highlightContent")
     private String content;
     /**
      * Highlight the field whose return value is mapped
      */
     private String highlightContent;
     // Omit other extraneous fields...
}
````

> **Tips:**
> - Highlight annotation supports setting the length fragmentSize of the content intercepted by the highlight return, the default value is 100
> - Highlight annotation supports setting the label of highlighted content, the default is <em></em>
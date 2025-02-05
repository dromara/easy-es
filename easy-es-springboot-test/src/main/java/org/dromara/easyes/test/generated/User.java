package org.dromara.easyes.test.generated;

import lombok.Data;
import org.dromara.easyes.annotation.*;

import java.util.List;

@Data
public class User {
    // Fields
    private List<Faq> faqs;
    private String password;
    private String userName;
    private Integer age;
}

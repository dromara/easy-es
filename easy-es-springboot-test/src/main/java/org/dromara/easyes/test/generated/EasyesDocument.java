package org.dromara.easyes.test.generated;

import lombok.Data;
import org.dromara.easyes.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@IndexName("EasyesDocument")
@Settings(shardsNum = 3, replicasNum = 2)
@Data
public class EasyesDocument {
    // Fields
    private String authorName;
    private BigDecimal bigNum;
    private Date gmtCreate;
    private String caseTest;
    private String creator;
    private String nullField;
    private String address;
    private String geoLocation;
    private String subTitle;
    private String multiField;
    private String wula;
    private Integer starNum;
    private String ipAddress;
    private String title;
    private String content;
    private List<User> users;
    private String filedData;
    private String english;
    private String commentContent;
    private String location;
    private Object vector;
}

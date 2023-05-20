package com.zdjizhi.json.pojo;


import java.util.List;

/**
 * @author qidaijie
 * @Package com.zdjizhi.json.pojo
 * @Description:
 * @date 2023/5/2014:06
 */
public class UserList {
    private String name;
    private Integer age;
    private Long idcard;
    private List devicetag;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Long getIdcard() {
        return idcard;
    }

    public void setIdcard(Long idcard) {
        this.idcard = idcard;
    }

    public List getDevicetag() {
        return devicetag;
    }

    public void setDevicetag(List devicetag) {
        this.devicetag = devicetag;
    }

    public UserList(String name, Integer age, Long idcard, List devicetag) {
        this.name = name;
        this.age = age;
        this.idcard = idcard;
        this.devicetag = devicetag;
    }
}

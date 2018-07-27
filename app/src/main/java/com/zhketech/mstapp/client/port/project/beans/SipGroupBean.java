package com.zhketech.mstapp.client.port.project.beans;

import java.io.Serializable;


/**
 * sip group 分组展示 的实体类
 * <p>
 * Created by Root on 2018/5/25.
 */

public class SipGroupBean implements Serializable {

    private String flage;
    private int group_id;
    private String group_name;
    private int sip_count;

    @Override
    public String toString() {
        return "SipGroupBean{" +
                "flage='" + flage + '\'' +
                ", group_id=" + group_id +
                ", group_name='" + group_name + '\'' +
                ", sip_count=" + sip_count +
                '}';
    }

    public SipGroupBean() {
    }

    public SipGroupBean(String flage, int group_id, String group_name, int sip_count) {

        this.flage = flage;
        this.group_id = group_id;
        this.group_name = group_name;
        this.sip_count = sip_count;
    }

    public String getFlage() {

        return flage;
    }

    public void setFlage(String flage) {
        this.flage = flage;
    }

    public int getGroup_id() {
        return group_id;
    }

    public void setGroup_id(int group_id) {
        this.group_id = group_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public int getSip_count() {
        return sip_count;
    }

    public void setSip_count(int sip_count) {
        this.sip_count = sip_count;
    }
}

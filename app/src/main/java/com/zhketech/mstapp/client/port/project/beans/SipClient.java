package com.zhketech.mstapp.client.port.project.beans;

import java.io.Serializable;

/**
 * Created by Root on 2018/5/21.
 */

public class SipClient  implements Serializable {

    private String  usrname;
    private String description;
    private String dispname;
    private String addr;
    private String state;//0：unregistered 1:registered 2: ring 3:calling
    private String userAgent;


    @Override
    public String toString() {
        return "SipClient{" +
                "usrname='" + usrname + '\'' +
                ", description='" + description + '\'' +
                ", dispname='" + dispname + '\'' +
                ", addr='" + addr + '\'' +
                ", state='" + state + '\'' +
                ", userAgent='" + userAgent + '\'' +
                '}';
    }

    public String getUsrname() {
        return usrname;
    }

    public void setUsrname(String usrname) {
        this.usrname = usrname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDispname() {
        return dispname;
    }

    public void setDispname(String dispname) {
        this.dispname = dispname;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public SipClient() {

    }

    public SipClient(String usrname, String description, String dispname, String addr, String state, String userAgent) {
        this.usrname = usrname;
        this.description = description;
        this.dispname = dispname;
        this.addr = addr;
        this.state = state;
        this.userAgent = userAgent;
    }
}

package com.zhketech.mstapp.client.port.project.beans;

/**
 * Created by Root on 2018/6/15.
 *
 * 登录服务时的全部参数
 *
 */

public class LoginParameters {
    String username;    //用户名
    String pass;        //密码
    String native_ip;   //本机ip
    String server_ip;   //服务器ip

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getNative_ip() {
        return native_ip;
    }

    public void setNative_ip(String native_ip) {
        this.native_ip = native_ip;
    }

    public String getServer_ip() {
        return server_ip;
    }

    public void setServer_ip(String server_ip) {
        this.server_ip = server_ip;
    }

    public LoginParameters() {
    }
}

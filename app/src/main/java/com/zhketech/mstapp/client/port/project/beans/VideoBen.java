package com.zhketech.mstapp.client.port.project.beans;

import java.io.Serializable;

public class VideoBen implements Serializable {

    private String flage;
    private String id;
    private String name;
    private String devicetype;
    private String ip;
    private String port;
    private String channel;
    private String username;
    private String password;
    private String rtsp;
    private  String ptz_url;
    private String token;


    public VideoBen() {
    }


    @Override
    public String toString() {
        return "VideoBen{" +
                "flage='" + flage + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", devicetype='" + devicetype + '\'' +
                ", ip='" + ip + '\'' +
                ", port_icon='" + port + '\'' +
                ", channel='" + channel + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", rtsp='" + rtsp + '\'' +
                ", ptz_url='" + ptz_url + '\'' +
                ", token='" + token + '\'' +
                '}';
    }

    public String getFlage() {
        return flage;
    }

    public void setFlage(String flage) {
        this.flage = flage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDevicetype() {
        return devicetype;
    }

    public void setDevicetype(String devicetype) {
        this.devicetype = devicetype;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRtsp() {
        return rtsp;
    }

    public void setRtsp(String rtsp) {
        this.rtsp = rtsp;
    }


    public String getPtz_url() {
        return ptz_url;
    }

    public void setPtz_url(String ptz_url) {
        this.ptz_url = ptz_url;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public VideoBen(String flage, String id, String name, String devicetype, String ip, String port, String channel, String username, String password, String rtsp, boolean isSuporrtPtz, String ptz_url, String token) {

        this.flage = flage;
        this.id = id;
        this.name = name;
        this.devicetype = devicetype;
        this.ip = ip;
        this.port = port;
        this.channel = channel;
        this.username = username;
        this.password = password;
        this.rtsp = rtsp;
        this.ptz_url = ptz_url;
        this.token = token;
    }
}

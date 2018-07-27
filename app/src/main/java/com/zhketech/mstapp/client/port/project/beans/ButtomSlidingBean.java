package com.zhketech.mstapp.client.port.project.beans;

import java.io.Serializable;

/**
 * Created by Root on 2018/7/9.
 */

public class ButtomSlidingBean implements Serializable {
    private String name;

    @Override
    public String toString() {
        return "ButtomSlidingBean{" +
                "name='" + name + '\'' +
                '}';
    }

    public ButtomSlidingBean() {
    }

    public ButtomSlidingBean(String name) {

        this.name = name;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

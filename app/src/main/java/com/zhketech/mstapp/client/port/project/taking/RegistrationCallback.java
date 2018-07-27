package com.zhketech.mstapp.client.port.project.taking;

/**
 * 注册状态的回调
 */

public abstract class RegistrationCallback {


    public void registrationNone() {}

    public void registrationProgress() {}

    public void registrationOk() {}

    public void registrationCleared() {}

    public void registrationFailed() {}
}

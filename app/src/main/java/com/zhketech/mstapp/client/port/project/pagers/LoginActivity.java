package com.zhketech.mstapp.client.port.project.pagers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.zhketech.mstapp.client.port.project.R;
import com.zhketech.mstapp.client.port.project.base.App;
import com.zhketech.mstapp.client.port.project.base.BaseActivity;
import com.zhketech.mstapp.client.port.project.beans.LoginParameters;
import com.zhketech.mstapp.client.port.project.callbacks.BatteryAndWifiService;
import com.zhketech.mstapp.client.port.project.callbacks.LoginIntoServerThread;
import com.zhketech.mstapp.client.port.project.utils.Logutils;
import com.zhketech.mstapp.client.port.project.utils.CpuAndRam;
import com.zhketech.mstapp.client.port.project.utils.PhoneUtils;
import com.zhketech.mstapp.client.port.project.utils.SharedPreferencesUtils;
import com.zhketech.mstapp.client.port.project.utils.WriteLogToFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity {


    //用户名
    @BindView(R.id.edit_username_layout)
    EditText userName;
    //密码
    @BindView(R.id.edit_userpass_layout)
    EditText userPwd;
    //记住密码Checkbox
    @BindView(R.id.remember_pass_layout)
    Checkable rememberPwd;
    //自动登录CheckBox
    @BindView(R.id.auto_login_layout)
    Checkable autoLoginCheckBox;
    //服务器
    @BindView(R.id.edit_serviceip_layout)
    EditText serverIp;
    //updateServerIpCheckBox
    @BindView(R.id.remembe_serverip_layout)
    CheckBox updateServerIpCheckBox;

    boolean isRemember;
    boolean isAuto;

    //整个项目可能用到的权限
    String[] permissions = new String[]{
            Manifest.permission.USE_SIP,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    //存放未授权的权限
    List<String> mPermissionList = new ArrayList<>();


    @Override
    public int intiLayout() {
        return R.layout.activity_login;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        verifyPermissions();

    }

    @Override
    public void initData() {
        String nativeIP = PhoneUtils.displayIpAddress(LoginActivity.this);
        if (!TextUtils.isEmpty(nativeIP)) {
            SharedPreferencesUtils.putObject(LoginActivity.this, "nativeIp", nativeIP);
        } else {
            SharedPreferencesUtils.putObject(LoginActivity.this, "nativeIp", "127.0.0.1");
        }
        startService(new Intent(this, BatteryAndWifiService.class));


        String logined_name = (String) SharedPreferencesUtils.getObject(App.getInstance(), "username", "");
        String logined_pass = (String) SharedPreferencesUtils.getObject(App.getInstance(), "userpass", "");
        String logined_serverip = (String) SharedPreferencesUtils.getObject(App.getInstance(), "serverip", "");
        boolean p = (boolean) SharedPreferencesUtils.getObject(App.getInstance(), "rememberinfor", false);
        //输入框显示用户名和密码
        userName.setText(logined_name);
        userPwd.setText(logined_pass);
        serverIp.setText(logined_serverip);
        if (p) serverIp.setEnabled(false);

        updateServerIpCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    serverIp.setEnabled(true);
                } else {
                    serverIp.setEnabled(false);
                }
            }
        });


    }
//, R.id.userlogin_button_cancel_layout
    @OnClick({R.id.userlogin_button_layout})
    public void loginOrCancel(View view) {
        switch (view.getId()) {
            case R.id.userlogin_button_layout:
                loginMethod();
                //  LoginToCMS();
                break;
//            case R.id.userlogin_button_cancel_layout:
//                LoginCancel();
//                break;
        }
    }

    private void LoginCancel() {
        startActivity(new Intent(LoginActivity.this, ChannelListActivity.class));
    }

    private void loginMethod() {

        final String name = userName.getText().toString().trim();
        final String pass = userPwd.getText().toString().trim();
        final String server_IP = serverIp.getText().toString().trim();
        isRemember = rememberPwd.isChecked();
        isAuto = autoLoginCheckBox.isChecked();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(server_IP)) {
            LoginParameters loginParameters = new LoginParameters();
            loginParameters.setUsername(name);
            loginParameters.setPass(pass);
            loginParameters.setServer_ip(server_IP);
            String localIp = (String) SharedPreferencesUtils.getObject(LoginActivity.this, "nativeIp", "");
            if (TextUtils.isEmpty(localIp)) {
                Logutils.e("Local_Ip is empty !!!");
                return;
            }
            loginParameters.setNative_ip(localIp);
            LoginIntoServerThread loginThread = new LoginIntoServerThread(LoginActivity.this, loginParameters, new LoginIntoServerThread.IsLoginListern() {
                @Override
                public void loginStatus(final String status) {
                    if (!TextUtils.isEmpty(status)) {
                        String result = status;
                        Logutils.i(result);
                        if (status.equals("success")) {
                            if (isRemember == true) {
                                SharedPreferencesUtils.putObject(App.getInstance(), "rememberinfor", true);
                                SharedPreferencesUtils.putObject(App.getInstance(), "serverip", server_IP);
                                SharedPreferencesUtils.putObject(App.getInstance(), "username", name);
                                SharedPreferencesUtils.putObject(App.getInstance(), "userpass", pass);
                            }
                            if (isAuto) {
                                SharedPreferencesUtils.putObject(App.getInstance(), "auto", true);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loginToCMS();
                                }
                            });
                        } else if (status.contains("fail")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    toastShort("Login Filed !!\n Please enter the correct informatiom !!!"+status);
                                }
                            });
                        }
                    }
                }
            });
            loginThread.start();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LoginActivity.this, "Can not be empty !!!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loginToCMS() {
        CpuAndRam.getInstance().init(getApplicationContext(), 5 * 1000L);
        CpuAndRam.getInstance().start();
        getLocation();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        LoginActivity.this.finish();
        WriteLogToFile.info("成功登录：" + new Date().toString());
    }

    /**
     * 权限申请
     */
    private void verifyPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission();
        } else {
            initThisPageData();
        }
    }

    private void initThisPageData() {
        Log.i("TAG", "can't request permission !!!");
    }

    /**
     * 申请权限
     */
    private void requestPermission() {
        mPermissionList.clear();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(LoginActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);
            }
        } /** * 判断存储委授予权限的集合是否为空 */
        if (!mPermissionList.isEmpty()) {
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            ActivityCompat.requestPermissions(LoginActivity.this, permissions, 1);
        } else {
            //未授予的权限为空，表示都授予了 // 后续操作...
            Log.i("TAG", "权限已全部给予");
            initThisPageData();
        }
    }

    boolean mShowRequestPermission = true;//用户是否禁止权限

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        //判断是否勾选禁止后不再询问
                        boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissions[i]);
                        if (showRequestPermission) {//
                            requestPermission();//重新申请权限
                            return;
                        } else {
                            mShowRequestPermission = false;//已经禁止
                            String permisson = permissions[i];
                            android.util.Log.w("TAG", "permisson:" + permisson);
                        }
                    }
                }
                initThisPageData();
                break;
            default:
                break;
        }
    }


    public void openPermissionPager(View view) {
        String sdk = android.os.Build.VERSION.SDK; // SDK号
        String model = android.os.Build.MODEL; // 手机型号
        String release = android.os.Build.VERSION.RELEASE; // android系统版本号
        String brand = Build.BRAND;//手机厂商

        Log.i("TAG", "");
    }


    @SuppressLint("MissingPermission")
    public void getLocation() {
        LocationManager mLocationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);

        if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5 * 60 * 1000, 1, mLocationListener);
        }

    }

    LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            double lat = location.getLatitude();
            double log = location.getLongitude();
            Logutils.i("Lat:" + lat + "\n" + log);
            SharedPreferencesUtils.putObject(LoginActivity.this, "lat", lat + "");
            SharedPreferencesUtils.putObject(LoginActivity.this, "long", log + "");

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };


}

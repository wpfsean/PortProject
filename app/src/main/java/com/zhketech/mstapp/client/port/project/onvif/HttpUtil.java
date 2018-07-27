package com.zhketech.mstapp.client.port.project.onvif;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HttpUtil {

    public static String getRequest(String url){



        return "";
    }

    /**
     * POST 请求
     */
    public static String postRequest(String baseUrl, String params) throws Exception {
        String receive = "";
        // 新建一个URL对象
        URL url = new URL(baseUrl);
        // 打开一个HttpURLConnection连接
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        //设置请求允许输入 默认是true
        urlConn.setDoInput(true);
        // Post请求必须设置允许输出 默认false
        urlConn.setDoOutput(true);
        // 设置为Post请求
        urlConn.setRequestMethod("POST");
        // Post请求不能使用缓存
        urlConn.setUseCaches(false);
        //设置本次连接是否自动处理重定向
        urlConn.setInstanceFollowRedirects(true);
        // 配置请求Content-Type,application/soap+xml
        urlConn.setRequestProperty("Content-Type",
                "application/soap+xml;charset=utf-8");
        // 开始连接
        urlConn.connect();
        // 发送请求数据
        urlConn.getOutputStream().write(params.getBytes());
        // 判断请求是否成功
        if (urlConn.getResponseCode() == 200) {
            // 获取返回的数据
            InputStream is = urlConn.getInputStream();
            byte[] data = new byte[1024];
            int n;
            while ((n = is.read(data)) != -1) {
                receive = receive + new String(data, 0, n);
            }
        } else {
            throw new Exception("ResponseCodeError : " + urlConn.getResponseCode());
        }
        // 关闭连接
        urlConn.disconnect();
        return receive;
    }

    /**
     *  ip 是否可以Ping通
     * @param ip
     * @return
     */
    public static boolean isIpReachable(String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            if (addr.isReachable(3000)) {
                return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }


    /**
     * 获取字符串中的ip
     * @param baseUrl
     * @return
     */
    public static String getIpFromBaseUrl(String baseUrl) {
        String regEx = "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(baseUrl);
        while (m.find()) {
            String result = m.group();
            return result;
        }

        return "";
    }

}

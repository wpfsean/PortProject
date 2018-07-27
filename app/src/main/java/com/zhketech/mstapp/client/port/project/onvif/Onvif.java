package com.zhketech.mstapp.client.port.project.onvif;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Xml;


import com.zhketech.mstapp.client.port.project.global.AppConfig;

import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * 解析Rtsp
 *
 * Created by Root on 2018/7/2.
 * <p>
 * 观自在菩萨，行深般若波罗蜜多时，照见五蕴皆空，度一切苦厄。
 * 舍利子，色不异空，空不异色，色即是空，空即是色，受想行识亦复如是。
 * 舍利子，是诸法空相，不生不灭，不垢不净，不增不减。是故空中无色，无受想行识，无眼耳鼻舌身意，无色声香味触法，无眼界乃至无意识界，无无明亦无无明尽，乃至无老死，亦无老死尽，无苦集灭道，无智亦无得。
 * 以无所得故，菩提萨埵，依般若波罗蜜多故，心无挂碍；无挂碍故，无有恐怖，远离颠倒梦想，究竟涅槃。
 * 三世诸佛，依般若波罗蜜多故，得阿耨多罗三藐三菩提。
 * 故知般若波罗蜜多，是大神咒，是大明咒，是无上咒，是无等等咒，能除一切苦，真实不虚。
 * 故说般若波罗蜜多咒，即说咒曰：揭谛揭谛，波罗揭谛，波罗僧揭谛，菩提萨婆诃。
 */

public class Onvif implements Runnable {

    String getServices = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:tds=\"http://www.onvif.org/ver10/device/wsdl\" xmlns:tt=\"http://www.onvif.org/ver10/schema\">\n" +
            "  <s:Header xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
            "    <wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n" +
            "      <wsse:UsernameToken>\n" +
            "        <wsse:Username>%s</wsse:Username>\n" +
            "        <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">%s</wsse:Password>\n" +
            "        <wsse:Nonce>%s</wsse:Nonce>\n" +
            "        <wsu:Created>%s</wsu:Created>\n" +
            "      </wsse:UsernameToken>\n" +
            "    </wsse:Security>\n" +
            "  </s:Header>\n" +
            "  <soap:Body>\n" +
            "    <tds:GetServices>\n" +
            "      <tds:IncludeCapability>false</tds:IncludeCapability>\n" +
            "    </tds:GetServices>\n" +
            "  </soap:Body>\n" +
            "</soap:Envelope>";

    String getCapabilities = "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\"><s:Header><Security s:mustUnderstand=\"1\" xmlns=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\"><UsernameToken><Username>%s</Username><Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">%s</Password><Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">%s</Nonce><Created xmlns=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">%s</Created></UsernameToken></Security></s:Header><s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><GetCapabilities xmlns=\"http://www.onvif.org/ver10/device/wsdl\"><Category>All</Category></GetCapabilities></s:Body></s:Envelope>";

    String GET_PROFILES = "<?xml version=\"1.0\" encoding=\"utf-8\"?><s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\"><s:Header><wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\"><wsse:UsernameToken><wsse:Username>%s</wsse:Username><wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">%s</wsse:Password><wsse:Nonce>%s</wsse:Nonce><wsu:Created>%s</wsu:Created></wsse:UsernameToken></wsse:Security></s:Header><s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><GetProfiles xmlns=\"http://www.onvif.org/ver10/media/wsdl\"></GetProfiles></s:Body></s:Envelope>";

    String GET_URI = "<?xml version=\"1.0\" encoding=\"utf-8\"?><s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\"><s:Header><wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\"><wsse:UsernameToken><wsse:Username>%s</wsse:Username><wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">%s</wsse:Password><wsse:Nonce>%s</wsse:Nonce><wsu:Created>%s</wsu:Created></wsse:UsernameToken></wsse:Security></s:Header><s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><GetStreamUri xmlns=\"http://www.onvif.org/ver10/media/wsdl\"><StreamSetup><Stream xmlns=\"http://www.onvif.org/ver10/schema\">RTP-Unicast</Stream><Transport xmlns=\"http://www.onvif.org/ver10/schema\"><Protocol>RTSP</Protocol></Transport></StreamSetup><ProfileToken>%s</ProfileToken></GetStreamUri></s:Body></s:Envelope>";

    Device device;
    private String mCreated, mNonce, mAuthPwd;

    GetRtspCallback listern;

    public Onvif(Device device, GetRtspCallback listern) {
        this.device = device;
        this.listern = listern;
        createAuthString();
    }

    //post请求
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

    //生成必要的参数
    private void createAuthString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
                Locale.CHINA);
        mCreated = df.format(new Date());
        mNonce = getNonce();
        mAuthPwd = getPasswordEncode(mNonce, device.getVideoBen().getPassword(), mCreated);
    }

    //密码加密
    public String getPasswordEncode(String nonce, String password, String date) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            // 从官方文档可以知道我们nonce还需要用Base64解码一次
            byte[] b1 = Base64.decode(nonce.getBytes(), Base64.DEFAULT);
            // 生成字符字节流
            byte[] b2 = date.getBytes(); // "2013-09-17T09:13:35Z";
            byte[] b3 = password.getBytes();
            // 根据我们传得值的长度生成流的长度
            byte[] b4 = new byte[b1.length + b2.length + b3.length];
            // 利用sha-1加密字符
            md.update(b1, 0, b1.length);
            md.update(b2, 0, b2.length);
            md.update(b3, 0, b3.length);
            // 生成sha-1加密后的流
            b4 = md.digest();
            // 生成最终的加密字符串
            String result = new String(Base64.encode(b4, Base64.DEFAULT));
            return result.replace("\n", "");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    //随机数据
    public String getNonce() {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 24; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    @Override
    public void run() {
        synchronized ("") {
            try {
                String ip = HttpUtil.getIpFromBaseUrl(device.getServiceUrl());
                boolean isConnect = HttpUtil.isIpReachable(ip);
                if (!isConnect) {
                    listern.getDeviceInfoResult("Ip of this video cannot be pinged", false,device);
                    return;
                }
                String parms = String.format(getServices, device.getVideoBen().getUsername(), mAuthPwd, mNonce, mCreated);
                String result = postRequest(device.getServiceUrl(), parms);
                if (TextUtils.isEmpty(result)) {
                    parms = String.format(getCapabilities,  device.getVideoBen().getUsername(), mAuthPwd, mNonce, mCreated);
                    result = postRequest(device.getServiceUrl(), parms);
                    if (TextUtils.isEmpty(result)) return;
                }
                //解析mediaUrl和ptzUrl
                resolveMediaUrlByXml(result);
                String mediaUrl = device.getMediaUrl();
                parms = String.format(GET_PROFILES,
                        device.getVideoBen().getUsername(), mAuthPwd, mNonce, mCreated);
                result = postRequest(mediaUrl, parms);
                if (TextUtils.isEmpty(result)) {
                    listern.getDeviceInfoResult("Did not get the xml with token information", false,device);
                    return;
                }
                device.addProfiles(getMediaProfiles(result));
                String channel = device.getVideoBen().getChannel();
                int position = Integer.parseInt(channel);
                int p = -1;
                if (AppConfig.isMainStream) {
                    p = 2 * position - 2;
                } else {
                    p = 2 * position - 1;
                }
                String profile = device.getProfiles().get(p).getToken();
                device.getVideoBen().setToken(profile);
                parms = String.format(GET_URI,  device.getVideoBen().getUsername(),
                        mAuthPwd, mNonce, mCreated, profile);
                result = postRequest(mediaUrl, parms);

                if (TextUtils.isEmpty(result)) {
                    listern.getDeviceInfoResult("Did not get the xml with Rtsp information", false,device);
                    return;
                }
                String rtsp = getRtspByXml(result);
                String newUrl = "";
                if (!TextUtils.isEmpty(rtsp)) {
                    if (!rtsp.contains("@")) {
                        String[] flage = rtsp.split("//");
                        String header = flage[0];
                        String footer = flage[1];
                        newUrl = header + "//" + device.getVideoBen().getUsername() + ":" + device.getVideoBen().getPassword() + "@" + footer;
                        device.setRtspUrl(newUrl);
                    }
                }
                listern.getDeviceInfoResult(newUrl, true,device);
            } catch (Exception e) {
                String error = "Execption:"+e.getMessage();
                listern.getDeviceInfoResult(error, false,device);
            }
        }
    }

    //解析mediaUrl和ptzUrl
    public String resolveMediaUrlByXml(String xml) {
        XmlPullParser parser = Xml.newPullParser();
        InputStream input = new ByteArrayInputStream(xml.getBytes());
        try {
            parser.setInput(input, "UTF-8");
            int eventType = parser.getEventType();
            boolean done = false;
            while (eventType != XmlPullParser.END_DOCUMENT || done) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        String node = parser.getName();
                        if (node.equals("Service")) {
                            eventType = parser.next();
                            String url = parser.nextText();
                            if (url.equals("http://www.onvif.org/ver20/analytics/wsdl")) {
                                eventType = parser.next();
                                String analyticsUrl = parser.nextText();
                                if (!TextUtils.isEmpty(analyticsUrl))
                                    device.setAnalyticsUrl(analyticsUrl);
                            }
                            if (url.equals("http://www.onvif.org/ver20/imaging/wsdl")) {
                                eventType = parser.next();
                                String imagingUrl = parser.nextText();
                                if (!TextUtils.isEmpty(imagingUrl))
                                    device.setImageUrl(imagingUrl);
                            }
                            if (url.equals("http://www.onvif.org/ver10/media/wsdl")) {
                                eventType = parser.next();
                                String mediaUrl = parser.nextText();
                                if (!TextUtils.isEmpty(mediaUrl))
                                    device.setMediaUrl(mediaUrl);
                            }
                            if (url.equals("http://www.onvif.org/ver10/events/wsdl")) {
                                eventType = parser.next();
                                String eventsUrl = parser.nextText();
                                if (!TextUtils.isEmpty(eventsUrl))
                                    device.setEventUrl(eventsUrl);
                            }
                            if (url.equals("http://www.onvif.org/ver20/ptz/wsdl")) {
                                eventType = parser.next();
                                String ptzUrl = parser.nextText();
                                if (!TextUtils.isEmpty(ptzUrl))
                                    device.setPtzUrl(ptzUrl);
                                    device.getVideoBen().setPtz_url(ptzUrl);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    //获取所有的token及视频信息
    public ArrayList<MediaProfile> getMediaProfiles(String xml) throws Exception {
        //初始化XmlPullParser
        XmlPullParser parser = Xml.newPullParser();
        //
        ArrayList<MediaProfile> profiles = new ArrayList<>();
        MediaProfile profile = null;
        //tag 用来判断当前解析Video还是Audio
        String tag = "";
        InputStream input = new ByteArrayInputStream(xml.getBytes());
        parser.setInput(input, "UTF-8");
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    //serviceUrl
                    if (parser.getName().equals("Profiles")) {
                        profile = new MediaProfile();
                        //获取token
                        profile.setToken(parser.getAttributeValue(1));
                        parser.next();
                        //获取name
                        if (parser.getName() != null && parser.getName().equals("Name")) {
                            profile.setName(parser.nextText());
                        }
                    } else if (parser.getName().equals("VideoEncoderConfiguration") && profile != null) {
                        //获取VideoEncode Token
                        int count = parser.getAttributeCount();
                        if (count == 2){
                            profile.getVideoEncode().setToken(parser.getAttributeValue(1));
                        }else if (count == 1){
                            profile.getVideoEncode().setToken(parser.getAttributeValue(0));
                        }

                        tag = "Video";
                    } else if (parser.getName().equals("AudioEncoderConfiguration") && profile != null) {
                        //获取AudioEncode Token
                        int count = parser.getAttributeCount();
                        if (count == 2){
                            profile.getAudioEncode().setToken(parser.getAttributeValue(1));
                        }else if (count == 1){
                            profile.getAudioEncode().setToken(parser.getAttributeValue(0));
                        }

                        tag = "Audio";
                    } else if (parser.getName().equals("Width") && profile != null) {
                        //分辨率宽
                        String text = parser.nextText();
                        if (tag.equals("Video")) {
                            profile.getVideoEncode().setWidth(Integer.parseInt(text));
                        }
                    } else if (parser.getName().equals("Height") && profile != null) {
                        //分辨率高
                        String text = parser.nextText();
                        if (tag.equals("Video")) {
                            profile.getVideoEncode().setHeight(Integer.parseInt(text));
                        }
                    } else if (parser.getName().equals("FrameRateLimit") && profile != null) {
                        //帧率
                        String text = parser.nextText();
                        if (tag.equals("Video")) {
                            profile.getVideoEncode().setFrameRate(Integer.parseInt(text));
                        }
                    } else if (parser.getName().equals("Encoding") && profile != null) {
                        //编码格式
                        String text = parser.nextText();
                        if (tag.equals("Video")) {
                            profile.getVideoEncode().setEncoding(text);
                        } else if (tag.equals("Audio")) {
                            profile.getAudioEncode().setEncoding(text);
                        }
                    } else if (parser.getName().equals("Bitrate") && profile != null) {
                        //Bitrate
                        String text = parser.nextText();
                        if (tag.equals("Audio")) {
                            profile.getAudioEncode().setBitrate(Integer.parseInt(text));
                        }
                    } else if (parser.getName().equals("SampleRate") && profile != null) {
                        //SampleRate
                        String text = parser.nextText();
                        if (tag.equals("Audio")) {
                            profile.getAudioEncode().setSampleRate(Integer.parseInt(text));
                        }
                    } else if (parser.getName().equals("PTZConfiguration") && profile != null) {
                        //获取VideoEncode Token
                        profile.getPtzConfiguration().setToken(parser.getAttributeValue(0));
                        tag = "Ptz";
                    } else if (parser.getName().equals("NodeToken") && profile != null) {
                        //NodeToken
                        String text = parser.nextText();
                        if (tag.equals("Ptz")) {
                            profile.getPtzConfiguration().setNodeToken(text);
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (parser.getName().equals("Profiles")) {
                        profiles.add(profile);
                    }
                    if (parser.getName().equals("AudioEncoderConfiguration")
                            || parser.getName().equals("VideoEncoderConfiguration") || parser.getName().equals("PTZConfiguration")) {
                        tag = "";
                    }
                    break;
                default:
                    break;
            }
            eventType = parser.next();
        }

        return profiles;
    }

    //解析rtsp地址
    public String getRtspByXml(String xml) {
        XmlPullParser parser = Xml.newPullParser();
        InputStream input = new ByteArrayInputStream(xml.getBytes());
        try {
            parser.setInput(input, "UTF-8");
            int eventType = parser.getEventType();
            boolean done = false;
            while (eventType != XmlPullParser.END_DOCUMENT || done) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals("Uri")) {
                            eventType = parser.next();
                            return parser.getText();
                        }
                        break;
                    case XmlPullParser.END_TAG:

                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public interface GetRtspCallback {
        void getDeviceInfoResult(String rtsp, boolean isSuccess, Device device);
    }


}

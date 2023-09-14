package com.keyvalues.optaplanner.maprouting.utils;

import com.alibaba.fastjson.JSONObject;
import com.keyvalues.optaplanner.geo.Point;
import com.keyvalues.optaplanner.utils.Md5Util;
import com.keyvalues.optaplanner.utils.RedisUtil;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class BaiduMapUtil {

    @Value("${baidu-map.key}")
    private String key;
    @Value("${proxy.host:}")
    private String proxyHost;
    @Value("${proxy.port:8080}")
    private Integer proxyPort;
    @Value("${proxy.user:}")
    private String proxyUser;
    @Value("${proxy.password:}")
    private String proxyPassword;
    @Value("${baidu-map.reverseGeocoding:false}")
    private Boolean directionReverseGeocoding;
    @Value("${baidu-map.cacheable:true}")
    private Boolean cacheable = true;
    @Autowired
    private RedisUtil redisUtil;
    @Value("#{${baidu-map.baseDirectionParams:{}}}")
    private Map<String, String> baseDirectionParams;

    public Boolean getCacheable() {
        return cacheable;
    }

    public void  setCacheable(Boolean cacheable) {
        this.cacheable = cacheable;
    }

    /**
     * 路径规划时是否使用逆编码来设置destination_uid
     * @return
     */
    public Boolean getDirectionReverseGeocoding() {
        return directionReverseGeocoding;
    }

    public JSONObject direction(Point origin, Point destination) {
        return direction(origin, destination, null);
    }

    public JSONObject direction(Point origin, Point destination, Map<String, Object> params) {
        String uri = "https://api.map.baidu.com/direction/v2/driving";
        Map<String, Object> requestParams = new HashMap<>();
        if (baseDirectionParams != null) {
            requestParams.putAll(baseDirectionParams);
        }
        if (params != null) {
            requestParams.putAll(params);
        }
        requestParams.put("origin", origin.latitude + "," + origin.longitude);
        requestParams.put("destination", destination.latitude + "," + destination.longitude);
        return request(uri, requestParams);
    }

    public JSONObject reverseGeocoding(Point point) {
        String uri = "https://api.map.baidu.com/reverse_geocoding/v3/";
        Map<String, Object> params = new HashMap<>();
        params.put("location", point.latitude + "," + point.longitude);
        params.put("extensions_poi", Integer.valueOf(1));
        params.put("output", "json");
        return request(uri, params);
    }

    public JSONObject request(String uri, Map<String, Object> params) {
        params.put("ak", this.key);
        StringBuilder sb = new StringBuilder(uri).append("?");
        for (String k : params.keySet()) {
            sb.append(k).append("=").append(params.get(k)).append("&");
        }
        String url = sb.toString();
        log.info(url);
        String realKey = Md5Util.md5Encode(url, "utf-8");
        if (cacheable) {
            // Object cacheData = redisUtil.get(realKey);
            // if (cacheData != null) {
            //     return (JSONObject) cacheData;
            // }
        }
        try {
            HttpClientBuilder builder = HttpClients.custom();
            RequestConfig.Builder configBuilder = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000);
            if (proxyHost != null && proxyHost.length() > 2) {
                log.debug("use proxy ----------------\nproxyHost:"+proxyHost+"\nproxyPort:"+"\nproxyUser:"+proxyUser+"\nproxyPassword:"+proxyPassword);
                HttpHost proxy = new HttpHost(proxyHost, proxyPort);
                if (StringUtils.hasText(proxyUser)) {
                    CredentialsProvider provider = new BasicCredentialsProvider();
                    provider.setCredentials(new AuthScope(proxy), new UsernamePasswordCredentials(proxyUser, proxyPassword));
                    builder.setDefaultCredentialsProvider(provider);
                }
                configBuilder.setProxy(proxy);
            } else {
                log.debug("No proxy request -----------------");
            }
            HttpClient client = builder.build();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(configBuilder.build());
            HttpResponse res = client.execute(httpGet);
            HttpEntity entity = res.getEntity();
            String result = EntityUtils.toString(entity);
            log.debug("response: "+result);
            EntityUtils.consume(entity);
            JSONObject response = JSONObject.parseObject(result);
            if (response.getIntValue("status") == 0) {
                // redisUtil.set(realKey, response, 24 * 3600);
                return response;
            } else {
                throw new Exception(response.getString("message"));
            }
        } catch (Exception e) {
            log.error("request baidu map error -----------\nurl:"+url+"\nerror:"+e.getMessage(), e);
            return null;
        }
    }
}

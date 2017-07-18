package com.ethan.apigatewaytry.client;

import com.ethan.apigatewaytry.dto.Request;
import com.ethan.apigatewaytry.dto.Response;
import com.ethan.apigatewaytry.dto.SumReqBody;
import com.ethan.apigatewaytry.enums.Method;
import com.ethan.apigatewaytry.util.HttpUtil;
import com.google.gson.Gson;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class AppAuthClient {

    private static final Logger logger = getLogger(AppAuthClient.class);

    private static final String APP_KEY = "24550036";
    private static final String APP_SECRET = "ebd9dc910cb1de1e0195466527c03df3";
    private static final String API_DOMAIN = "http://aeb8558025634af091b8a5b58a6e5212-cn-hangzhou.alicloudapi.com";

    public static void main(String[] args) throws Exception {
        Request req = new Request();
        req.setMethod(Method.POST_STRING);
        req.setHost(API_DOMAIN);
        req.setPath("/appauth/api/sum.json");
        req.setAppKey(APP_KEY);
        req.setAppSecret(APP_SECRET);
        req.setTimeout(3000);
        req.setStringBody(newSumReqBodyString(1, 11));
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Ca-Stage", "TEST");
        headers.put("X-Ca-Request-Mode", "debug");
        req.setHeaders(headers);
        logger.info("req: {}", req);
        Response res = execute(req);
        logger.info("res: {}", res);
    }

    private static String newSumReqBodyString(Integer a, Integer b) {
        Gson gson = new Gson();
        return gson.toJson(new SumReqBody(a, b));
    }

    private static Response execute(Request request) throws Exception {
        switch (request.getMethod()) {
            case GET:
                return HttpUtil.httpGet(request.getHost(), request.getPath(),
                        request.getTimeout(),
                        request.getHeaders(),
                        request.getQuerys(),
                        request.getSignHeaderPrefixList(),
                        request.getAppKey(), request.getAppSecret());
            case POST_FORM:
                return HttpUtil.httpPost(request.getHost(), request.getPath(),
                        request.getTimeout(),
                        request.getHeaders(),
                        request.getQuerys(),
                        request.getBodys(),
                        request.getSignHeaderPrefixList(),
                        request.getAppKey(), request.getAppSecret());
            case POST_STRING:
                return HttpUtil.httpPost(request.getHost(), request.getPath(),
                        request.getTimeout(),
                        request.getHeaders(),
                        request.getQuerys(),
                        request.getStringBody(),
                        request.getSignHeaderPrefixList(),
                        request.getAppKey(), request.getAppSecret());
            case POST_BYTES:
                return HttpUtil.httpPost(request.getHost(), request.getPath(),
                        request.getTimeout(),
                        request.getHeaders(),
                        request.getQuerys(),
                        request.getBytesBody(),
                        request.getSignHeaderPrefixList(),
                        request.getAppKey(), request.getAppSecret());
            case PUT_STRING:
                return HttpUtil.httpPut(request.getHost(), request.getPath(),
                        request.getTimeout(),
                        request.getHeaders(),
                        request.getQuerys(),
                        request.getStringBody(),
                        request.getSignHeaderPrefixList(),
                        request.getAppKey(), request.getAppSecret());
            case PUT_BYTES:
                return HttpUtil.httpPut(request.getHost(), request.getPath(),
                        request.getTimeout(),
                        request.getHeaders(),
                        request.getQuerys(),
                        request.getBytesBody(),
                        request.getSignHeaderPrefixList(),
                        request.getAppKey(), request.getAppSecret());
            case DELETE:
                return HttpUtil.httpDelete(request.getHost(), request.getPath(),
                        request.getTimeout(),
                        request.getHeaders(),
                        request.getQuerys(),
                        request.getSignHeaderPrefixList(),
                        request.getAppKey(), request.getAppSecret());
            default:
                throw new IllegalArgumentException(String.format("unsupported method:%s", request.getMethod()));
        }
    }
    
}

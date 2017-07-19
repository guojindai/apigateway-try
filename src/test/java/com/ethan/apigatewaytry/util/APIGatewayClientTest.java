package com.ethan.apigatewaytry.util;

import com.ethan.apigatewaytry.dto.Request;
import com.ethan.apigatewaytry.dto.Response;
import com.ethan.apigatewaytry.dto.SumReqBody;
import com.ethan.apigatewaytry.enums.Method;
import com.google.common.io.Files;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class APIGatewayClientTest {

    private static final Logger logger = getLogger(APIGatewayClientTest.class);

    private static final String APP_KEY = "24550036";
    private static final String APP_SECRET = "ebd9dc910cb1de1e0195466527c03df3";
    private static final String API_DOMAIN = "http://aeb8558025634af091b8a5b58a6e5212-cn-hangzhou.alicloudapi.com";

    @Test
    public void sum() throws Exception {
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
        Response res = APIGatewayClient.execute(req);
        logger.info("res: {}", res);
        Assert.assertEquals("{\"code\":\"SUCCESS\",\"detail\":{\"sum\":12}}", res.getBody());
    }

    @Test
    public void file() throws IOException {
        File file = File.createTempFile("aaaaaaaaaa-", ".txt");
        logger.info("temp file: {}", file.getAbsolutePath());
        Files.write("lalal", file, Charset.forName("UTF-8"));
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost("http://127.0.0.1:8081/appauth/api/file.json");
        FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
        StringBody fileName = new StringBody("abc", ContentType.MULTIPART_FORM_DATA);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("file", fileBody);
        builder.addPart("fileName", fileName);
        HttpEntity entity = builder.build();
        post.setEntity(entity);
        HttpResponse response = client.execute(post);
        logger.info(IOUtils.toString(response.getEntity().getContent()));
    }

    private String newSumReqBodyString(Integer a, Integer b) {
        Gson gson = new Gson();
        return gson.toJson(new SumReqBody(a, b));
    }

}

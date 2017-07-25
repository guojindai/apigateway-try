package com.ethan.apigatewaytry.util;

import com.ethan.apigatewaytry.dto.Request;
import com.ethan.apigatewaytry.dto.Response;
import com.ethan.apigatewaytry.dto.SumReqBody;
import com.ethan.apigatewaytry.enums.Method;
import com.ethan.apigatewaytry.pf.PFData;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class APIGatewayClientTest {

    private static final Logger logger = getLogger(APIGatewayClientTest.class);

    private static final String APP_KEY = "24550036";
    private static final String APP_SECRET = "ebd9dc910cb1de1e0195466527c03df3";

    // private static final String API_DOMAIN = "http://aeb8558025634af091b8a5b58a6e5212-cn-hangzhou.alicloudapi.com";
    private static final String API_DOMAIN = "http://localhost:8081";

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
        // File file = new File("/Users/guojindai/Desktop/a.png");
        File file = new File("/Users/guojindai/Desktop/eth-java-api-3.0.3.jar");
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(API_DOMAIN + "/appauth/api/file.json?fileName=xxx");
        ByteArrayEntity entity = new ByteArrayEntity(Files.toByteArray(file));
        post.setEntity(entity);
        post.setHeader("X-Ca-Stage", "TEST");
        post.setHeader("X-Ca-Request-Mode", "debug");
        HttpResponse response = client.execute(post);
        logger.info("res status: {}", response.getStatusLine());
        logger.info("res headers:");
        for (Header header: response.getAllHeaders()) {
            logger.info("  {} = {}", header.getName(), header.getValue());
        }
        logger.info("res body: {}", IOUtils.toString(response.getEntity().getContent()));
    }

    @Test
    public void filePf() throws IOException {
        File file = new File("/Users/guojindai/Desktop/a.png");
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(API_DOMAIN + "/appauth/api/filePf.json?fileName=xxx2");
        PFData.PFFile.Builder pfFile = PFData.PFFile.newBuilder();
        pfFile.setFileBytes(ByteString.copyFrom(Files.toByteArray(file)));
        ByteArrayEntity entity = new ByteArrayEntity(pfFile.build().toByteArray());
        post.setEntity(entity);
        post.setHeader("X-Ca-Stage", "TEST");
        post.setHeader("X-Ca-Request-Mode", "debug");
        HttpResponse response = client.execute(post);
        logger.info("res status: {}", response.getStatusLine());
        logger.info("res headers:");
        for (Header header: response.getAllHeaders()) {
            logger.info("  {} = {}", header.getName(), header.getValue());
        }
        logger.info("res body: {}", IOUtils.toString(response.getEntity().getContent()));
    }

    @Test
    public void filePfSize() throws IOException {
        File file = new File("/Users/guojindai/Desktop/a.png");
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(API_DOMAIN + "/appauth/api/filePf.json?fileName=xxx");
        PFData.PFFile.Builder pfFile = PFData.PFFile.newBuilder();
        pfFile.setFileBytes(ByteString.copyFrom(Files.toByteArray(file)));
        FileOutputStream fos = new FileOutputStream("/Users/guojindai/Desktop/wxwx.png");
        fos.write(pfFile.build().toByteArray());
    }

    private String newSumReqBodyString(Integer a, Integer b) {
        Gson gson = new Gson();
        return gson.toJson(new SumReqBody(a, b));
    }

}

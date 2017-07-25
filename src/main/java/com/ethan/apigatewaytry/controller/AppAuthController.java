package com.ethan.apigatewaytry.controller;

import com.ethan.apigatewaytry.dto.ResResult;
import com.ethan.apigatewaytry.dto.SumReqBody;
import com.google.common.io.Files;
import com.google.gson.Gson;
import org.apache.catalina.util.IOTools;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

@RestController
@RequestMapping("appauth")
public class AppAuthController {

    private static final Logger logger = getLogger(AppAuthController.class);
    private static String tmpDir;

    @PostConstruct
    public void postConstruct() {
        tmpDir = Files.createTempDir().getAbsolutePath();
        logger.info("file path: {}", tmpDir);
    }

    @RequestMapping(value = "api/sum.json", method = RequestMethod.POST)
    public ResponseEntity sum(HttpServletRequest req) throws UnsupportedEncodingException {
        logger.info("------ controller start");
        logger.info("req url: {}, query: {}", req.getRequestURL(), req.getQueryString());
        printReqHeaders(req);
        Gson gson = new Gson();
        SumReqBody sumReqBody = gson.fromJson(new String((byte[]) req.getAttribute("bodyBytes"),
                "UTF-8"), SumReqBody.class);
        logger.info("req body: {}", sumReqBody);
        if (sumReqBody.getA() != null && sumReqBody.getB() != null) {
            Map<String, Integer> detail = new HashMap<>();
            detail.put("sum", sumReqBody.getA() + sumReqBody.getB());
            return buildResponseEntity(new ResResult("SUCCESS", detail));
        } else {
            return buildResponseEntity(HttpStatus.BAD_REQUEST,
                    new ResResult("PARAM_ERROR", "需要参数a和b，都为数字"));
        }
    }

    @RequestMapping(value = "api/file.json", method = RequestMethod.POST)
    public ResponseEntity file(@RequestParam String fileName, HttpServletRequest req) throws IOException {
        String filePath = tmpDir + "/" + fileName;
        logger.info("filePath: {}", filePath);
        IOUtils.copy(req.getInputStream(), new FileOutputStream(filePath));
        return buildResponseEntity(new ResResult("SUCCESS", null));
    }

    private ResponseEntity buildResponseEntity(Object body) {
        return buildResponseEntity(HttpStatus.OK, body);
    }

    private ResponseEntity buildResponseEntity(HttpStatus status, Object body) {
        logger.info("res status: {}, body: {}", status, body);
        return ResponseEntity.status(status).body(body);
    }

    private void printReqHeaders(HttpServletRequest req) {
        Enumeration<String> names = req.getHeaderNames();
        String name;
        logger.info("req headers:");
        while (names.hasMoreElements()) {
            name = names.nextElement();
            logger.info("  {} = {}", name, req.getHeader(name));
        }
    }

}

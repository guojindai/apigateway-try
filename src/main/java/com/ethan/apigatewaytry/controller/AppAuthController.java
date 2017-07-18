package com.ethan.apigatewaytry.controller;

import com.ethan.apigatewaytry.dto.ResResult;
import com.ethan.apigatewaytry.dto.SumReqBody;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

@RestController
@RequestMapping("appauth")
public class AppAuthController {

    private static final Logger logger = getLogger(AppAuthController.class);

    @RequestMapping(value = "api/sum.json", method = RequestMethod.POST)
    public ResponseEntity sum(@RequestBody SumReqBody sumReqBody,
                              HttpServletRequest req,
                              HttpServletResponse res) {
        logger.info("------ controller start");
        logger.info("req url: {}, query: {}", req.getRequestURL(), req.getQueryString());
        printReqHeaders(req);
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

    private ResponseEntity buildResponseEntity(Object body) {
        return buildResponseEntity(HttpStatus.OK, body);
    }

    private ResponseEntity buildResponseEntity(HttpStatus status, Object body) {
        logger.info("res status: {}, body: {}", status, body);
        return ResponseEntity.status(status).body(body);
    }

    private void printReqHeaders(HttpServletRequest req) {
        Enumeration<String> names = req.getHeaderNames();
        String name = null;
        logger.info("req headers:");
        while (names.hasMoreElements()) {
            name = names.nextElement();
            logger.info("  {} = {}", name, req.getHeader(name));
        }
    }

}

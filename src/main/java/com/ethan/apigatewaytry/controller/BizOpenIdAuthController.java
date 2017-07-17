package com.ethan.apigatewaytry.controller;

import com.ethan.apigatewaytry.dto.ResResult;
import com.ethan.apigatewaytry.dto.SumReqBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("openidauth")
public class BizOpenIdAuthController {

    @RequestMapping(value = "api/sum.json", method = RequestMethod.POST)
    public ResponseEntity sum(@RequestBody SumReqBody sumReqBody) {
        if (sumReqBody.getA() != null && sumReqBody.getB() != null) {
            Map<String, Integer> detail = new HashMap<>();
            detail.put("sum", sumReqBody.getA() + sumReqBody.getB());
            return ResponseEntity.ok(new ResResult("SUCCESS", detail));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResResult("PARAM_ERROR", "需要参数a和b，都为数字"));
        }
    }

}

package com.ethan.apigatewaytry.interceptor;

import com.ethan.apigatewaytry.constant.Constants;
import com.ethan.apigatewaytry.util.MessageDigestUtil;
import com.google.common.base.Strings;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SignatureVerifyInterceptor implements HandlerInterceptor {

    private static final Logger logger = getLogger(SignatureVerifyInterceptor.class);

    private static final String CA_PROXY_SIGN = "X-Ca-Proxy-Signature";
    private static final String CA_PROXY_SIGN_SECRET_KEY = "X-Ca-Proxy-Signature-Secret-Key";
    private static final String CA_PROXY_SIGN_HEADERS = "X-Ca-Proxy-Signature-Headers";
    private static final boolean HTTP_HEADER_TO_LOWER_CASE = false;
    private static final String HTTP_METHOD_POST = "post";
    private static final String HTTP_METHOD_PUT = "put";
    private static final Map<String, String> signSecretMap = new HashMap<>();
    private static final char LF = '\n';

    static {
        signSecretMap.put("A78934526", "AG976mDSWs48yTFZP53p");
    }

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String uri = httpServletRequest.getRequestURI();
        String httpMethod = httpServletRequest.getMethod();
        Map<String, String> headerMap = createHeaderMap(httpServletRequest);
        Map<String, Object> paramMap = createParamMap(httpServletRequest);
        byte[] inputStreamBytes = IOUtils.toByteArray(httpServletRequest.getInputStream());
        String gatewaySign = httpServletRequest.getHeader(HTTP_HEADER_TO_LOWER_CASE
                ? CA_PROXY_SIGN.toLowerCase() : CA_PROXY_SIGN);
        logger.info("uri: {}", uri);
        logger.info("CA_PROXY_SIGN_HEADERS: {}", httpServletRequest.getHeader(CA_PROXY_SIGN_HEADERS));
        logger.info("headerMap: {}", headerMap);
        logger.info("paramMap: {}", paramMap);
        logger.info("API网关签名: {}", gatewaySign);
        try {
            String serviceSign = serviceSign(uri, httpMethod, headerMap, paramMap, inputStreamBytes);
            logger.info("服务端签名: {}", serviceSign);
            if (!serviceSign.equals(gatewaySign)) {
                throw new RuntimeException("InvalidSignature");
            }
        } catch (Exception e) {
            e.printStackTrace();
            httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
            httpServletResponse.getWriter().write(e.getMessage());
            return false;
        }
        return true;
    }

    private Map<String, String> createHeaderMap(HttpServletRequest httpServletRequest) {
        Map<String, String> headerMap = new HashMap<>();
        String headerNamesString = httpServletRequest.getHeader(HTTP_HEADER_TO_LOWER_CASE
                ? CA_PROXY_SIGN_HEADERS.toLowerCase() : CA_PROXY_SIGN_HEADERS);
        if (!Strings.isNullOrEmpty(headerNamesString)) {
            for (String name: headerNamesString.split(",")) {
                headerMap.put(name, httpServletRequest.getHeader(name));
            }
        }
        return headerMap;
    }

    private Map<String, Object> createParamMap(HttpServletRequest httpServletRequest) {
        Map<String, Object> paramMap = new HashMap<>();
        if (!Strings.isNullOrEmpty(httpServletRequest.getQueryString())) {
            for (String keyValue: httpServletRequest.getQueryString().split("&")) {
                if (!Strings.isNullOrEmpty(keyValue)) {
                    String[] keyValueArray = keyValue.split("=");
                    paramMap.put(keyValueArray[0], keyValueArray.length > 1 ? keyValueArray[1] : null);
                }
            }
        }
        return paramMap;
    }


    private static String serviceSign(String uri, String httpMethod, Map<String, String> headers, Map<String, Object> paramsMap, byte[] inputStreamBytes) throws Exception {
        Map<String, String> headersToSign = buildHeadersToSign(headers);
        String bodyMd5 = buildBodyMd5(httpMethod, inputStreamBytes);
        String resourceToSign = buildResource(uri, paramsMap);
        String stringToSign = buildStringToSign(headersToSign, resourceToSign, httpMethod, bodyMd5);
        logger.info("stringToSign: {}", stringToSign.replaceAll("\n", "|"));
        Mac hmacSha256 = Mac.getInstance(Constants.HMAC_SHA256);
        String secret = signSecretMap.get(headers.get(HTTP_HEADER_TO_LOWER_CASE
                ? CA_PROXY_SIGN_SECRET_KEY.toLowerCase() : CA_PROXY_SIGN_SECRET_KEY));
        secret = Strings.isNullOrEmpty(secret) ? "" : secret;
        byte[] keyBytes = secret.getBytes(Constants.ENCODING);
        hmacSha256.init(new SecretKeySpec(keyBytes, 0, keyBytes.length, Constants.HMAC_SHA256));
        return new String(Base64.encodeBase64(hmacSha256.doFinal(stringToSign.getBytes(Constants.ENCODING))),
                Constants.ENCODING);
    }

    private static String buildBodyMd5(String httpMethod, byte[] inputStreamBytes) throws IOException {
        if (inputStreamBytes == null) {
            return null;
        }
        if (!httpMethod.equalsIgnoreCase(HTTP_METHOD_POST) && !httpMethod.equalsIgnoreCase(HTTP_METHOD_PUT)) {
            return null;
        }
        InputStream inputStream = new ByteArrayInputStream(inputStreamBytes);
        byte[] bodyBytes = IOUtils.toByteArray(inputStream);
        if (bodyBytes != null && bodyBytes.length > 0) {
            return MessageDigestUtil.base64AndMD5(bodyBytes).trim();
        }
        return null;
    }

    private static String buildMapToSign(Map<String, Object> paramMap) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> e : paramMap.entrySet()) {
            if (builder.length() > 0) {
                builder.append('&');
            }
            String key = e.getKey();
            Object value = e.getValue();
            if (value != null) {
                if (value instanceof List) {
                    List list = (List) value;
                    if (list.size() == 0) {
                        builder.append(key);
                    } else {
                        builder.append(key).append("=").append(String.valueOf(list.get(0)));
                    }
                } else if (value instanceof Object[]) {
                    Object[] objs = (Object[]) value;
                    if (objs.length == 0) {
                        builder.append(key);
                    } else {
                        builder.append(key).append("=").append(String.valueOf(objs[0]));
                    }
                } else {
                    builder.append(key).append("=").append(String.valueOf(value));
                }
            }
        }

        return builder.toString();
    }

    private static Map<String, String> buildHeadersToSign(Map<String, String> headers) {
        Map<String, String> headersToSignMap = new TreeMap<>();
        String headersToSignString = headers.get(HTTP_HEADER_TO_LOWER_CASE ? CA_PROXY_SIGN_HEADERS.toLowerCase() : CA_PROXY_SIGN_HEADERS);
        if (headersToSignString != null) {
            for (String headerKey : headersToSignString.split("\\,")) {
                headersToSignMap.put(headerKey, headers.get(HTTP_HEADER_TO_LOWER_CASE
                        ? headerKey.toLowerCase() : headerKey));
            }
        }
        return headersToSignMap;
    }

    private static String buildStringToSign(Map<String, String> headers, String resourceToSign, String method, String bodyMd5) {
        StringBuilder sb = new StringBuilder();
        sb.append(method).append(LF);
        if (StringUtils.isNotBlank(bodyMd5)) {
            sb.append(bodyMd5);
        }
        sb.append(LF);
        sb.append(buildHeaders(headers));
        sb.append(resourceToSign);
        return sb.toString();
    }

    private static String buildHeaders(Map<String, String> headers) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : headers.entrySet()) {
            if (e.getValue() != null) {
                sb.append(e.getKey().toLowerCase()).append(':').append(e.getValue()).append(LF);
            }
        }
        return sb.toString();
    }

    private static String buildResource(String uri, Map<String, Object> paramsMap) {
        StringBuilder builder = new StringBuilder();
        builder.append(uri);
        TreeMap<String, Object> sortMap = new TreeMap<>();
        sortMap.putAll(paramsMap);
        if (sortMap.size() > 0) {
            builder.append('?');
            builder.append(buildMapToSign(sortMap));
        }
        return builder.toString();
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
    }

}

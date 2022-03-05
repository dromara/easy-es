package com.xpc.easyes.autoconfig.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HttpContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 打印 es 请求与返回日志
 *
 * @author hutu
 * @date 2022/3/5 3:11 下午
 */
@Slf4j
public class HttpLoggingInterceptor implements HttpResponseInterceptor, HttpRequestInterceptor {

    @Override
    public void process(HttpRequest request, HttpContext context) throws IOException {

        if (request instanceof HttpEntityEnclosingRequest && ((HttpEntityEnclosingRequest) request).getEntity() != null) {

            HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) request;
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            entity.writeTo(buffer);

            if (!entity.isRepeatable()) {
                entityRequest.setEntity(new ByteArrayEntity(buffer.toByteArray()));
            }
            log.info("request url: {} {} \n parameter: {}", request.getRequestLine().getMethod(), request.getRequestLine().getUri(), new String(buffer.toByteArray()));

        } else {
            log.info("request url: {} {}", request.getRequestLine().getMethod(), request.getRequestLine().getUri());
        }
    }

    @Override
    public void process(HttpResponse response, HttpContext context) {
        log.info("Received raw response: {}", response.getStatusLine().getStatusCode());
    }
}
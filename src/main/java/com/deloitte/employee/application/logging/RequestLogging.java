package com.deloitte.employee.application.logging;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * Logging Filter to log the request and response details.
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
class RequestLogging extends OncePerRequestFilter {

    private final Tracer tracer;


    /**
     * Filter method to log the request and response details.
     *
     * @param httpRequest  request object.
     * @param httpResponse response object.
     * @param filterChain  filter chain object.
     * @throws ServletException if any error occurs while processing the request or response.
     * @throws IOException      if any error occurs while processing the request or response.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest httpRequest,
                                    HttpServletResponse httpResponse,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        boolean isMultipart = httpRequest.getContentType() != null
                && httpRequest.getContentType().startsWith("multipart/");
        boolean isDownload = httpResponse.getContentType() != null
                && httpResponse.getContentType().startsWith("application/octet-stream");
        (httpResponse).setHeader("X-request-id", tracer.currentSpan().context().traceId());
        if (isMultipart) {
            logRequest(httpRequest, "<<Multipart content>>");
//            CachedBodyHttpServletRequest cachedBodyHttpServletRequest =
//                new CachedBodyHttpServletRequest(httpRequest);
//            ContentCachingResponseWrapper cachedResponse =
//                new ContentCachingResponseWrapper(httpResponse);
            filterChain.doFilter(httpRequest, httpResponse);
            log.info("Response: Latency = {} ms, status={}",
                    (System.currentTimeMillis() - startTime), httpResponse.getStatus());
        } else if (isDownload) {
            logRequest(httpRequest, "<<Download content>>");
            filterChain.doFilter(httpRequest, httpResponse);
            log.info("Response: Latency = {} ms, status={}",
                    (System.currentTimeMillis() - startTime), httpResponse.getStatus());
        } else {
            CachedBodyHttpServletRequest cachedBodyHttpServletRequest =
                    new CachedBodyHttpServletRequest(httpRequest);
            String body = IOUtils.toString(cachedBodyHttpServletRequest.getInputStream(),
                    cachedBodyHttpServletRequest.getCharacterEncoding());
            logRequest(cachedBodyHttpServletRequest, body);
            ContentCachingResponseWrapper cachedResponse =
                    new ContentCachingResponseWrapper(httpResponse);
            filterChain.doFilter(cachedBodyHttpServletRequest, cachedResponse);
            log.info("Response: Latency = {} ms, status={}, body={}",
                    (System.currentTimeMillis() - startTime), httpResponse.getStatus(),
                    new String(cachedResponse.getContentAsByteArray(), StandardCharsets.UTF_8));
            cachedResponse.copyBodyToResponse();
        }
    }

    /**
     * request logging.
     *
     * @param cachedBodyHttpServletRequest cached request object.
     * @param body                         request body.
     */
    private void logRequest(HttpServletRequest cachedBodyHttpServletRequest, String body) {
        log.info("Request: method={}, uri={},  body={}, headers={}, clientIps = {}",
                cachedBodyHttpServletRequest.getMethod(),
                cachedBodyHttpServletRequest.getRequestURI(), body,
                getHeaders(cachedBodyHttpServletRequest),
                getClientIpAddress(cachedBodyHttpServletRequest));
    }


    /**
     * get headers.
     *
     * @param cachedBodyHttpServletRequest cached request object.
     * @return headers.
     */
    private Map<String, String> getHeaders(HttpServletRequest cachedBodyHttpServletRequest) {
        Map<String, String> headers = new HashMap<>();
        cachedBodyHttpServletRequest.getHeaderNames().asIterator()
                .forEachRemaining(headerName -> headers.put(headerName,
                        cachedBodyHttpServletRequest.getHeader(headerName)));
        return headers;
    }

    /**
     * get client ip address.
     *
     * @param httpServletRequest request object.
     * @return client ip address.
     */
    private String getClientIpAddress(HttpServletRequest httpServletRequest) {
        String[] ipHeaderCandidates = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };
        for (String header : ipHeaderCandidates) {
            String ipAddress = httpServletRequest.getHeader(header.toLowerCase());
            if (ipAddress != null
                    && !StringUtils.isEmpty(ipAddress)
                    && !ipAddress.equalsIgnoreCase("unknown")) {
                return ipAddress;
            }
        }
        return httpServletRequest.getRemoteAddr();
    }
}

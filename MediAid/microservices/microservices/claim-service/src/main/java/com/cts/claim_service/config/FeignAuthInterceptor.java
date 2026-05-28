package com.cts.claim_service.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Forwards the trusted user-identity headers injected by the API gateway
 * (X-User-Id, X-User-Role, X-Username) on every outbound Feign call so the
 * downstream service can authenticate the request the same way it
 * authenticates a request that came directly from the gateway.
 *
 * The raw Authorization header is also forwarded as a defence-in-depth
 * fallback for services that still validate the JWT directly.
 */
@Configuration
public class FeignAuthInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return;
        HttpServletRequest req = attrs.getRequest();
        forward(template, req, "X-User-Id");
        forward(template, req, "X-User-Role");
        forward(template, req, "X-Username");
        forward(template, req, "Authorization");
    }

    private static void forward(RequestTemplate template, HttpServletRequest req, String header) {
        String value = req.getHeader(header);
        if (value != null && !value.isBlank()) {
            template.header(header, value);
        }
    }
}

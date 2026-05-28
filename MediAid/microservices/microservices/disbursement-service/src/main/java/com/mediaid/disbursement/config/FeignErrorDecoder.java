package com.mediaid.disbursement.config;

import com.mediaid.disbursement.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 404) {
            String message = "Resource not found";
            if (response.body() != null) {
                try {
                    String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
                    JsonNode node = objectMapper.readTree(body);
                    String parsed = node.path("message").asText(null);
                    if (parsed != null && !parsed.isEmpty()) {
                        message = parsed;
                    }
                } catch (IOException ignored) {
                }
            }
            return new ResourceNotFoundException(message);
        }
        return defaultDecoder.decode(methodKey, response);
    }
}

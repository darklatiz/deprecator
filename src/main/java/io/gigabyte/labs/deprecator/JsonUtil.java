package io.gigabyte.labs.deprecator;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);

    public static Endpoint convertJsonToConfiguration(String jsonString) throws IOException {
        return objectMapper.readValue(jsonString, Endpoint.class);
    }
}
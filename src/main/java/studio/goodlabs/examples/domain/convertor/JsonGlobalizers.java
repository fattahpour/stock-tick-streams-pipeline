package studio.goodlabs.examples.domain.convertor;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import studio.goodlabs.examples.domain.GlobalTick;

import java.time.Instant;

public class JsonGlobalizers {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static GlobalTick fromJson(String json) {
        try {
            JsonNode node = MAPPER.readTree(json);
            return new GlobalTick(
                    node.path("ticker").asText(null),
                    node.has("lastPrice") ? node.get("lastPrice").asDouble() : null,
                    node.path("fx").asText(null),
                    node.path("venue").asText(null),
                    node.path("sector").asText(null),
                    node.has("ts") ? Instant.parse(node.get("ts").asText()) : null
            );
        } catch (Exception e) {
            return null;
        }
    }
}
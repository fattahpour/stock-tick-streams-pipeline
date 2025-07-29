package studio.goodlabs.examples.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class FilterRuleLoader {
    public static List<FilterRule> load(String resourcePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream is = FilterRuleLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) throw new IllegalArgumentException("Cannot find: " + resourcePath);
            Map<?, ?> yaml = mapper.readValue(is, Map.class);
            List<Map<String, String>> rulesList = (List<Map<String, String>>) yaml.get("filterRules");
            return rulesList.stream()
                    .map(m -> new FilterRule(m.get("mic"), m.get("marketSector"), m.get("tickerPattern")))
                    .toList();
        }
    }
}

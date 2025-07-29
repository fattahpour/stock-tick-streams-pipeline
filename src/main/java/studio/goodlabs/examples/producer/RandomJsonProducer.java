package studio.goodlabs.examples.producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import studio.goodlabs.examples.avros.StockTick;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Properties;
import java.util.Random;

public class RandomJsonProducer {
    public static void main(String[] args) throws Exception {
        String topic = "stock-feed-json";
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        ObjectMapper mapper = new ObjectMapper();
        Random rand = new Random();

        for (int i = 0; i < 100; i++) {
            StockTick tick = (rand.nextInt(5) == 0)
                    ? RandomStockTickGenerator.randomInvalid()
                    : RandomStockTickGenerator.next();

            String json = mapper.writeValueAsString(toJsonCompatibleStockTick(tick));

            ProducerRecord<String, String> record =
                    new ProducerRecord<>(topic, "json-key" + i, json);

            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    System.out.println("JSON Sent: " + json + " | offset: " + metadata.offset());
                } else {
                    exception.printStackTrace();
                }
            });

            Thread.sleep(5000);
        }
        producer.flush();
        producer.close();
    }

    private static Object toJsonCompatibleStockTick(StockTick t) {
        return new Object() {
            public final String ticker = t.getInstrumentId() != null ? t.getInstrumentId().toString() : null;
            public final Double lastPrice = t.getPrice();
            public final String fx = t.getCurrency() != null ? t.getCurrency().toString() : null;
            public final String venue = t.getMic() != null ? t.getMic().toString() : null;
            public final String sector = t.getMarketSector() != null ? t.getMarketSector().toString() : null;
            public final String ts = t.getTimestamp() != null ? t.getTimestamp().toString() : null;
        };
    }
}

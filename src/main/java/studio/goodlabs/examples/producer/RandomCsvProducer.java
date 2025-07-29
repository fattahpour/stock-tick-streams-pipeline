package studio.goodlabs.examples.producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Instant;
import java.util.Properties;
import java.util.Random;

public class RandomCsvProducer {
    public static void main(String[] args) throws Exception {
        String topic = "stock-feed-csv";
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        Random rand = new Random();

        for (int i = 0; i < 100; i++) {

            String symbol = randomFrom("AAPL", "TSLA", "BP", "MSFT", "RDSA", "VOW3", "ABCD", "XYZ");
            double price = rand.nextBoolean() ? rand.nextDouble() * 500 : 0.0;
            String currency = randomFrom("USD", "EUR", "GBP");
            String mic = randomFrom("XNAS", "XLON", "XETR", "ABCD");
            String sector = randomFrom("Technology", "Energy", "Automotive", "Consumer");
            String timestamp = Instant.now().toString();
            String flag = rand.nextBoolean() ? "Y" : "N";
            String region = randomFrom("US", "UK", "DE", "NA");


            String csv = String.join(",", symbol, String.valueOf(price), currency, mic, sector, timestamp, flag, region);

            ProducerRecord<String, String> record =
                    new ProducerRecord<>(topic, "csv-key" + i, csv);

            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    System.out.println("CSV Sent: " + csv + " | offset: " + metadata.offset());
                } else {
                    exception.printStackTrace();
                }
            });

            Thread.sleep(5000);
        }
        producer.flush();
        producer.close();
    }

    private static String randomFrom(String... vals) {
        return vals[new Random().nextInt(vals.length)];
    }
}

package studio.goodlabs.examples;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import studio.goodlabs.examples.avros.InvalidStockFeedRecord;
import studio.goodlabs.examples.avros.StockTick;
import studio.goodlabs.examples.domain.FilterRule;
import studio.goodlabs.examples.domain.FilterRuleLoader;
import studio.goodlabs.examples.domain.GlobalTick;
import studio.goodlabs.examples.domain.convertor.JsonGlobalizers;
import studio.goodlabs.examples.domain.convertor.CsvGlobalizers;
import studio.goodlabs.examples.util.Validator;
import studio.goodlabs.examples.domain.convertor.AvroConverter;

import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "tick-filter");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put("schema.registry.url", "http://localhost:8081");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        List<FilterRule> rules = FilterRuleLoader.load("filter-rules.yaml");

        StreamsBuilder builder = new StreamsBuilder();

        SpecificAvroSerde<StockTick> stockTickSerde = avroSerde(StockTick.class);
        SpecificAvroSerde<InvalidStockFeedRecord> invalidSerde = avroSerde(InvalidStockFeedRecord.class);

        KStream<String, String> jsonStream = builder.stream("stock-feed-json", Consumed.with(Serdes.String(), Serdes.String()));
        KStream<String, String> csvStream = builder.stream("stock-feed-csv", Consumed.with(Serdes.String(), Serdes.String()));

        KStream<String, GlobalTick> normalisedJson = jsonStream.mapValues(JsonGlobalizers::fromJson);
        KStream<String, GlobalTick> normalisedCsv = csvStream.mapValues(CsvGlobalizers::fromCsv);

        KStream<String, GlobalTick> merged = normalisedJson.merge(normalisedCsv);

        KStream<String, GlobalTick>[] branches = merged.branch(
                (k, v) -> v != null && Validator.isValid(v),
                (k, v) -> true
        );

        KStream<String, GlobalTick> valid = branches[0];
        KStream<String, GlobalTick> invalid = branches[1];


        invalid
                .mapValues(Validator::invalidRecord)
                .to("dql-invalid-ticks", Produced.with(Serdes.String(), invalidSerde));

        valid
                .filter((k, v) -> rules.stream().anyMatch(rule -> rule.matches(v)))
                .mapValues(AvroConverter::toStockTickAvro)
                .to("filtered-stock-ticks", Produced.with(Serdes.String(), stockTickSerde));

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private static <T extends SpecificRecord> SpecificAvroSerde<T> avroSerde(Class<T> cls) {
        SpecificAvroSerde<T> serde = new SpecificAvroSerde<>();
        serde.configure(Map.of("schema.registry.url", "http://localhost:8081"), false);
        return serde;
    }
}

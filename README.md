# Stock Tick Streams Pipeline

This project implements a Kafka Streams application that consumes stock tick data from CSV and JSON topics, normalises each event, validates required fields and applies filtering rules loaded from `src/main/resources/filter-rules.yaml`. Valid records are written to the `filtered-stock-ticks` topic using Avro while invalid events go to `dql-invalid-ticks`.

## Prerequisites
- Java 17+
- Maven 3.9+
- A running Kafka cluster and Schema Registry accessible at `localhost:9092` and `http://localhost:8081` (the default locations expected by the application). You can use Confluent's [cp-all-in-one](https://docs.confluent.io/platform/current/quickstart/ce-docker-quickstart.html) Docker compose for convenience.

## Building
Run the following from the repository root to compile sources and generate Avro classes:

```bash
mvn package
```

## Running the stream processor
Execute the application using Maven's exec plugin which resolves the runtime classpath automatically:

```bash
mvn exec:java -Dexec.mainClass=studio.goodlabs.examples.Main -Dexec.classpathScope=runtime
```

The processor expects the input topics `stock-feed-json` and `stock-feed-csv` and will produce to `filtered-stock-ticks` and `dql-invalid-ticks`.

## Producing sample data
Two helper producers are provided to generate random messages:

```bash
# CSV producer
mvn exec:java -Dexec.mainClass=studio.goodlabs.examples.producer.RandomCsvProducer -Dexec.classpathScope=runtime

# JSON producer
mvn exec:java -Dexec.mainClass=studio.goodlabs.examples.producer.RandomJsonProducer -Dexec.classpathScope=runtime
```

## Filter rules
Filtering behaviour is controlled by `src/main/resources/filter-rules.yaml`. Example rules:

```yaml
filterRules:
  - mic: "XNAS"
    marketSector: "Technology"
  - mic: "XLON"
  - marketSector: "Energy"
    tickerPattern: "^BP.*"
  - tickerPattern: "^TSLA$"
  - mic: "XETR"
    marketSector: "Consumer"
  - tickerPattern: "^AAPL|MSFT$"
```

Adjust these rules to change which ticks are emitted to the output topic.

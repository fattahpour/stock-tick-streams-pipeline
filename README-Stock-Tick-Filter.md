### 🧾 JIRA Task

**Title**:
Implement Kafka Streams Processor for Stock Tick Normalisation, Validation, Filtering, and DLQ Emission

---

### 🧭 Description

Develop a **Kafka Streams** application that ingests stock tick data from two heterogeneous input topics (`CSV` and `JSON`), normalises and validates the records, and emits valid, matched records to a single output topic. Filter rules are loaded from a YAML configuration file at startup. Validation failures are emitted to a DLQ topic in Avro format.

---

### 🔄 Processing Flow

1. **Ingest & Parse**:

    * Read from two input topics: one with **CSV** and one with **JSON**
    * Parse and normalise each message into a unified `NormalisedTick` representation, keyed by `instrumentId`

2. **Validate**:

    * Check that all mandatory fields are present and semantically valid (e.g. numeric price > 0, non-empty MIC)
    * If invalid:

        * Emit to **DLQ** `dql-invalid-ticks` as an `InvalidStockFeedRecord`, containing:

            * the `NormalisedTick`
            * a structured error code
            * a validation error message

3. **Filter**:

    * Apply a static set of filter rules from a **YAML config file**, loaded at startup
    * Each rule may include one or more of:

        * `mic`
        * `marketSector`
        * `tickerPattern` (regex on `instrumentId`)
    * A record matches if **all defined rule fields match**
    * If at least one rule matches → forward to output topic
    * Otherwise → silently discard

4. **Emit**:

    * Valid, matched records are emitted to the topic `filtered-stock-ticks` in Avro format (`StockTickAvro`)

---

### 📄 Topics

| Purpose                 | Topic Name             | Format                          |
| ----------------------- | ---------------------- | ------------------------------- |
| Input — JSON            | `stock-feed-json`      | JSON                            |
| Input — CSV             | `stock-feed-csv`       | CSV                             |
| Output — Filtered Valid | `filtered-stock-ticks` | Avro (`StockTickAvro`)          |
| Dead Letter Queue (DLQ) | `dql-invalid-ticks`    | Avro (`InvalidStockFeedRecord`) |

---

### 📁 Filter Rules Config (YAML)

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

---

## 🔤 Example Schemas

### 📥 Input JSON (from topic `stock-feed-json`)

```json
{
  "ticker": "AAPL",
  "lastPrice": 195.32,
  "fx": "USD",
  "venue": "XNAS",
  "sector": "Technology",
  "ts": "2025-07-14T10:45:32.123Z",
  "source": "reuters",
  "qualityScore": 92.5,
  "suspicious": false
}
```

### 📥 Input CSV (from topic `stock-feed-csv`)

```
symbol,price,currency,mic_code,industry_group,timestamp_iso,flag,region
TSLA,254.87,USD,XNAS,Automotive,2025-07-14T10:46:00.000Z,Y,NA
```

---

### 🔁 Transformation Summary

| Normalised Field | From JSON Input | From CSV Input   |
| ---------------- | --------------- | ---------------- |
| `instrumentId`   | `ticker`        | `symbol`         |
| `price`          | `lastPrice`     | `price`          |
| `currency`       | `fx`            | `currency`       |
| `mic`            | `venue`         | `mic_code`       |
| `marketSector`   | `sector`        | `industry_group` |
| `timestamp`      | `ts`            | `timestamp_iso`  |

* Extra fields in both inputs are ignored
* Timestamps are parsed from ISO strings into timestamp-millis

---

### 📤 Output Avro Schema: `StockTickAvro`

```avro
{
  "type": "record",
  "name": "StockTick",
  "namespace": "studio.goodlabs.examples.avros",
  "fields": [
    { "name": "instrumentId", "type": [ "null", "string" ], "default": null },
    { "name": "price", "type": [ "null", "double" ], "default": null },
    { "name": "currency", "type": [ "null", "string" ], "default": null },
    { "name": "mic", "type": [ "null", "string" ], "default": null },
    { "name": "marketSector", "type": [ "null", "string" ], "default": null },
    { "name": "timestamp", "type": [ "null", { "type": "long", "logicalType": "timestamp-millis" } ], "default": null }
  ]
}
```

---

### 🧨 Output Avro Schema: `InvalidStockFeedRecord`

```avro
{
  "type": "record",
  "name": "InvalidStockFeedRecord",
  "namespace": "studio.goodlabs.examples.avros",
  "fields": [
    { "name": "StockTick", "type": [ "null", "studio.goodlabs.examples.avros.StockTick" ], "default": null },
    { "name": "errorCode", "type": [ "null", "string" ], "default": null },
    { "name": "errorMessage", "type": [ "null", "string" ], "default": null }
  ]
}
```

---

### ✅ Acceptance Criteria

* [ ] Input events from `stock-feed-json` and `stock-feed-csv` are parsed and normalised to `NormalisedTick`
* [ ] Mandatory field validation is applied; failures emit an `InvalidStockFeedRecord` to `dql-invalid-ticks`
* [ ] YAML-based filter rules are loaded at startup
* [ ] Valid, matched records are emitted as `StockTickAvro` to `filtered-stock-ticks`
* [ ] Records that fail all filters are dropped
* [ ] Kafka Streams DSL is used throughout

---



## Local Testing
### Sample Data

**JSON, valid + rules-match**
```
01982ce5-5ab1-75e5-9477-160f0de0f585:{"ticker":"AAPL","lastPrice":195.32,"fx":"USD","venue":"XNAS","sector":"Technology","ts":"2025-07-14T10:45:32.123Z","source":"reuters","qualityScore":92.5,"suspicious":false}
be9ad210-3d16-41c9-953f-0c6c070eedcf:{"ticker":"AAPL","lastPrice":195.32,"fx":"USD","venue":"XNAS","sector":"Technology","ts":"2025-07-14T10:45:32.123Z","source":"reuters","qualityScore":92.5,"suspicious":false}
1b72205c-7cc1-4d0b-8891-6ffda2f84741:{"ticker":"TSLA","lastPrice":254.87,"fx":"USD","venue":"XNAS","sector":"Automotive","ts":"2025-07-14T10:46:00.000Z","source":"bloomberg","qualityScore":88.3,"suspicious":false}
c6073e9c-9cbe-48ea-8898-3de220db885d:{"ticker":"BP","lastPrice":32.15,"fx":"GBP","venue":"XLON","sector":"Energy","ts":"2025-07-14T10:47:00.000Z","source":"ft","qualityScore":91.0,"suspicious":false}
b5b4f58d-d4c7-484a-9e71-b5b03cce5ca7:{"ticker":"TSLA","lastPrice":0.00,"fx":"USD","venue":"XNAS","sector":"Automotive","ts":"2025-07-14T10:48:00.000Z","source":"reuters","qualityScore":45.1,"suspicious":true}
7e321a91-f6e4-496d-9efc-9e835098ec67:{"ticker":"VOW3","lastPrice":116.2,"fx":"EUR","venue":"XETR","sector":"Consumer","ts":"2025-07-14T10:49:00.000Z","source":"bloomberg","qualityScore":79.2,"suspicious":false}
8ef7752f-7b2c-482b-9f85-473e042ef24c:{"ticker":"MSFT","lastPrice":414.85,"fx":"USD","venue":"XNAS","sector":"Technology","ts":"2025-07-14T10:50:00.000Z","source":"yahoo","qualityScore":96.7,"suspicious":false}
f9c6b781-4321-4a29-910c-5e64bf33ceba:{"ticker":"RDSA","lastPrice":29.35,"fx":"GBP","venue":"XLON","sector":"Energy","ts":"2025-07-14T10:51:00.000Z","source":"ft","qualityScore":84.6,"suspicious":false}
40ba9856-b36b-4dc1-a940-7f15c1e2bece:{"ticker":"BP","lastPrice":31.99,"fx":"GBP","venue":"XLON","sector":"Energy","ts":"2025-07-14T10:52:00.000Z","source":"reuters","qualityScore":90.2,"suspicious":true}
ab48bc6f-4c5a-4948-b413-1c1e1962933f:{"ticker":"TSLA","lastPrice":256.21,"fx":"USD","venue":"XNAS","sector":"Automotive","ts":"2025-07-14T10:53:00.000Z","source":"yahoo","qualityScore":89.4,"suspicious":false}
cc87953a-3032-4e80-8073-09cf12e9d70a:{"ticker":"AAPL","lastPrice":195.30,"fx":"USD","venue":"XNAS","sector":"Technology","ts":"2025-07-14T10:54:00.000Z","source":"bloomberg","qualityScore":93.2,"suspicious":false}
```

**JSON, valid + NO rules-match**
```
01982d05-4e2a-7633-a67e-5abef608f4c0:{"ticker":"ABCD","lastPrice":195.32,"fx":"USD","venue":"ABCD","sector":"Technology","ts":"2025-07-14T10:45:32.123Z","source":"reuters","qualityScore":92.5,"suspicious":false}
```

**JSON, invalid**
```
01982d05-1e24-73f4-ae1d-66fffba58944:{"ticker":"AAPL","lastPrice":195.30,"fx":"USD","ts":"2025-07-14T10:54:00.000Z","source":"bloomberg","qualityScore":93.2,"suspicious":false}
```



**CSV, valid + rules-match**
```
BBG000BBVVK7:TSLA,254.87,USD,XNAS,Automotive,2025-07-14T10:46:00.000Z,Y,NA
BBG000C6K6G9:AAPL,195.32,USD,XNAS,Technology,2025-07-14T10:45:32.123Z,N,US
BBG000D87WQ5:BP,32.15,GBP,XLON,Energy,2025-07-14T10:47:00.000Z,Y,UK
BBG000D87WT9:TSLA,0.00,USD,XNAS,Automotive,2025-07-14T10:48:00.000Z,N,US
BBG000CL9VN6:VOW3,116.2,EUR,XETR,Consumer,2025-07-14T10:49:00.000Z,Y,DE
BBG000BPH459:MSFT,414.85,USD,XNAS,Technology,2025-07-14T10:50:00.000Z,N,US
BBG000BPH7M5:RDSA,29.35,GBP,XLON,Energy,2025-07-14T10:51:00.000Z,Y,UK
BBG000BBT7S7:BP,31.99,GBP,XLON,Energy,2025-07-14T10:52:00.000Z,N,UK
BBG000CL9VN7:TSLA,256.21,USD,XNAS,Automotive,2025-07-14T10:53:00.000Z,Y,US
BBG000C6K6H0:AAPL,195.30,USD,XNAS,Technology,2025-07-14T10:54:00.000Z,Y,US
```

**CSV, valid + NO rules-match**
```
BBG000C6K6H1:ABCD,195.30,USD,ABCD,Technology,2025-07-14T10:54:00.000Z,Y,US
```

**CSV, invalid**
```
BBG000C6K6H9:AAPL,195.30,USD,,Technology,2025-07-14T10:54:00.000Z,Y,US
```

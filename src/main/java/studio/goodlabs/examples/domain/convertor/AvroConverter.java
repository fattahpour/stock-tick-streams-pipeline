package studio.goodlabs.examples.domain.convertor;


import studio.goodlabs.examples.avros.StockTick;
import studio.goodlabs.examples.domain.GlobalTick;

import java.time.Instant;

public class AvroConverter {
    public static StockTick toStockTickAvro(GlobalTick t) {
        return StockTick.newBuilder()
                .setInstrumentId(t.instrumentId())
                .setPrice(t.price())
                .setCurrency(t.currency())
                .setMic(t.mic())
                .setMarketSector(t.marketSector())
                .setTimestamp(Instant.ofEpochSecond(t.timestamp() != null ? t.timestamp().toEpochMilli() : null))
                .build();
    }
}
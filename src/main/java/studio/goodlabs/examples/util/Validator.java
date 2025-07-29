package studio.goodlabs.examples.util;


import studio.goodlabs.examples.avros.InvalidStockFeedRecord;
import studio.goodlabs.examples.avros.StockTick;
import studio.goodlabs.examples.domain.GlobalTick;
import studio.goodlabs.examples.domain.convertor.AvroConverter;

public class Validator {
    public static boolean isValid(GlobalTick t) {
        return t.instrumentId() != null
                && t.price() != null && t.price() > 0
                && t.currency() != null
                && t.mic() != null && !t.mic().isEmpty()
                && t.marketSector() != null
                && t.timestamp() != null;
    }
    public static InvalidStockFeedRecord invalidRecord(GlobalTick t) {
        StockTick stockTickAvro = AvroConverter.toStockTickAvro(t);
        return InvalidStockFeedRecord.newBuilder()
                .setTick(stockTickAvro)
                .setErrorCode("VALIDATION_ERROR")
                .setErrorMessage("Missing or invalid fields")
                .build();
    }
}

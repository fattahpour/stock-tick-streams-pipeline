package studio.goodlabs.examples.producer;


import studio.goodlabs.examples.avros.StockTick;

import java.time.Instant;
import java.util.Random;

public class RandomStockTickGenerator {
    private static final String[] TICKERS = {"AAPL", "TSLA", "MSFT", "BP", "VOW3"};
    private static final String[] CURRENCIES = {"USD", "EUR", "GBP"};
    private static final String[] MICS = {"XNAS", "XLON", "XETR"};
    private static final String[] SECTORS = {"Technology", "Automotive", "Consumer", "Energy"};
    private static final Random RAND = new Random();

    public static StockTick next() {
        String ticker = TICKERS[RAND.nextInt(TICKERS.length)];
        double price = 10 + (500 * RAND.nextDouble());
        String currency = CURRENCIES[RAND.nextInt(CURRENCIES.length)];
        String mic = MICS[RAND.nextInt(MICS.length)];
        String sector = SECTORS[RAND.nextInt(SECTORS.length)];
        long ts = Instant.now().toEpochMilli();

        return StockTick.newBuilder()
                .setInstrumentId(ticker)
                .setPrice(price)
                .setCurrency(currency)
                .setMic(mic)
                .setMarketSector(sector)
                .setTimestamp(Instant.ofEpochSecond(ts))
                .build();
    }

    public static StockTick randomInvalid() {
        StockTick.Builder builder = StockTick.newBuilder()
                .setInstrumentId(RAND.nextBoolean() ? null : TICKERS[RAND.nextInt(TICKERS.length)])
                .setPrice(RAND.nextBoolean() ? null : 10 + (500 * RAND.nextDouble()))
                .setCurrency(RAND.nextBoolean() ? null : CURRENCIES[RAND.nextInt(CURRENCIES.length)])
                .setMic(RAND.nextBoolean() ? null : MICS[RAND.nextInt(MICS.length)])
                .setMarketSector(RAND.nextBoolean() ? null : SECTORS[RAND.nextInt(SECTORS.length)])
                .setTimestamp(Instant.ofEpochSecond(Instant.now().toEpochMilli()));
        return builder.build();
    }
}

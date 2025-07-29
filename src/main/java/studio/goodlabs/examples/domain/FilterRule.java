package studio.goodlabs.examples.domain;

import java.util.regex.Pattern;

public record FilterRule(
        String mic,
        String marketSector,
        String tickerPattern
) {
    public boolean matches(GlobalTick tick) {
        if (mic != null && !mic.equals(tick.mic())) return false;
        if (marketSector != null && !marketSector.equals(tick.marketSector())) return false;
        if (tickerPattern != null && (tick.instrumentId() == null ||
                !Pattern.compile(tickerPattern).matcher(tick.instrumentId()).matches())) return false;
        return true;
    }
}

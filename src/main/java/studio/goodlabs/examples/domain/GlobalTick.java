package studio.goodlabs.examples.domain;

import java.time.Instant;

public record GlobalTick(
        String instrumentId,
        Double price,
        String currency,
        String mic,
        String marketSector,
        Instant timestamp
) {}

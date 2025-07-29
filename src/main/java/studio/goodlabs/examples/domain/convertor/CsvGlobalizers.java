package studio.goodlabs.examples.domain.convertor;


import studio.goodlabs.examples.domain.GlobalTick;

import java.time.Instant;

public class CsvGlobalizers {
    public static GlobalTick fromCsv(String csv) {
        // Assume CSV: symbol,price,currency,mic_code,industry_group,timestamp_iso,flag,region
        String[] arr = csv.split(",");
        if (arr.length < 6) return null;
        return new GlobalTick(
                arr[0],
                arr[1].isEmpty() ? null : Double.valueOf(arr[1]),
                arr[2],
                arr[3],
                arr[4],
                arr[5].isEmpty() ? null : Instant.parse(arr[5])
        );
    }
}

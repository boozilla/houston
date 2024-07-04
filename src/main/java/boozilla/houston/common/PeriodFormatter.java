package boozilla.houston.common;

import lombok.experimental.UtilityClass;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatterBuilder;

@UtilityClass
public class PeriodFormatter {
    private static final org.joda.time.format.PeriodFormatter instance = new PeriodFormatterBuilder()
            .printZeroNever()
            .appendYears().appendSuffix("y")
            .appendMonths().appendSuffix("m")
            .appendDays().appendSuffix("d")
            .appendHours().appendSuffix("h")
            .appendMinutes().appendSuffix("m")
            .appendSeconds().appendSuffix("s")
            .toFormatter();

    public String print(final Period period)
    {
        return instance.print(period);
    }
}

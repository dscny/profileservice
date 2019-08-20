/**
 * A way to query and traverse dates/months/years in the histogram by taking a point-in-time
 * snapshot of the int[] array that represents the histogram.
 */

package name.chen.dave.impl;

import name.chen.dave.api.Reporter;

import java.time.LocalDate;
import java.time.Instant;
import java.util.Optional;

public class SnapshotReport implements Reporter {

    private final ArrayDateHistogram histogram;
    private final long snapshotEpoch;
    private int[] histogramArray;


    SnapshotReport(ArrayDateHistogram histogram, int[] histogramArray) {
        this.histogram = histogram;
        this.histogramArray = histogramArray;
        this.snapshotEpoch = Instant.now().getEpochSecond();
    }

    public int traverseDates(LocalDate startDate, LocalDate endDate) {
        return histogram.traverseDates(startDate, endDate, Optional.of(histogramArray));
    }

    public Optional<LocalDate> traverseUntil(LocalDate startDate, int untilNumBirthdays) {
        return histogram.traverseUntil(startDate, untilNumBirthdays, Optional.of(histogramArray));
    }

}

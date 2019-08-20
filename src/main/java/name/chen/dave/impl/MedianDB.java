/**
 * Calculates medians given date range (startDate and endDate). Also contains functionality to add birthdates.
 *
 * Uses the date/month/year histograms {@link name.chen.dave.impl.ArrayDateHistogram} to calculate median values
 *
 */

package name.chen.dave.impl;

import name.chen.dave.api.Reporter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.DAYS;

public class MedianDB {

    private ArrayDateHistogram histogramEngine;

    public MedianDB() throws IOException {
        this(true, true);
    }

    public MedianDB(boolean recoverFromPersistFile, boolean persistToDisk) throws IOException {
        histogramEngine = new ArrayDateHistogram(recoverFromPersistFile, persistToDisk);
    }

    public void addBirthday(LocalDate date) {
        histogramEngine.addDate(date);
    }

    public Optional<LocalDate> findMedian(LocalDate startDate, LocalDate endDate) {
        Reporter reporter = getReporter(histogramEngine);
        int birthdaysFound = reporter.traverseDates(startDate, endDate);
        LocalDate median;

        if (birthdaysFound == 0) {
            return Optional.empty();
        }
        if (birthdaysFound % 2 == 0) {
            int halfBirthdays = birthdaysFound/2;
            Optional<LocalDate> evenCountMedianDate1 = reporter.traverseUntil(startDate, halfBirthdays);
            Optional<LocalDate> evenCountMedianDate2 = reporter.traverseUntil(evenCountMedianDate1.get(), 2);
            long inBetween = DAYS.between(evenCountMedianDate1.get(), evenCountMedianDate2.get());
            return Optional.of(evenCountMedianDate1.get().plusDays(inBetween/2));
        } else {
            int middleBirthday = birthdaysFound/2 + 1;
            Optional<LocalDate> medianDate = reporter.traverseUntil(startDate, middleBirthday);
            return medianDate;
        }
    }

    private Reporter getReporter(ArrayDateHistogram histogram) {
        int[] histogramArraySnapshot = histogram.getHistogramArrayCopy();
        return new SnapshotReport(histogram, histogramArraySnapshot);
    }
}

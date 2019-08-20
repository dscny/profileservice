/**
 * Calculate median birthdates by using arrays that represent histograms of birthdays
 * by year, by month, and by day. Median birthday can be found by:
 *
 * 1. Traversing from start_date and end_date, record how many birthdates were encountered (N)
 * 2. Traversing from start_date, until we encounter N/2 (if N is odd) or N/2 + 1 (if N is even) birthdays
 *
 * Because traversing day-by-day can take many iterations, histograms of birthdays by month and by year
 * can help us traverse more quickly by covering more ground.
 */

package name.chen.dave.impl;

import name.chen.dave.api.DiskPersistable;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

public class ArrayDateHistogram implements DiskPersistable {
    private final static Logger LOGGER = Logger.getLogger(ArrayDateHistogram.class.getName());

    // In histogramArray, first TOTAL_NUM_YEARS_REPRESENTED elements of array represent the "years" histogram
    // representing the birthday count in the year 1850, 1851, ... , 2049
    private static final int EARLIEST_YEAR = 1850;
    private static final LocalDate MIN_DATE_SUPPORTED = LocalDate.of(1850,1,1);
    private static final LocalDate MAX_DATE_SUPPORTED = LocalDate.of(2049,12,31);
    private static final int TOTAL_NUM_YEARS_REPRESENTED = 200;
    private static final int YEARS_HISTOGRAM_NUM = TOTAL_NUM_YEARS_REPRESENTED;

    // Next TOTAL_NUM_YEARS_REPRESENTED*12 elements represent the "months" histogram
    // representing the birthday count in the month 1850-01, 1850-02, ..., 2049-12
    private static final int MONTHS_HISTOGRAM_NUM = YEARS_HISTOGRAM_NUM*12;

    // Lastly, TOTAL_NUM_YEARS_REPRESENTED*12*31 represent the "days" histogram
    // representing the birthday count in 1850-01-01, 1850-01-02, ... , 2049-12-31
    private static final int DAYS_HISTOGRAM_NUM = YEARS_HISTOGRAM_NUM*12*31;
        private static final int TOTAL_NUM_VALUES = YEARS_HISTOGRAM_NUM + MONTHS_HISTOGRAM_NUM + DAYS_HISTOGRAM_NUM;

        // Histogram representing year, month, and day birthday count
        private int[] histogramArray = new int[TOTAL_NUM_VALUES];

        // persist to disk
        private final boolean persistToDisk;
        private final String persistenceFile = System.getProperty("java.io.tmpdir") + File.separator + "bdayhistogram.bin";
        private final IntArrayPersistence intArrayToDisk = new IntArrayPersistence();
        private PersistenceService persistenceService;

        enum HISTOGRAMS { YEAR, MONTH, DAY; }

    public ArrayDateHistogram() throws IOException {
            this(true, true);
        }
    public ArrayDateHistogram(boolean recoverFromPersistFile, boolean persistToDisk) throws IOException {
            if (recoverFromPersistFile) {
                File persistedFile = new File(persistenceFile);
                if (persistedFile.exists()) {
                    this.histogramArray = intArrayToDisk.fromLocalDisk(persistenceFile);
                }
            }
            this.persistToDisk = persistToDisk;
    }

    private LocalDate getLastDayOfYear(LocalDate date) {
        return LocalDate.of(date.getYear(), 12, 31);
    }

    private LocalDate getLastDayOfMonth(LocalDate date) {
        return LocalDate.of(date.getYear(), date.getMonthValue(),  date.getMonth().length(date.isLeapYear()));
    }

    private int getHistogramIndex(LocalDate date, HISTOGRAMS type) {
        validateDate(date);
        int year = date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        switch (type) {
            case YEAR:
                return year - EARLIEST_YEAR;
            case MONTH:
                return TOTAL_NUM_YEARS_REPRESENTED + 12*(year - EARLIEST_YEAR) + month - 1;
            case DAY:
                return TOTAL_NUM_YEARS_REPRESENTED + TOTAL_NUM_YEARS_REPRESENTED*12 +
                        12*31*(year - EARLIEST_YEAR) + 31*(month - 1) + day - 1;
            default: throw new IllegalArgumentException("Invalid Histogram type: " + type);
        }
    }

    private void validateDate(LocalDate date) {
        if (date.isBefore(MIN_DATE_SUPPORTED) || date.isAfter(MAX_DATE_SUPPORTED)) {
            throw new IllegalArgumentException("Date: " + date + " not supported. "
             + "Min Date: " + MIN_DATE_SUPPORTED + ", Max Date: " + MAX_DATE_SUPPORTED);
        }
    }

    private int getHistogramValue(int[] histogramArrayParam, LocalDate date, HISTOGRAMS type) {
        return histogramArrayParam[getHistogramIndex(date, type)];
    }

    public synchronized void addDate(LocalDate date) {
        if (persistToDisk && persistenceService == null) {
            persistenceService = new PersistenceService(this, 10);
            persistenceService.start();
        }
        this.histogramArray[getHistogramIndex(date, HISTOGRAMS.YEAR)]++;
        this.histogramArray[getHistogramIndex(date, HISTOGRAMS.MONTH)]++;
        this.histogramArray[getHistogramIndex(date, HISTOGRAMS.DAY)]++;
        if (persistToDisk) {
            persistenceService.indicateNewChanges();
        }
    }

    public synchronized int[] getHistogramArrayCopy() {
        return Arrays.copyOf(this.histogramArray, TOTAL_NUM_VALUES);
    }

    public int traverseDates(LocalDate startDate, LocalDate endDate, Optional<int[]> histogramCopyParam) {
        int[] histogramCopy = histogramCopyParam.orElse(getHistogramArrayCopy());
        LocalDate currentDate = startDate;
        int birthdayCount = 0;
        int currentDateMonth = 0;

        yearlyHistogram:
        while (currentDate.isBefore(endDate)) {
            while ((currentDate.getDayOfYear() == 1) &&
                    (getLastDayOfYear(currentDate).isBefore(endDate.plusDays(1)))) {
                birthdayCount += getHistogramValue(histogramCopy, currentDate, HISTOGRAMS.YEAR);
                currentDate = currentDate.plusYears(1);
            }
            while ((currentDate.getDayOfMonth() == 1) &&
                    (getLastDayOfMonth(currentDate).isBefore(endDate.plusDays(1)))) {
                birthdayCount += getHistogramValue(histogramCopy, currentDate, HISTOGRAMS.MONTH);
                currentDate = currentDate.plusMonths(1);
                if (currentDate.getDayOfYear() == 1) {
                    continue yearlyHistogram;
                }
            }
            currentDateMonth = currentDate.getMonthValue();
            while (currentDate.isBefore(endDate.plusDays(1)) && currentDate.getMonthValue() == currentDateMonth) {
                birthdayCount += getHistogramValue(histogramCopy, currentDate, HISTOGRAMS.DAY);
                currentDate = currentDate.plusDays(1);
            }
        }
        return birthdayCount;
    }


    /**
     * Traverse from start, and keep traversing until numBirthdays
     * number of birthdays are reached.  Return the first Date during which
     * >= numBirthdays was reached (if multiple birthdays on that date, for instance)
     *
     * @param startDate
     * @param numBirthdays
     * @return
     */
    public Optional<LocalDate> traverseUntil(LocalDate startDate, int numBirthdays, Optional<int[]> histogramParam) {
        int[] histogramCopy = histogramParam.orElse(getHistogramArrayCopy());
        LocalDate currentDate = startDate;
        int birthdayCount = 0;
        int currentDateMonth = 0;
        LocalDate pastDateRange = LocalDate.of(EARLIEST_YEAR+TOTAL_NUM_YEARS_REPRESENTED, 1,1);
        yearlyHistogram:
        while ((birthdayCount < numBirthdays) && (currentDate.isBefore(pastDateRange))) {
            while ((currentDate.isBefore(pastDateRange)) && (currentDate.getDayOfYear() == 1) &&
                   ((birthdayCount + getHistogramValue(histogramCopy, currentDate, HISTOGRAMS.YEAR)) < numBirthdays)) {
                birthdayCount += getHistogramValue(histogramCopy, currentDate, HISTOGRAMS.YEAR);
                currentDate = currentDate.plusYears(1);
            }
            while ((currentDate.isBefore(pastDateRange)) && (currentDate.getDayOfMonth() == 1) &&
                   ((birthdayCount + getHistogramValue(histogramCopy, currentDate, HISTOGRAMS.MONTH)) < numBirthdays)) {
                birthdayCount += getHistogramValue(histogramCopy, currentDate, HISTOGRAMS.MONTH);
                currentDate = currentDate.plusMonths(1);
                if (currentDate.getDayOfYear() == 1) {
                    continue yearlyHistogram;
                }
            }
            currentDateMonth = currentDate.getMonthValue();
            while ((currentDate.isBefore(pastDateRange)) &&
                    (currentDate.getMonthValue() == currentDateMonth)) {
                birthdayCount += getHistogramValue(histogramCopy, currentDate, HISTOGRAMS.DAY);
                if (birthdayCount >= numBirthdays) {
                    break yearlyHistogram;
                }
                currentDate = currentDate.plusDays(1);
            }
        }
        if (!currentDate.isBefore(pastDateRange)) {
            return Optional.empty();
        }
        return Optional.of(currentDate);
    }

    @Override
    public synchronized void persistToLocalDisk() throws IOException {
        intArrayToDisk.toLocalDisk(histogramArray, persistenceFile);
    }



}

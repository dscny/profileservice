/**
 * Test {@link name.chen.dave.impl.ArrayDateHistogram}, the heart of the program.
 *
 * Traversing dates/months/years to get how many birthdates were encountered.
 */
package name.chen.dave.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

public class ArrayDateHistogramTest {

    private ArrayDateHistogram histogram;

    @Before
    public void setUp() throws IOException {
        histogram = new ArrayDateHistogram(false, false);
    }

    @Test
    public void testAddDates() {
        histogram.addDate(LocalDate.of(2001, 5, 2));
        histogram.addDate(LocalDate.of(2001, 6, 23));
        histogram.addDate(LocalDate.of(2001, 7, 1));
        histogram.addDate(LocalDate.of(2007, 2, 23));
        int bdays = histogram.traverseDates(LocalDate.of(2000, 2, 1),
            LocalDate.of(2001, 7, 1), Optional.empty());
        Assert.assertEquals(3, bdays);
    }

    @Test
    public void testTraverseDatesEmptyHistogram() {
        // traverse empty histogram
        int bdays = histogram.traverseDates(LocalDate.of(2000, 2, 10),
                LocalDate.of(2001, 7, 1), Optional.empty());
        Assert.assertEquals(0, bdays);
        bdays = histogram.traverseDates(LocalDate.of(1850, 1, 10),
                LocalDate.of(2049, 12, 31), Optional.empty());
        Assert.assertEquals(0, bdays);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTraverseDatesSupportedDatesUpper() {
        histogram.traverseDates(LocalDate.of(2000, 2, 10),
                LocalDate.of(2050, 7, 1), Optional.empty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTraverseDatesSupportedDatesLower() {
        histogram.traverseDates(LocalDate.of(1849, 2, 10),
                LocalDate.of(2019, 8, 1), Optional.empty());
    }

    @Test
    public void testTraverseUntil() {
        histogram.addDate(LocalDate.of(2001, 2, 5));
        histogram.addDate(LocalDate.of(2001, 2, 5));
        histogram.addDate(LocalDate.of(2001, 2, 6));
        histogram.addDate(LocalDate.of(2001, 2, 6));
        histogram.addDate(LocalDate.of(2001, 2, 7));
        histogram.addDate(LocalDate.of(2001, 3, 7));
        Optional<LocalDate> dateUntil = histogram.traverseUntil(LocalDate.of(2001,2,1), 1, Optional.empty());
        Assert.assertEquals(LocalDate.of(2001,2,5), dateUntil.get());
        dateUntil = histogram.traverseUntil(LocalDate.of(2001,2,1), 2, Optional.empty());
        Assert.assertEquals(LocalDate.of(2001,2,5), dateUntil.get());
        dateUntil = histogram.traverseUntil(LocalDate.of(2001,2,1), 4, Optional.empty());
        Assert.assertEquals(LocalDate.of(2001,2,6), dateUntil.get());
        dateUntil = histogram.traverseUntil(LocalDate.of(2001,2,1), 10, Optional.empty());
        Assert.assertFalse(dateUntil.isPresent());

    }
}

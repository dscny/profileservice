/**
 * Test getting medians
 */

package name.chen.dave.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

public class MedianDBTest {

    private MedianDB bdayMedians;

    @Before
    public void setUp() throws IOException {
        bdayMedians = new MedianDB(false, false);
    }

    @Test
    public void testGetMedianFromEmptyDB() {
        Optional<LocalDate> medianBDay = bdayMedians.findMedian(LocalDate.of(2003,5,3),
            LocalDate.of(2005,6,7));
        Assert.assertFalse(medianBDay.isPresent());
    }

    @Test
    public void testGetMedianFromOneBDay() {
        bdayMedians.addBirthday(LocalDate.of(1999,2,4));
        Optional<LocalDate> medianBDay = bdayMedians.findMedian(LocalDate.of(1998,7,1),
                LocalDate.of(1999,5,5));
        Assert.assertEquals(LocalDate.of(1999, 2,4), medianBDay.get());
    }

    @Test
    public void testGetMedianFromEvenBDayCount() {
        bdayMedians.addBirthday(LocalDate.of(1999,2,5));
        bdayMedians.addBirthday(LocalDate.of(1999,2,6));
        bdayMedians.addBirthday(LocalDate.of(1999,2,8));
        bdayMedians.addBirthday(LocalDate.of(1999,2,9));
        Optional<LocalDate> medianBDay = bdayMedians.findMedian(LocalDate.of(1999,2,1),
                LocalDate.of(1999,3,1));
        Assert.assertEquals(LocalDate.of(1999, 2,7), medianBDay.get());
    }

    @Test
    public void testGetMedianQueryRangeContainsNoBirthdays() {
        bdayMedians.addBirthday(LocalDate.of(1999,2,5));
        bdayMedians.addBirthday(LocalDate.of(1999,2,6));
        bdayMedians.addBirthday(LocalDate.of(1999,2,7));
        Optional<LocalDate> medianBDay = bdayMedians.findMedian(LocalDate.of(1950,1,1),
                LocalDate.of(1990,9,30));
        Assert.assertFalse(medianBDay.isPresent());
    }

    @Test
    public void testGetMedianQueryRangeContainsOneBirthdays() {
        bdayMedians.addBirthday(LocalDate.of(1999,2,5));
        bdayMedians.addBirthday(LocalDate.of(1999,2,6));
        bdayMedians.addBirthday(LocalDate.of(1999,2,7));
        Optional<LocalDate> medianBDay = bdayMedians.findMedian(LocalDate.of(1950,1,1),
                LocalDate.of(1999,2,5));
        Assert.assertEquals(LocalDate.of(1999, 2,5), medianBDay.get());
    }

    @Test
    public void testGetMedianMultipleBirthdaysOnSameDay() {
        bdayMedians.addBirthday(LocalDate.of(1990,1,1));
        bdayMedians.addBirthday(LocalDate.of(1999,2,5));
        bdayMedians.addBirthday(LocalDate.of(1999,2,5));
        bdayMedians.addBirthday(LocalDate.of(1999,2,5));
        bdayMedians.addBirthday(LocalDate.of(2015,10,31));
        Optional<LocalDate> medianBDay = bdayMedians.findMedian(LocalDate.of(1950,1,1),
                LocalDate.of(2019,2,5));
        Assert.assertEquals(LocalDate.of(1999, 2,5), medianBDay.get());
    }
}

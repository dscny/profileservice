/**
 * Interface pertaining to basic functionality of the histogram.
 *
 */
package name.chen.dave.api;

import java.time.LocalDate;
import java.util.Optional;

public interface Reporter {

    int traverseDates(LocalDate startDate, LocalDate endDate);

    Optional<LocalDate> traverseUntil(LocalDate startDate, int untilNumBirthdays);
}

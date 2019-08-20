/**
 * Class called by the Rest controller ({@link name.chen.dave.rest.ProfileMedianReporter}. Dispatches to
 * {@link name.chen.dave.impl.MedianDB} to add birthday as well as calculate median birthday given a date range
 */
package name.chen.dave.rest;

import name.chen.dave.impl.MedianDB;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class Birthday {
    private final static Logger LOGGER = Logger.getLogger(Birthday.class.getName());
    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
    private final static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private MedianDB medianDB;

    public Birthday() throws IOException {
        medianDB = new MedianDB();
    }

    public static class Add {
        private final String birthdayAdded;
        private final String timeAdded;

        public Add(String birthdayAdded) {
            this.birthdayAdded = birthdayAdded;
            this.timeAdded = dateTimeFormatter.format(ZonedDateTime.now());
        }
        public String getBirthdayAdded() {
            return birthdayAdded;
        }

        public String getTimeAdded() {
            return timeAdded;
        }
    }

    public static class Median {
        private final Integer medianAge;
        private final String fulfillmentTime;

        public Median(Integer medianAge) {
            this.medianAge = medianAge;
            this.fulfillmentTime = dateTimeFormatter.format(ZonedDateTime.now());
        }
        public Integer getMedianAge() {
            return medianAge;
        }

        public String getFulfillmentTime() {
            return fulfillmentTime;
        }
    }

    public static class ErrorResponse
    {
        private final String message;
        private final List<String> details;

        public ErrorResponse(HttpStatus httpStatus, List<String> details) {
            this.message = httpStatus.getReasonPhrase();
            this.details = details;
        }
        //getters and setters
        public String getMessage() {
            return message;
        }
        public List<String> getDetails() {
            return details;
        }
    }

    public Add addBirthday(String birthday) {
        LocalDate bdayDate = LocalDate.parse(birthday, dateFormatter);
        medianDB.addBirthday(bdayDate);
        return new Add(birthday);
    }

    public Median getMedianAge(String start, String end) {
        LocalDate startDate = LocalDate.parse(start, dateFormatter);
        LocalDate endDate = LocalDate.parse(end, dateFormatter);
        Optional<LocalDate> medianBirthdayOptional = medianDB.findMedian(startDate, endDate);
        if (medianBirthdayOptional.isPresent()) {
            LocalDate medianBirthday = medianBirthdayOptional.get();
            LocalDate today = LocalDate.now();
            int medianAge = Period.between(medianBirthday, today).getYears();
            return new Median(medianAge);
        } else {
            return new Median(null);
        }
    }
}

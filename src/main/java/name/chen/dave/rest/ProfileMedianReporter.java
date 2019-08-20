/**
 * The REST controller
 */
package name.chen.dave.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
public class ProfileMedianReporter {

    private final static Logger LOGGER = Logger.getLogger(ProfileMedianReporter.class.getName());

    private Birthday birthday;

    public ProfileMedianReporter() throws IOException {
        birthday = new Birthday();
    }

    @RequestMapping(value = "/birthday/add", method = RequestMethod.GET)
    public Birthday.Add addBirthday(@RequestParam(value="birthday") String birthdayParam) {
        return birthday.addBirthday(birthdayParam);
    }

    @RequestMapping(value = "/birthday/medianage", method = RequestMethod.GET)
    public Birthday.Median getMedianAge(@RequestParam(value="start") String start,
                                        @RequestParam(value="end") String end) {
        return birthday.getMedianAge(start, end);
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Birthday.ErrorResponse> handleInvalidTraceIdException
            (Exception ex, WebRequest request) {
        List<String> details = new ArrayList<>();
        details.add(ex.getLocalizedMessage());
        Birthday.ErrorResponse error = new Birthday.ErrorResponse(BAD_REQUEST, details);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}

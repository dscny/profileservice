/**
 * Test app by loading entire Spring application with embedded Tomcat web container and testing against it.
 */

package name.chen.dave.rest;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.io.File;

public class ApplicationTest {

    private ApplicationContext context;
    private RestTemplate restTemplate;
    private static final String addBirthday = "http://localhost:8080/birthday/add";
    private static final String medianAge = "http://localhost:8080/birthday/medianage";
    private static final JSONParser parser = new JSONParser();

    @Before
    public void setUp() {
        String[] args = {};
        context = SpringApplication.run(Application.class, args);
        restTemplate = new RestTemplate();
    }

    @After
    public void tearDown() {
        SpringApplication.exit(context);
        File db = new File(System.getProperty("java.io.tmpdir") + File.separator + "bdayhistogram.bin");
        if (db.exists()) {
            db.deleteOnExit();
        }
        restTemplate = null;
    }

    @Test
    public void testRestAPI() throws ParseException {
        ResponseEntity<String> response = restTemplate.getForEntity(addBirthday + "?birthday=2005-02-03", String.class);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);

        ResponseEntity<String> response2 = restTemplate.getForEntity(addBirthday + "?birthday=2004-02-03", String.class);
        Assert.assertEquals(response2.getStatusCode(), HttpStatus.OK);

        ResponseEntity<String> response3 = restTemplate.getForEntity(addBirthday + "?birthday=2006-02-03", String.class);
        Assert.assertEquals(response3.getStatusCode(), HttpStatus.OK);

        ResponseEntity<String> medianResp = restTemplate.getForEntity(
                medianAge + "?start=2001-02-04&end=2008-05-02", String.class);
        Assert.assertEquals(medianResp.getStatusCode(), HttpStatus.OK);
        String resp = medianResp.getBody();
        JSONObject jsonObject = (JSONObject) parser.parse(resp);
        Assert.assertEquals(14L, jsonObject.get("medianAge"));
    }

    @Test
    public void testAppShutdownRecovery() throws InterruptedException, ParseException {
        // add birthday
        ResponseEntity<String> response = restTemplate.getForEntity(addBirthday + "?birthday=1995-07-20", String.class);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        // wait some time
        Thread.sleep(12000);

        // shutdown app
        SpringApplication.exit(context);

        // wait some time
        Thread.sleep(3000);

        // restart app
        context = SpringApplication.run(Application.class);

        // get median
        ResponseEntity<String> medianResp = restTemplate.getForEntity(
                medianAge + "?start=1990-02-04&end=2008-05-02", String.class);
        Assert.assertEquals(medianResp.getStatusCode(), HttpStatus.OK);

        JSONObject jsonObject = (JSONObject) parser.parse( medianResp.getBody());
        Assert.assertEquals(24L, jsonObject.get("medianAge"));
    }

    @Test(expected = HttpClientErrorException.class)
    public void testErrorRequest() {
        // add birthday
        ResponseEntity<String> response = restTemplate.getForEntity(addBirthday + "?birthday=BADDATA", String.class);
    }
}

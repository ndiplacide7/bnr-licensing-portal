package rw.gov.bnr.bnrlicensingportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BnrLicensingPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(BnrLicensingPortalApplication.class, args);
    }

}

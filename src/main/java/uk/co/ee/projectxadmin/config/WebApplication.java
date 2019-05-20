package uk.co.ee.projectxadmin.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(scanBasePackages="uk.co.ee.projectxadmin")
public class WebApplication {

    public static void main(String[] args) throws Exception {
    	System.out.println("Starting Application....");
        SpringApplication.run(WebApplication.class, args);
    }
}
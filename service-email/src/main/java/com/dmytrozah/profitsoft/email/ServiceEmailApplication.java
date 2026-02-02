package com.dmytrozah.profitsoft.email;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ServiceEmailApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceEmailApplication.class, args);
    }

}

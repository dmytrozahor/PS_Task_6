package com.dmytrozah.profitsoft;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.dmytrozah.profitsoft.config",
        "com.dmytrozah.profitsoft.service",
        "com.dmytrozah.profitsoft.adapter",
        "com.dmytrozah.profitsoft.domain.repository"
})
/*
@Slf4j
*/
public class Task2App implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Task2App.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

    }
}

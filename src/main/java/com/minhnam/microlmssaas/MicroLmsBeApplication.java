package com.minhnam.microlmssaas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MicroLmsBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroLmsBeApplication.class, args);
    }

}

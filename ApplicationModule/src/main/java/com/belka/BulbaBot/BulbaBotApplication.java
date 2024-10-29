package com.belka.BulbaBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.belka"})
public class BulbaBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(BulbaBotApplication.class, args);
    }
}

//todo: add exceptions handling that will show exactly what went wrong and get relevant answers

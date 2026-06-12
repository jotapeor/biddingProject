package com.bidding.system.bidding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * {@code @EnableScheduling} é necessário para que os métodos anotados com {@code @Scheduled}
 * (como o verificador de editais expirados em {@link com.bidding.system.bidding.service.EditalService})
 * sejam executados em background de forma periódica pelo Spring Task Scheduler.
 */
@SpringBootApplication
@EnableScheduling
public class BiddingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BiddingApplication.class, args);
    }

}
package com.bidding.system.bidding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Combina @SpringBootConfiguration + @EnableAutoConfiguration + @ComponentScan:
// configura beans, ativa autoconfiguração e varre subpacotes em busca de @Component, @Service, etc.
@SpringBootApplication
public class BiddingApplication {

    public static void main(String[] args) {
        // Inicializa o contexto Spring, sobe o servidor Tomcat embutido e deixa a aplicação pronta para receber requisições
        SpringApplication.run(BiddingApplication.class, args);
    }

}
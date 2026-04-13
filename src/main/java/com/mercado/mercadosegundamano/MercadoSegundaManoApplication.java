package com.mercado.mercadosegundamano;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class MercadoSegundaManoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MercadoSegundaManoApplication.class, args);
    }

}

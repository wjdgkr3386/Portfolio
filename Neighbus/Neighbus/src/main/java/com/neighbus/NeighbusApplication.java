package com.neighbus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // [1] 이거 추가됨

@EnableScheduling // [2] ★이 줄이 있어야 스케줄러가 작동합니다!
@SpringBootApplication
public class NeighbusApplication {

    public static void main(String[] args) {
        SpringApplication.run(NeighbusApplication.class, args);
    }

}
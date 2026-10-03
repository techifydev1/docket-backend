package com.qudus.docket_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DocketBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocketBackendApplication.class, args);
	}

}

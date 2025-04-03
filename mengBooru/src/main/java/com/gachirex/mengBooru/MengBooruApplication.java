package com.gachirex.mengBooru;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.gachirex.mengBooru.config.StorageProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class MengBooruApplication {

	public static void main(String[] args) {
		SpringApplication.run(MengBooruApplication.class, args);
	}
}
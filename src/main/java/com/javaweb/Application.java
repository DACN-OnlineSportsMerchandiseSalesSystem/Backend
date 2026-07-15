package com.javaweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = ElasticsearchRepositoriesAutoConfiguration.class)
@EnableScheduling
@org.springframework.scheduling.annotation.EnableAsync
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);

	}

}
 	

package com.eikona.mata;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.datatables.repository.DataTablesRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.vonage.client.VonageClient;
import com.vonage.client.sms.MessageStatus;
import com.vonage.client.sms.SmsSubmissionResponse;
import com.vonage.client.sms.messages.TextMessage;

@SpringBootApplication
@ComponentScan(basePackages = "com.eikona.mata")
@ComponentScan(basePackages = "com.eikona.mata.controller" )
@EntityScan(basePackages = {"com.eikona.mata.entity"})
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages ={"com.eikona.mata.repository"},
									repositoryFactoryBeanClass = DataTablesRepositoryFactoryBean.class)
public class MataApplication {

	@PostConstruct
	public void init(){
		TimeZone.setDefault(TimeZone.getTimeZone("IST"));
	}
	public static void main(String[] args) {
		
		SpringApplication.run(MataApplication.class, args);
		
		 
//		 VonageClient client = VonageClient.builder()
//			        .apiKey("a93b6e12")
//			        .apiSecret("10DcoM9W5jDFS2if")
//			        .build();
//
//
//         TextMessage message = new TextMessage("919348481478", "916362556445", "Hello Pradeep!");
//
//         SmsSubmissionResponse response = client.getSmsClient().submitMessage(message);
//
//         if (response.getMessages().get(0).getStatus() == MessageStatus.OK) {
//             System.out.println("Message sent successfully. " + response.getMessages());
//         } else {
//             System.out.println("Message failed with error: " + response.getMessages().get(0).getErrorText());
//         }
	}
}

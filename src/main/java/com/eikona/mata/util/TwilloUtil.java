package com.eikona.mata.util;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
@Component
@EnableScheduling
public class TwilloUtil {
    // Find your Account SID and Auth Token at twilio.com/console
    // and set the environment variables. See http://twil.io/secure
    public static final String ACCOUNT_SID = "AC50a4a5187bd70057fc7167df8b74f959";
    public static final String AUTH_TOKEN = "342c3a87ac796bee465a542af0c535dd";

//    @Scheduled(fixedDelay = 50000)
    public void sms() {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Message message = Message.creator(
                new com.twilio.type.PhoneNumber("+916362556445"),
                new com.twilio.type.PhoneNumber("+919348481478"),
                "Hii Pradeep")
            .create();

        System.out.println(message.getSid());
    }
}

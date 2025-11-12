package com.cineasy.mailservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MailserviceApplication {

	public static void main(String[] args) {

		try {
			var result = AuthHelper.getAccessToken();
			GraphHelper.sendEmail(result.accessToken());
		} catch (Exception e) {
			e.printStackTrace();
		}



	}

}

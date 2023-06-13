package com.bctech.hive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HiveApplication {

	public static void main(String[] args) {
		SpringApplication.run(HiveApplication.class, args);
	}

/*TODO: Add the following features to the HiveApplication 2.0:
     Oauth2 Login... google, facebook, twitter, github, linkedin
     Caching list of banks
     caching most last used bank
     security check for withdrawal
     enable real time upating of wallet balance
*/
}

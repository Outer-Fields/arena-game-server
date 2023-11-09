package io.mindspice.okragameserver;

import io.mindspice.okragameserver.core.Settings;
import io.mindspice.okragameserver.game.cards.ActionCard;
import io.mindspice.okragameserver.util.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;


@SpringBootApplication
public class OkraGameServerApplication {

	public static void main(String[] args) throws IOException {
		Settings.writeBlank();

		SpringApplication.run(OkraGameServerApplication.class, args);
	}

}

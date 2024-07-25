package SlackWorkBot.WorkBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class WorkBotApplication {
	public static void main(String[] args) {
		SpringApplication.run(WorkBotApplication.class, args);
	}
}

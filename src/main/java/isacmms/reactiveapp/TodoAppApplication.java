package isacmms.reactiveapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import lombok.extern.log4j.Log4j2;
import reactor.tools.agent.ReactorDebugAgent;

@Log4j2
@EnableReactiveMongoRepositories
@EnableR2dbcRepositories
@SpringBootApplication(scanBasePackages = { "isacmms.reactiveapp" })
public class TodoAppApplication {

	public static void main(String[] args) {
		log.debug("Initializing Reactor Debug Agent");
		ReactorDebugAgent.init();
		SpringApplication.run(TodoAppApplication.class, args);
	}
	
}

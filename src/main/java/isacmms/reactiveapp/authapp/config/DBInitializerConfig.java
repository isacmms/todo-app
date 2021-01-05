package isacmms.reactiveapp.authapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.connectionfactory.init.CompositeDatabasePopulator;
import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer;
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator;

import io.r2dbc.spi.ConnectionFactory;

/**
 * Not used with docker-compose.
 * R2DBC has a bug in which the query expander thinks a bcrypt ($2$...etc) is a binding marker and 
 * can't properly set default user with encrypted password. Using docker-compose volume copy to /docker-entrypoint-initdb.d
 * which is intended for db initialization and will run any shell or sql script.
 * @author isacm
 *
 */
@Configuration
public class DBInitializerConfig /*extends AbstractR2dbcConfiguration*/ {

	@Bean
	public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {

		ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
		initializer.setConnectionFactory(connectionFactory);

		CompositeDatabasePopulator populator = new CompositeDatabasePopulator();
		populator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("init.sql")));
		initializer.setDatabasePopulator(populator);

		return initializer;
	}

}

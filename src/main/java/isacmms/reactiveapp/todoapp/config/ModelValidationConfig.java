package isacmms.reactiveapp.todoapp.config;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
/**
 * Por algum motivo Spring Boot 2.3.0 apresenta problemas com 
 * javax validation-api. Talvez precise fazer tudo manualmente
 * para funcionar.
 */
//@Configurable
@Deprecated
public class ModelValidationConfig {
	
	//@Bean
	public LocalValidatorFactoryBean validator() {
		return new LocalValidatorFactoryBean();
	}
}

package isacmms.reactiveapp.authapp.api;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class AuthenticationRouter {

	@Bean
	RouterFunction<ServerResponse> authRoutes(AuthenticationHandler handler) {
		return route(i(POST("/authenticate")), handler::createAuthenticationToken);
	}
	
	private static RequestPredicate i(RequestPredicate target) {
        return new CaseInsensitiveRequestPredicate(target);
    }
}

package isacmms.reactiveapp.authapp.api;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.PATCH;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import isacmms.reactiveapp.todoapp.api.CaseInsensitiveRequestPredicate;

@Configuration
public class UserRouter {

	@Bean
    RouterFunction<ServerResponse> userRoutes(UserHandler handler) {
        return route(i(GET("/api/users")), handler::get)
        		//.andRoute(i(GET("/api/users/{username}")), handler::getByUsername)
        		.andRoute(i(GET("/api/users/{email}")), handler::getByEmail)
        		//.andRoute(i(GET("/api/users")), handler::get)
        		.andRoute(i(POST("/api/users")), handler::create)
        		.andRoute(i(PUT("/api/users/{username}")), handler::update)
        		.andRoute(i(PATCH("/api/users/{username}")), handler::patch)
        		.andRoute(i(DELETE("/api/users/{username}")), handler::delete)
        		//.andRoute(i(DELETE("/api/users")), handler::clear)
        		;
    }
	
    private static RequestPredicate i(RequestPredicate target) {
        return new CaseInsensitiveRequestPredicate(target);
    }
}

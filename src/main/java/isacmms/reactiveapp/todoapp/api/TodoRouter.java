package isacmms.reactiveapp.todoapp.api;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.PATCH;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class TodoRouter {

	@Bean
    RouterFunction<ServerResponse> todoRoutes(UserTodoHandler handler) {
        return route(i(GET("/api/todos")), handler::all)
        		.andRoute(i(GET("/api/todos/{id}")), handler::getById)
        		.andRoute(i(POST("/api/todos")), handler::create)
        		.andRoute(i(PUT("/api/todos/{id}")), handler::updateById)
        		.andRoute(i(PATCH("/api/todos/{id}")), handler::patchById)
        		.andRoute(i(DELETE("/api/todos/{id}")), handler::deleteById);
    }
	
	@Bean
	RouterFunction<ServerResponse> adminTodoRoutes(AdminTodoHandler handler) {
		return route(i(GET("/api/admin/todos")), handler::all)
        		//.andRoute(i(GET("/greet")), handler::greet)
        		//.andRoute(i(GET("/api/todos/example")), handler::example)
        		//.andRoute(i(GET("/api/todos/events")), handler::events)
        		.andRoute(i(GET("/api/admin/todos/{id}")), handler::getById)
        		.andRoute(i(POST("/api/admin/todos")), handler::create)
        		.andRoute(i(PUT("/api/admin/todos/{id}")), handler::updateById)
        		.andRoute(i(PATCH("/api/admin/todos/{id}")), handler::patchById)
        		.andRoute(i(DELETE("/api/admin/todos/{id}")), handler::deleteById)
        		.andRoute(i(DELETE("/api/admin/todos")), handler::clear);
	}
	
    private static RequestPredicate i(RequestPredicate target) {
        return new CaseInsensitiveRequestPredicate(target);
    }
}

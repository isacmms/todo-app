package isacmms.reactiveapp.todoapp.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import isacmms.reactiveapp.todoapp.service.TodoService;
import isacmms.reactiveapp.todoapp.service.UserTodoService;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * 
 * @author isacm
 *
 */
@Log4j2
@Component
@PreAuthorize("isFullyAuthenticated()")
public class UserTodoHandler extends TodoHandler {
	
	private final UserTodoService service;
	
	public UserTodoHandler(TodoService service, Validator validator, ObjectMapper mapper) {
		super(validator, mapper);
		this.service = service;
	}
	
	public Mono<ServerResponse> all(ServerRequest req) {
		log.debug("> UserTodoHandler.all()");
		return req.principal().flatMap(principal -> 
				defaultOkManyResponse(
						this.service.findAll(principal.getName(), regexParams(req), sortParams(req))));
	}
	
	public Mono<ServerResponse> getById(ServerRequest req) {
		log.debug("> UserTodoHandler.getById()");
		return req.principal().flatMap(principal -> 
			defaultOkOneResponse(this.service.findById(id(req), principal.getName())));
	}
	
	public Mono<ServerResponse> create(ServerRequest req) {
		return req.principal().flatMap(principal -> 
			defaultCreatedResponse(
				extractBodyRequired(req)
					.doOnNext(this::validate)
					.flatMap(todo -> this.service.create(todo, principal.getName()))));
	}
	
	public Mono<ServerResponse> updateById(ServerRequest req) {
		return req.principal().flatMap(principal -> 
			defaultOkOneResponse(
				extractBodyRequired(req)
					.doOnNext(this::validate)
					.flatMap(todo -> this.service.update(id(req), todo, principal.getName()))));
	}
	
	public Mono<ServerResponse> patchById(ServerRequest req) {
		return req.principal().flatMap(principal -> 
			defaultOkOneResponse(
				extractBodyRequired(req)
					.flatMap(todo -> this.service.patch(id(req), todo, principal.getName()))));
	}
	
	public Mono<ServerResponse> deleteById(ServerRequest req) {
		return req.principal().flatMap(principal -> 
			defaultAcceptedResponse(this.service.delete(id(req), principal.getName())));
	}
	
}

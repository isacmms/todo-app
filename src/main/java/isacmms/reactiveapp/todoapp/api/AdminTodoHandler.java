package isacmms.reactiveapp.todoapp.api;

import java.time.Duration;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import isacmms.reactiveapp.todoapp.model.Todo;
import isacmms.reactiveapp.todoapp.service.AdminTodoService;
import isacmms.reactiveapp.todoapp.service.TodoService;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@Component
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminTodoHandler extends TodoHandler {
	
	private final AdminTodoService service;
	
	public AdminTodoHandler(TodoService service, Validator validator, ObjectMapper mapper) {
		super(validator, mapper);
		this.service = service;
	}

	@Deprecated
	public Mono<ServerResponse> greet(ServerRequest req) {
		return ServerResponse
				.ok()
				.contentType(MediaType.TEXT_PLAIN)
				.bodyValue("Hello!");
	}
	
	@Deprecated
	public Mono<ServerResponse> events(ServerRequest req) {
		final Flux<Todo> todos = this.service.findAllIgnoreOwnership();
		return ServerResponse
				.ok()
				.contentType(MediaType.APPLICATION_STREAM_JSON)
				.body(todos.delayElements(Duration.ofSeconds(1L)), Todo.class);
		/*
		return Mono.from(Flux.zip(interval, todos).doOnNext(onNext))
			.flatMap(
				tuple -> ServerResponse
					.ok()
					.contentType(MediaType.TEXT_EVENT_STREAM)
					.build(tuple.getT2(), Todo.class));
		*/
	}
	
	public Mono<ServerResponse> all(ServerRequest req) {
		log.debug("> UserTodoHandler.all()");
		return defaultOkManyResponse(
				this.service.findAllIgnoreOwnership(regexParams(req), sortParams(req)));
	}
	
	public Mono<ServerResponse> getById(ServerRequest req) {
		log.debug("> UserTodoHandler.getById()");
		return defaultOkOneResponse(this.service.findByIdIgnoreOwnership(id(req)));
	}
	
	public Mono<ServerResponse> create(ServerRequest req) {
		return req.principal().flatMap(principal -> {
			final Optional<String> forceOwner = forceOwner(req);
			final String owner = forceOwner.isPresent() ? forceOwner.get() : principal.getName();
			
			return defaultCreatedResponse(
					extractBodyRequired(req)
						.doOnNext(this::validate)
						.flatMap(todo -> this.service.create(todo, owner)));
		});
	}
	
	public Mono<ServerResponse> updateById(ServerRequest req) {
		return defaultOkOneResponse(
				extractBodyRequired(req)
					.doOnNext(this::validate)
					.flatMap(todo -> this.service.updateIgnoreOwnership(id(req), todo)));
	}
	
	public Mono<ServerResponse> patchById(ServerRequest req) {
		return defaultOkOneResponse(
				extractBodyRequired(req)
					.doOnNext(this::validateIgnoreNull)
					.flatMap(todo -> this.service.patchIgnoreOwnership(id(req), todo)));
	}
	
	public Mono<ServerResponse> deleteById(ServerRequest req) {
		return defaultAcceptedResponse(this.service.deleteIgnoreOwnership(id(req)));
	}
	
	public Mono<ServerResponse> clear(ServerRequest req) {
		return defaultAcceptedResponse(this.service.deleteAll());
	}
	
	/**
	 * Extract the owner name to be used on insertion.
	 * This is used to spoof ownership of Todo by admins.
	 * 
	 * @param req
	 * @return owner or empty.
	 */
	private static Optional<String> forceOwner(ServerRequest req) {
		return req.queryParam("forceOwner");
	}
	
}

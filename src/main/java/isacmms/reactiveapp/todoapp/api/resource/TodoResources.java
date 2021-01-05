package isacmms.reactiveapp.todoapp.api.resource;

import java.security.Principal;
import java.time.Duration;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import isacmms.reactiveapp.todoapp.model.Todo;
import isacmms.reactiveapp.todoapp.service.TodoService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/jwt")
public class TodoResources {

	private final TodoService service;
	
	public TodoResources(TodoService service) {
		this.service = service;
	}
	
	@PreAuthorize("hasRole('ROLE_USER')")
	@RequestMapping(value = "/greet", method = RequestMethod.GET)
	public Mono<String> greet(Mono<Principal> principal) {
		return principal.flatMap(p -> Mono.just(p.getName()));
	}
	
	@RequestMapping(value = "/todos", method = RequestMethod.GET)
	public Flux<Todo> getAll() {
		return this.service.findAllIgnoreOwnership();
	}
	
	@RequestMapping(value = "/todos/{id}", method = RequestMethod.GET)
	public Mono<Todo> getById(@PathVariable String id) {
		return this.service.findByIdIgnoreOwnership(id);
	}
	
	@RequestMapping(value = "/todos", method = RequestMethod.POST)
	public Mono<Todo> create(@RequestBody Todo todo) {
		return this.service.create(todo, null);
	}
	
	@RequestMapping(value = "/todos/{id}", method = RequestMethod.PUT)
	public Mono<Todo> update(@PathVariable String id, @RequestBody Todo todo) {
		return this.service.updateIgnoreOwnership(id, todo);
	}
	
	@RequestMapping(value = "/todos/{id}", method = RequestMethod.DELETE)
	public Mono<Todo> delete(@PathVariable String id) {
		return this.service.deleteIgnoreOwnership(id);
	}
	
	@RequestMapping(value = "/todos/events", method = RequestMethod.GET, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<Todo> getEvents() {
		Flux<Long> interval = Flux.interval(Duration.ofSeconds(5L));
		Flux<Todo> todos = this.service.findAllIgnoreOwnership();
		return Flux.zip(interval, todos).map(tuple -> tuple.getT2());
		/*
		return Flux.zip(
					Flux.interval(Duration.ofSeconds(5L)), 
					this.service.findAll()
				);
		*/
	}
}

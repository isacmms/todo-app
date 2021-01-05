package isacmms.reactiveapp.todoapp.service;

import java.util.Map;

import isacmms.reactiveapp.todoapp.model.Todo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserTodoService {

	Flux<Todo> findAll(String owner, Map<String, String> rxs, String... sortProperties);
	Mono<Todo> findById(String id, String owner);
	Mono<Todo> create(Todo todo, String owner);
	Mono<Todo> update(String id, Todo dto, String owner);
	Mono<Todo> patch(String id, Todo dto, String owner);
	Mono<Todo> delete(String id, String owner);
	
}

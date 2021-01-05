package isacmms.reactiveapp.todoapp.service;

import java.util.Map;

import isacmms.reactiveapp.todoapp.model.Todo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AdminTodoService {

	Flux<Todo> findAllIgnoreOwnership(String... sortProperties);
	Flux<Todo> findAllIgnoreOwnership(Map<String, String> rxs, String... sortProperties);
	Mono<Todo> findByIdIgnoreOwnership(String id);
	Mono<Todo> create(Todo todo, String owner);
	Mono<Todo> updateIgnoreOwnership(String id, Todo dto);
	Mono<Todo> patchIgnoreOwnership(String id, Todo dto);
	Mono<Todo> deleteIgnoreOwnership(String id);
	Flux<Todo> deleteAll();
	
}

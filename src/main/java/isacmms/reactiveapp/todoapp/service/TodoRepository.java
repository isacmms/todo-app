package isacmms.reactiveapp.todoapp.service;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import isacmms.reactiveapp.todoapp.model.Todo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TodoRepository extends ReactiveMongoRepository<Todo, String>, CustomReactiveMongoRepository {
	
	//public Mono<Todo> findById(ObjectId id);
	public Flux<Todo> findByOwner(String owner);
	public Mono<Todo> findBy_idInAndOwner(String id, String owner);
	
}

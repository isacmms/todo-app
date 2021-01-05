package isacmms.reactiveapp.todoapp.service;

import java.util.Map;

import org.springframework.data.domain.Sort;

import isacmms.reactiveapp.todoapp.model.Todo;
import reactor.core.publisher.Flux;

interface CustomReactiveMongoRepository {
	
	Flux<Todo> findByRegex(Map<String, String> regexs, Sort sort);
	
}

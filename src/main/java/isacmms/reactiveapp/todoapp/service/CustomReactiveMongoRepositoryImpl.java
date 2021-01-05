package isacmms.reactiveapp.todoapp.service;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import isacmms.reactiveapp.todoapp.model.Todo;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Custom repository implementaiton for MongoTemplate using regex and sort criteria.
 * 
 * @author isacm
 *
 */
@Log4j2
@Repository
class CustomReactiveMongoRepositoryImpl implements CustomReactiveMongoRepository {
	
	private final ReactiveMongoTemplate mongoTemplate;
	
	public CustomReactiveMongoRepositoryImpl(ReactiveMongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	public Flux<Todo> findByRegex(Map<String, String> regexs, Sort sort) {
		log.debug("> CustomReactiveMongoRepositoryImpl.findByRegex()");
		return Flux.fromIterable(regexs.keySet())
			.handle((key, sink) -> {
				Pattern p;
				try {
					p = Pattern.compile(regexs.get(key), Pattern.CASE_INSENSITIVE);
				} catch (PatternSyntaxException e) {
					log.debug("Regex compilation found invalid operators. All special characters will be removed.");
					log.debug(e);
					p = Pattern.compile(regexs.get(key).replaceAll("[^a-zA-Z0-9\\s]", ""), Pattern.CASE_INSENSITIVE);
				}
				sink.next(new Criteria().and(key.split("__regex")[0]).regex(p));
			})
			/*
			.flatMap(key -> {
				Pattern p;
				try {
					p = Pattern.compile(regexs.get(key), Pattern.CASE_INSENSITIVE);
				} catch (PatternSyntaxException e) {
					e.printStackTrace();
					p = Pattern.compile(regexs.get(key).replaceAll("[^a-zA-Z0-9]", ""), Pattern.CASE_INSENSITIVE);
				}
				return Flux.just(new Criteria().and(key.split("__regex")[0]).regex(p));
			})
			*/
			.collectList() // To Mono
			.flatMap(criterias ->
				Mono.just(
					new Query()
						.with(sort)
						.addCriteria(
								new Criteria().andOperator(criterias.toArray(new Criteria[criterias.size()]))))
			)
			.switchIfEmpty(Mono.just(new Query().with(sort)))
			.flatMapMany(query -> mongoTemplate.find(query, Todo.class)); // Back to Flux
	}
	
}

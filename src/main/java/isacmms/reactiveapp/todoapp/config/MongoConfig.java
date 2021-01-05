package isacmms.reactiveapp.todoapp.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Configuration
public class MongoConfig {
	
	private ReactiveMongoTemplate mongoTemplate;
	
	public MongoConfig(ReactiveMongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	/**
	 * Mongo manual initializations as now required per 3.x versions
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void initIndicesAfterStartup() {
		log.info("Mongo InitIndicesAfterStartup init");
		Long init = System.currentTimeMillis();
		
		var mongoMappingContext = this.mongoTemplate.getConverter().getMappingContext();
		if (mongoMappingContext instanceof MappingContext) {
			for (MongoPersistentEntity<?> entity : mongoMappingContext.getPersistentEntities()) {
				var clazz = entity.getClass();
				if (clazz.isAnnotationPresent(Document.class)) {
					ReactiveIndexOperations indexOps = mongoTemplate.indexOps(clazz);
					IndexResolver resolver = new MongoPersistentEntityIndexResolver(mongoMappingContext);
					resolver.resolveIndexFor(clazz).forEach(indexOps::ensureIndex);
				}
			}
		}
		log.info("Initialized in: {} milis", (System.currentTimeMillis() - init));
	}
}

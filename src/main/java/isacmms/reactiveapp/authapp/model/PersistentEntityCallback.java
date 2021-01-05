package isacmms.reactiveapp.authapp.model;

import java.time.Instant;

import org.reactivestreams.Publisher;
import org.springframework.data.domain.Auditable;
import org.springframework.data.mongodb.core.mapping.event.ReactiveBeforeConvertCallback;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class PersistentEntityCallback implements ReactiveBeforeConvertCallback<Auditable<String, String, Instant>> {
	
	@Override
	public Publisher<Auditable<String, String, Instant>> onBeforeConvert(Auditable<String, String, Instant> entity, String collection) {
		var user = ReactiveSecurityContextHolder.getContext()
				.map(SecurityContext::getAuthentication)
				.filter(it -> it != null && it.isAuthenticated())
				.map(Authentication::getPrincipal)
				.cast(String.class)
				.map(username -> Usuario.builder().username(username).build())
				.switchIfEmpty(Mono.empty());
		
		var currentTime = Instant.now();

        if (entity.isNew()) {
            entity.setCreatedDate(currentTime);
        }
        entity.setLastModifiedDate(currentTime);
		return user.map(u -> {
			if (entity.isNew()) {
				entity.setCreatedBy(u.getUsername());
			}
			entity.setLastModifiedBy(u.getUsername());

			return entity;
		})
		.defaultIfEmpty(entity);
	}
	
}

package isacmms.reactiveapp.authapp.service;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import isacmms.reactiveapp.authapp.model.UserRole;
import reactor.core.publisher.Mono;

@Repository
interface UserRoleRepository extends ReactiveCrudRepository<UserRole, Long> {

	Mono<Void> deleteAllByUserId(Long userId);
	
}

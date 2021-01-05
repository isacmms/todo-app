package isacmms.reactiveapp.authapp.service;

import isacmms.reactiveapp.authapp.model.Usuario;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Providing "exists by" methods since the CrudRepository 
 * ones tries to convert to entity instead of boolean and
 * complain about not having a converter for entity to boolean
 * 
 * @author isacm
 *
 */
interface CustomR2dbcUserRepository {

	Mono<Boolean> existsByUsername(String username);
	Mono<Boolean> existsByEmail(String email);
	Mono<Usuario> findByUsernameIgnoreCase(String username);
	Mono<Usuario> findByEmailIgnoreCase(String email);
	Flux<Usuario> allWithRoles();
	
}

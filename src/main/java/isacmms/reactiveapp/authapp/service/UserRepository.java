package isacmms.reactiveapp.authapp.service;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import isacmms.reactiveapp.authapp.model.Usuario;

@Repository
interface UserRepository extends ReactiveCrudRepository<Usuario, Long>, CustomR2dbcUserRepository, CustomR2dbcRoleRepository {
	
	//Mono<Usuario> findByUsernameIgnoreCase(Publisher<String> username);
	//Mono<Usuario> findByUsernameIgnoreCase(String username);
	//Mono<Usuario> findByEmailIgnoreCase(Publisher<String> email);
	//Mono<Usuario> findByEmailIgnoreCase(String email);
	
}

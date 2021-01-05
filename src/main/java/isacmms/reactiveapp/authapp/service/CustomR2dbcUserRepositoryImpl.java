package isacmms.reactiveapp.authapp.service;

import java.util.List;

import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import isacmms.reactiveapp.authapp.model.Role;
import isacmms.reactiveapp.authapp.model.Usuario;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Providing exists by methods since the CrudRepository 
 * ones tries to convert to entity instead of boolean and
 * complain about not having a converter for entity to boolean
 * 
 * @author isacm
 *
 */
@Log4j2
@Repository
class CustomR2dbcUserRepositoryImpl implements CustomR2dbcUserRepository {

	private final DatabaseClient client;
	
	public CustomR2dbcUserRepositoryImpl(DatabaseClient client) {
		this.client = client;
	}
	
	@Override // findAllWithRoles
	public Flux<Usuario> allWithRoles() {
		log.debug("> CustomR2dbcRoleRepositoryImpl.findAllWithRoles()");
		return this.client
				.execute("SELECT * FROM users")
				.as(Usuario.class).fetch().all()
				.flatMap(this::fetchRoles);
	}
	
	@Override
	public Mono<Boolean> existsByUsername(String username) {
		log.debug("> CustomR2dbcRoleRepositoryImpl.existsByUsername()");
		return this.client
				.execute("SELECT EXISTS(SELECT 1 FROM users WHERE LOWER(username)=LOWER(:username))")
				.bind("username", username)
				.as(Boolean.class).fetch().one();
	}

	@Override
	public Mono<Boolean> existsByEmail(String email) {
		log.debug("> CustomR2dbcRoleRepositoryImpl.existsByEmail()");
		return this.client
				.execute("SELECT EXISTS(SELECT 1 FROM users WHERE LOWER(email)=LOWER(:email))")
				.bind("email", email)
				.as(Boolean.class).fetch().one();
	}

	@Override
	public Mono<Usuario> findByUsernameIgnoreCase(String username) {
		log.debug("> CustomR2dbcRoleRepositoryImpl.findByUsernameIgnoreCase()");
		return this.client
				.execute("SELECT * FROM users WHERE LOWER(username)=LOWER(:username)")
				.bind("username", username)
				.as(Usuario.class).fetch().one()
				
				.flatMap(this::fetchRoles);
	}
	
	@Override
	public Mono<Usuario> findByEmailIgnoreCase(String email) {
		log.debug("> CustomR2dbcRoleRepositoryImpl.findByEmailIgnoreCase()");
		return this.client
				.execute("SELECT * FROM users WHERE LOWER(email)=LOWER(:email)")
				.bind("email", email)
				.as(Usuario.class).fetch().one()
				
				.flatMap(this::fetchRoles);
	}
	
	/**
	 * The use of right join might bring duplicates if table allows
	 * 
	 * @param user
	 * @return
	 */
	private Mono<Usuario> fetchRoles(Usuario user) {
		return Mono.zip(
				Mono.just(user),
				this.client
					.execute("SELECT roles.* FROM roles RIGHT OUTER JOIN users_roles ON roles.id=users_roles.role_id WHERE users_roles.user_id=:userId")
					.bind("userId", user.getId())
					.as(Role.class).fetch().all().collectList(),
				this::aggregateRolesCombiner
		);
	}
	
	private Usuario aggregateRolesCombiner(Usuario user, List<Role> roles) {
		user.getRoles().addAll(roles);
		return user;
	}
	
}

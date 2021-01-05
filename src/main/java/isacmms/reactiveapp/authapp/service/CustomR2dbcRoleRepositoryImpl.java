package isacmms.reactiveapp.authapp.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import isacmms.reactiveapp.authapp.model.Role;
import isacmms.reactiveapp.authapp.model.Role.RoleEnum;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@Repository
class CustomR2dbcRoleRepositoryImpl implements CustomR2dbcRoleRepository {
	
	private final DatabaseClient client;
	
	public CustomR2dbcRoleRepositoryImpl(DatabaseClient client) {
		this.client = client;
	}
	/*
	@Override
	public Mono<List<Role>> findAllByUserId(Long userId) {
		log.debug("> CustomR2dbcRoleRepositoryImpl.findAllByUserId()");
		return this.client
				.execute("SELECT roles.* FROM roles LEFT OUTER JOIN users_roles ON roles.id=users_roles.role_id WHERE users_roles.user_id=:userId")
				.bind("userId", userId)
				.as(Role.class).fetch().all().collectList();
	}
	 */
	@Override
	public Mono<List<Role>> findAllRolesByNameAsList(Iterable<RoleEnum> names) {
		log.debug("> CustomR2dbcRoleRepositoryImpl.findAllByName()");
		List<String> roles = 
				StreamSupport.stream(names.spliterator(), false)
				.map(RoleEnum::name)
				.collect(Collectors.toList());
		return this.client
				.execute("SELECT * FROM roles WHERE roles.name IN(:names)")
				.bind("names", roles)
				.as(Role.class).fetch().all().collectList();
	}
	
	@Override
	public Flux<Role> findAllRolesByName(Iterable<RoleEnum> names) {
		log.debug("> CustomR2dbcRoleRepositoryImpl.findAllByName()");
		List<String> roles = 
				StreamSupport.stream(names.spliterator(), false)
				.map(RoleEnum::name)
				.collect(Collectors.toList());
		return this.client
				.execute("SELECT * FROM roles WHERE roles.name IN(:names)")
				.bind("names", roles)
				.as(Role.class).fetch().all();
	}

}

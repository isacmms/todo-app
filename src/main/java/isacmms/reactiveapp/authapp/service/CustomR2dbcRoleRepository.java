package isacmms.reactiveapp.authapp.service;

import java.util.List;

import isacmms.reactiveapp.authapp.model.Role;
import isacmms.reactiveapp.authapp.model.Role.RoleEnum;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

interface CustomR2dbcRoleRepository {
	
	Mono<List<Role>> findAllRolesByNameAsList(Iterable<RoleEnum> names);
	Flux<Role> findAllRolesByName(Iterable<RoleEnum> names);

}

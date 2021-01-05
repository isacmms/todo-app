package isacmms.reactiveapp.authapp.service;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import isacmms.reactiveapp.authapp.model.Role;
/**
 * @deprecated Functionalities from {@link CrudRepository<Role, Long>} not needed. {@link CustomR2dbcRoleRepository} implementation moved to UserRepository via Repository Composition 
 * 
 * @author isacm
 *
 */
@Deprecated
//@Repository
 interface RoleRepository extends ReactiveCrudRepository<Role, Long>, CustomR2dbcRoleRepository {
	
}

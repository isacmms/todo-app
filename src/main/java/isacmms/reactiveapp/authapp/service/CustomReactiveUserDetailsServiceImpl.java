package isacmms.reactiveapp.authapp.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * Está sendo usada mas ainda não se faz necessária. Usa UserDetails padrão.
 * Possivelmente será removida
 * 
 * @author isacm
 *
 */
@Log4j2
@Service
class CustomReactiveUserDetailsServiceImpl implements CustomReactiveUserDetailsService {

	private UserRepository repository;
	
	public CustomReactiveUserDetailsServiceImpl(UserRepository repository) {
		this.repository = repository;
	}
	
	@Transactional(readOnly = true)
	@Override
	public Mono<UserDetails> findByUsername(String username) throws UsernameNotFoundException {
		log.debug("> CustomReactiveUserDetailsServiceImpl.findByUsername()");
		return repository.findByUsernameIgnoreCase(username)
			.flatMap(usuario -> Mono.just(
					
						(UserDetails) User.builder()
								.username(usuario.getUsername())
								.password(usuario.getPassword())
								.authorities(usuario.getAuthorities())
								
								.accountExpired(!usuario.isAccountNonExpired())
								.accountLocked(!usuario.isAccountNonLocked())
								.credentialsExpired(!usuario.isCredentialsNonExpired())
								.disabled(!usuario.isEnabled())
								
								.build()))
			
			.switchIfEmpty(Mono.defer(() -> Mono.error(
							new UsernameNotFoundException(
									String.format("No user found for the username %s.", username))) ));
	}
	/*
	private Set<? extends GrantedAuthority> mapRolesToAuthorities(Set<Role> roles) {
		log.debug("> CustomReactiveUserDetailsServiceImpl.mapRolesToAuthorities()");
		return roles.stream()
				.map(role -> new SimpleGrantedAuthority(role.getName().name()))
				.collect(Collectors.toSet());
	}
	*/
	@Override
	public Mono<UserDetails> updatePassword(UserDetails user, String newPassword) {
		// TODO Auto-generated method stub
		return null;
	}

}

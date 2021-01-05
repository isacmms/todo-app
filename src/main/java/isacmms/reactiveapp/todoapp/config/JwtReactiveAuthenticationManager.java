package isacmms.reactiveapp.todoapp.config;

import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import isacmms.reactiveapp.authapp.util.JwtUtil;
import isacmms.reactiveapp.todoapp.api.ForbiddenException;
import isacmms.reactiveapp.todoapp.api.UnauthorizedException;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

/**
 * 
 * @author isacm
 *
 */
@Log4j2
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

	private static final String BAD_CREDENTIALS = "Invalid Credentials";
	
	private final JwtUtil jwtTokenUtil;

	public JwtReactiveAuthenticationManager(JwtUtil jwtUtil) {
		this.jwtTokenUtil = jwtUtil;
	}

	@Override
	public Mono<Authentication> authenticate(@NotNull Authentication authentication) {
		log.debug("> JwtReactiveAuthenticationManager.authenticate()");
		return Mono.justOrEmpty(authentication)
			.switchIfEmpty(deferUnauthorizedException())
			.flatMap(auth -> Mono.justOrEmpty(auth.getCredentials()))
			.switchIfEmpty(deferUnauthorizedException())
			.handle(this::tokenValidationHandler)
			.flatMap(this::buildAuthentication);
	}
	
	private Mono<Authentication> buildAuthentication(@NotNull Object tokenObject) {
		String token = tokenObject.toString();
		String username = jwtTokenUtil.extractUsername(token);
		
		Set<GrantedAuthority> authorities = jwtTokenUtil.extractAuthorities(token).stream()
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toSet());
		
		if (authorities.isEmpty()) {
			log.error("Anonymous authentication not supported.");
			return Mono.defer(() -> Mono.error(ForbiddenException::new)); // Anonymous authentication not supported.
		}
		
		return Mono.just(new UsernamePasswordAuthenticationToken(username, token, authorities));
	}
	
	private void tokenValidationHandler(@NotNull Object credentials, @NotNull SynchronousSink<Object> sink) {
		String token = credentials.toString();
		try {
			if (!jwtTokenUtil.validateToken(token)) {
				log.debug("\tToken is invalid.");
				sink.error(new UnauthorizedException(BAD_CREDENTIALS));
			} else {
				log.debug("\tToken is valid.");
				sink.next(token);
			}
		} catch (Exception e) {
			log.debug(e.getClass().getName());
			log.debug(e.getMessage());
			sink.error(new UnauthorizedException(e.getMessage(), e));
		}
	}
	
	private static Mono<Authentication> deferUnauthorizedException() {
		return Mono.defer(() -> Mono.error(new UnauthorizedException(BAD_CREDENTIALS)));
	}
	
}

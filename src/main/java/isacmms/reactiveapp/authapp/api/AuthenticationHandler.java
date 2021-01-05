package isacmms.reactiveapp.authapp.api;

import java.util.Date;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import isacmms.reactiveapp.authapp.model.AuthenticationRequest;
import isacmms.reactiveapp.authapp.model.AuthenticationResponse;
import isacmms.reactiveapp.authapp.util.JwtUtil;
import isacmms.reactiveapp.todoapp.api.UnauthorizedException;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
@Component
public class AuthenticationHandler {
	
	private final ReactiveAuthenticationManager authenticationManager;
	private final JwtUtil jwtTokenUtil;
	
	public AuthenticationHandler(JwtUtil jwtUtil,
			@Qualifier(value = "userDetailsAuthManager") ReactiveAuthenticationManager authenticationManager) {
		
		this.authenticationManager = authenticationManager;
		this.jwtTokenUtil = jwtUtil;
	}
	
	/**
	 * JWT authentication based on UserDetailsAuthenticationManager.
	 * 
	 * @param req
	 * @return jwt token.
	 */
	public Mono<ServerResponse> createAuthenticationToken(ServerRequest req) {
		log.debug("> AuthenticationHandler.createAuthenticationToken()");
		return extractBody(req)
			.flatMap(authReq ->
					authenticationManager.authenticate(
								authenticationToken(authReq.getUsername(), authReq.getPassword())))
			.flatMap(userPassToken -> {
				if (!(userPassToken.getPrincipal() instanceof UserDetails))
					return Mono.defer(() -> Mono.error(UnauthorizedException::new));
				UserDetails user = (UserDetails) userPassToken.getPrincipal();
				String token = jwtTokenUtil.generateToken(user, isRememberMe(req));
				return defaultAuthenticationResponse(token, jwtTokenUtil.extractExpiration(token));
			});
	}
	
	private static Mono<ServerResponse> defaultAuthenticationResponse(String token, Date expiration) {
		log.debug("> AuthenticationHandler.defaultAuthenticationResponse()");
		return ServerResponse
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(Mono.just(new AuthenticationResponse(token, expiration)), AuthenticationResponse.class);
	}
	
	private static UsernamePasswordAuthenticationToken authenticationToken(String username, String password) {
		log.debug("> AuthenticationHandler.doAuthentication()");
		return new UsernamePasswordAuthenticationToken(username, password);
	}
	
	private static Mono<AuthenticationRequest> extractBody(ServerRequest req) {
		log.debug("> AuthenticationHandler.extractAuthentication()");
		return req.bodyToMono(AuthenticationRequest.class);
	}
	
	private boolean isRememberMe(ServerRequest req) {
		log.debug("> AuthenticationHandler.isRememberMe()");
		return req.queryParam("rememberme").isPresent();
	}
	
}

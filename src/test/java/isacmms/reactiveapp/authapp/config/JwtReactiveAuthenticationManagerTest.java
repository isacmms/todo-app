package isacmms.reactiveapp.authapp.config;

import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

import isacmms.reactiveapp.authapp.model.Role.RoleEnum;
import isacmms.reactiveapp.authapp.util.JwtUtil;
import isacmms.reactiveapp.todoapp.api.ForbiddenException;
import isacmms.reactiveapp.todoapp.api.UnauthorizedException;
import isacmms.reactiveapp.todoapp.config.JwtReactiveAuthenticationManager;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * TODO Tests:
 * 0. deferException method
 * 1. Empty authentication
 * 
 * @author isacm
 *
 */
@Log4j2
@ExtendWith(MockitoExtension.class) // Tests run faster reusing same extension. Not worth using Mockito and Spring
class JwtReactiveAuthenticationManagerTest {

	@Mock
	private JwtUtil jwtTokenUtil;
	@InjectMocks
	private JwtReactiveAuthenticationManager authManager;
	
	@BeforeEach
	public void setUp(TestInfo testInfo) {
		log.info(testInfo.getDisplayName());
	}
	
	@DisplayName("Test Valid Authentication Flow")
	@Test
	public void testGivenValidAuthentication_ThenAuthenticate_ExpectAuthorityCredentialsBuilt() {
		
		Authentication userPassToken = new UsernamePasswordAuthenticationToken(
				"user", "token", 
				AuthorityUtils.createAuthorityList(RoleEnum.ROLE_USER.name()));
		
		when(jwtTokenUtil.validateToken("token"))
			.thenReturn(true);
		when(jwtTokenUtil.extractUsername("token"))
			.thenReturn("user");
		when(jwtTokenUtil.extractAuthorities("token"))
			.thenReturn(Stream.of(RoleEnum.ROLE_USER.name()).collect(Collectors.toSet()));
		
		Mono<Authentication> monoResult = this.authManager.authenticate(userPassToken);
		
		StepVerifier.create(monoResult)
	    	.assertNext(authenticationResult ->
	    		assertAll(
	    			() -> assertThat(authenticationResult, instanceOf(UsernamePasswordAuthenticationToken.class)),
	    			
	    			() -> assertEquals(userPassToken.getPrincipal().toString(), authenticationResult.getPrincipal().toString()),
	    			() -> assertEquals(userPassToken.getCredentials().toString(), authenticationResult.getCredentials().toString()),
	    			() -> assertEquals(userPassToken.getAuthorities(), authenticationResult.getAuthorities())))
	    	.expectComplete()
	    	.log()
	    	.verify();
	}
	
	@DisplayName("Test Invalid Token Exception")
	@Test
	public void testGivenInvalidToken_ThenAuthenticate_ExpectUnauthorizedExceptionWithInvalidCredentialsMessage() {
		Authentication userPassToken = new UsernamePasswordAuthenticationToken(
				"user", "token", 
				AuthorityUtils.createAuthorityList(RoleEnum.ROLE_USER.name()));
		
		when(jwtTokenUtil.validateToken("token"))
			.thenReturn(false);
		
		Mono<Authentication> monoResult = this.authManager.authenticate(userPassToken);
		
		StepVerifier.create(monoResult)
			.expectErrorSatisfies(error ->
		    	assertAll(
		    		() -> assertEquals(UnauthorizedException.class, error.getClass()),
		    		() -> assertEquals("Invalid Credentials", ((UnauthorizedException) error).getReason())))
	    	.log()
	    	.verify();
		
	}
	
	@DisplayName("Test null Authentication Exception")
	@Test
	public void testGivenNullAuthentication_ThenAuthenticate_ExpectUnauthorizedExceptionWithInvalidCredentialsMessage() {
		Mono<Authentication> monoResult = this.authManager.authenticate(null);
		
		StepVerifier.create(monoResult)
			.expectErrorSatisfies(error ->
		    	assertAll(
		    		() -> assertEquals(UnauthorizedException.class, error.getClass()),
		    		() -> assertEquals("Invalid Credentials", ((UnauthorizedException) error).getReason())))
	    	.log()
	    	.verify();
		
	}
	
	@DisplayName("Test null Credentials Exception")
	@Test
	public void testGivenAuthenticationWithNullCredentials_ThenAuthenticate_ExpectUnauthorizedExceptionWithInvalidCredentialsMessage() {
		Authentication userPassToken = new UsernamePasswordAuthenticationToken(null, null, null);

		Mono<Authentication> monoResult = this.authManager.authenticate(userPassToken);
		
		StepVerifier.create(monoResult)
			.expectErrorSatisfies(error ->
		    	assertAll(
		    		() -> assertEquals(UnauthorizedException.class, error.getClass()),
		    		() -> assertEquals("Invalid Credentials", ((UnauthorizedException) error).getReason())))
	    	.log()
	    	.verify();
		
	}
	
	@DisplayName("Test Empty Credentials Exception")
	@Test
	public void testGivenAuthenticationWithEmptyCredentials_ThenAuthenticate_ExpectUnauthorizedExceptionWithInvalidCredentialsMessage() {
		Authentication userPassToken = new UsernamePasswordAuthenticationToken("", "");

		Mono<Authentication> monoResult = this.authManager.authenticate(userPassToken);
		
		StepVerifier.create(monoResult)
			.expectErrorSatisfies(error ->
		    	assertAll(
		    		() -> assertEquals(UnauthorizedException.class, error.getClass()),
		    		() -> assertEquals("Invalid Credentials", ((UnauthorizedException) error).getReason())))
	    	.log()
	    	.verify();
		
	}
	
	@DisplayName("Test Token With No Authority Exception")
	@Test
	public void testGivenTokenWithNoAuthority_ThenAuthenticate_ExpectForbiddenExceptionWithDefaultHttpReasonPhraseMessage() {
		Authentication userPassToken = new UsernamePasswordAuthenticationToken(
				"user", "token", 
				AuthorityUtils.NO_AUTHORITIES);
		
		when(jwtTokenUtil.validateToken("token"))
			.thenReturn(true);
		when(jwtTokenUtil.extractUsername("token"))
			.thenReturn("user");
		when(jwtTokenUtil.extractAuthorities("token"))
			.thenReturn(new HashSet<>());
		
		Mono<Authentication> monoResult = this.authManager.authenticate(userPassToken);
		
		StepVerifier.create(monoResult)
			.expectErrorSatisfies(error ->
		    	assertAll(
		    		() -> assertEquals(ForbiddenException.class, error.getClass()),
		    		() -> assertEquals(
		    				HttpStatus.FORBIDDEN.getReasonPhrase(), ((ForbiddenException) error).getReason())))
	    	.log()
	    	.verify();
		
	}
	
}

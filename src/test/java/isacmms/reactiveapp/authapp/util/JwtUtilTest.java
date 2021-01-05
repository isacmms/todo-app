package isacmms.reactiveapp.authapp.util;

import static org.hamcrest.CoreMatchers.containsStringIgnoringCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import isacmms.reactiveapp.authapp.model.Role.RoleEnum;
import isacmms.reactiveapp.authapp.util.JwtUtil;
import isacmms.reactiveapp.todoapp.api.UnauthorizedException;
import isacmms.reactiveapp.todoapp.config.JwtReactiveAuthenticationManager;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Log4j2
@ContextConfiguration(classes = { JwtReactiveAuthenticationManager.class, JwtUtil.class })
@ExtendWith(SpringExtension.class)
class JwtUtilTest {
		
	@Autowired
	private JwtUtil jwtTokenUtil;
	
	//@Autowired
	private JwtReactiveAuthenticationManager authManager ;/* = 
			new JwtReactiveAuthenticationManager(
					new JwtUtil(true, "asdasdasdasdasdasdasda", SignatureAlgorithm.HS256, 1L, 1L));*/
	
	@BeforeEach
	public void setUp(TestInfo testInfo) {
		log.debug(testInfo.getDisplayName());
		this.authManager = new JwtReactiveAuthenticationManager(jwtTokenUtil);;
	}
	
	@DisplayName("Test Invalid Token Jwt Specific Exceptions")
	@Test
	public void testGivenInvalidToken_ThenAuthenticate_ExpectUnauthorizedExceptionWithBadCredentialsMessage() {
		Authentication userPassToken = new UsernamePasswordAuthenticationToken(
				"user", "token", 
				AuthorityUtils.createAuthorityList(RoleEnum.ROLE_USER.name()));
		
		Mono<Authentication> monoResult = this.authManager.authenticate(userPassToken);
		
		StepVerifier.create(monoResult)
	    	.expectErrorSatisfies(error ->
		    	assertAll(
		    		() -> assertEquals(UnauthorizedException.class, error.getClass()),
		    		() -> assertThat(
			    			((UnauthorizedException) error).getReason(), 
			    			containsStringIgnoringCase("must contain exactly 2 period characters. Found: 0"))))
	    	.log()
	    	.verify();
	}
	
}

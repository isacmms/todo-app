package isacmms.reactiveapp.authapp.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserHandlerTest {
	
	/**
	 * Field errors are mapped and should support multiple keys with multi value map like structure
	 * Should not throw IllegalStateException: Duplicate key
	 * Should return Http Unprocessable Entity 422 with more than one field error message for a single field
	 */
	@Test
	@DisplayName("Test user field validation error map supporting duplicate keys")
	void GivenUserWithMultipleErrorsOnASingleField_ThenCreateUser_ExpectedMultipleValidationMessagesForASingleField() {
		/*
		Usuario user = Usuario.builder().build();
		
		when(userRepository.existsByUsername(anyString()))
			.thenReturn(Mono.just(false));
		when(userRepository.existsByEmail(anyString()))
			.thenReturn(Mono.just(false));
		
		Mono<Usuario> monoResult = this.service.create(user);
		
		StepVerifier.create(monoResult).expectErrorMatches(error -> error instanceof)
		
		// must not throw IllegalStateException: Duplicate key
		// must throw unprocessable entity 422 with more than one error on any field
		// for fields with multiple errors like password must not be empty and size must not be less than
		 */
	}
}

package isacmms.reactiveapp.authapp.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import isacmms.reactiveapp.authapp.model.Role;
import isacmms.reactiveapp.authapp.model.Usuario;
import isacmms.reactiveapp.authapp.model.Role.RoleEnum;
import isacmms.reactiveapp.authapp.service.UserRepository;
import isacmms.reactiveapp.authapp.service.UserRoleRepository;
import isacmms.reactiveapp.authapp.service.UserService;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * 
 * @author isacm
 *
 */
@Log4j2
@ExtendWith(MockitoExtension.class)
//@ContextConfiguration(classes = { BCryptPasswordEncoder.class })
//@WebFluxTest
class UserServiceTest {
	
	@Mock
	private UserRoleRepository userRoleRepository;
	@Mock
	private UserRepository userRepository;
	//@Mock
	//private RoleRepository roleRepository;
	@Spy
	private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
	@InjectMocks
	private UserService service;
	
	// ~ Set up
	// ========================================================
	
	@BeforeEach
	public void setUp(TestInfo testInfo) {
		this.service.init(); // Initialize @PostConstruct
		log.debug(testInfo.getDisplayName());
	}

	// ~ Expected default behaviors
	// ===============================================================================================
	
	@Test
	@DisplayName("Test find all users expected success behavior")
	void testFindAllUsers_ExpectCollectionOfAllUsers() {
		final String user1 = "user1";
		final String user2 = "user2";
		final List<Usuario> users = Stream.of(
					Usuario.builder().username(user1).build(), 
					Usuario.builder().username(user2).build())
				.collect(Collectors.toList());
		
		when(this.userRepository.allWithRoles())
			.thenReturn(Flux.fromIterable(users));
		
		final Flux<Usuario> fluxResult = this.service.all();
		
		StepVerifier.create(fluxResult)	
			.assertNext(userResult -> assertEquals(user1, userResult.getUsername()))
			.assertNext(userResult -> assertEquals(user2, userResult.getUsername()))
			.expectComplete()
			.log()
			.verify();
	}
	
	@Test
	@DisplayName("Test find a user by its username success behavior")
	void testFindUserByUsername_ExpectFoundUser() {
		final String user1 = "user1";
		
		
		when(this.userRepository.findByUsernameIgnoreCase(user1))
			.thenReturn(Mono.just(Usuario.builder().username(user1).build()));
		
		final Mono<Usuario> fluxResult = this.service.findByUsername(user1);
		
		StepVerifier.create(fluxResult)	
			.assertNext(userResult -> assertEquals(user1, userResult.getUsername()))
			.expectComplete()
			.log()
			.verify();
	}
	
	@Test
	@DisplayName("Test find a user by its e-mail success behavior")
	void testFindUserByEmail_ExpectFoundUser() {
		final String email = "email";
		
		
		when(this.userRepository.findByEmailIgnoreCase(email))
			.thenReturn(Mono.just(Usuario.builder().email(email).build()));
		
		final Mono<Usuario> fluxResult = this.service.findByEmail(email);
		
		StepVerifier.create(fluxResult)	
			.assertNext(userResult -> assertEquals(email, userResult.getEmail()))
			.expectComplete()
			.log()
			.verify();
	}
	
	@Test
	@DisplayName("Test user create expected success behavior")
	void testGivenUser_ThenCreateUser_ExpectUserPreparedForCreateWithEncodedPasswordAndBasicRoles() {
		String uncodedPassword = "user";
		
		Usuario user = Usuario.builder()
				.username("user")
				.password(uncodedPassword)
				.email("user@email")
				.firstName("User")
				.lastName("Test").build();
				
		//Usuario user = new Usuario("user", uncodedPassword, "user@email", "User", "Test");
		
		Set<Role> expectedRoles = Stream.of(new Role(RoleEnum.ROLE_USER))
				.collect(Collectors.toSet());
		

		
		when(userRepository.existsByUsername(user.getUsername()))
    		.thenReturn(Mono.just(false));
		when(userRepository.existsByEmail(user.getEmail()))
			.thenReturn(Mono.just(false));
		when(userRepository.findAllRolesByName(anySet()))
    		.thenReturn(Flux.fromIterable(expectedRoles));
	    when(userRepository.save(any(Usuario.class)))
	    	.thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
	    when(userRoleRepository.saveAll(anyList()))
    		.thenAnswer(invocation -> Flux.fromIterable(invocation.getArgument(0)));
	    
	    Mono<Usuario> monoResult = service.create(user);

	    StepVerifier.create(monoResult)
	    	.assertNext(userResult ->
	    		assertAll(
	    				() -> assertEquals(user.getUsername(), userResult.getUsername()),
	    				
	    				() -> assertTrue(
	    						encoder.matches(uncodedPassword, userResult.getPassword()),
	    						() -> String.format("The expected password (%s) encoded should match the recieved (%s).", 
	    								uncodedPassword, 
	    								userResult.getPassword())),
	    				
	    				() -> assertEquals(user.getEmail(), userResult.getEmail()),
	    				() -> assertEquals(user.getFirstName(), userResult.getFirstName()),
	    				() -> assertEquals(user.getLastName(), userResult.getLastName()),
	    				
	    				() -> assertEquals(expectedRoles, userResult.getRoles())))
	    	.expectComplete()
	    	.log()
	    	.verify();
	}
	
	/**
	 * Properties not sent in the request will translate to null.
	 * Update via Http PUT expect to overwrite all entity fields.
	 * (<i>Allowed fields:</i> { password, e-mail, firstName, lasName })
	 */
	@Test
	@DisplayName("Test user update expected success behavior")
	void testGivenUserWithNullValues_ThenUpdateUser_ExpectUserWithUpdatableNewValues() {
		Set<Role> oldRoles = Stream.of(
					new Role(RoleEnum.ROLE_USER))
				.collect(Collectors.toSet());
		Set<Role> newRoles = Stream.of(
					new Role(RoleEnum.ROLE_USER), 
					new Role(RoleEnum.ROLE_ADMIN))
				.collect(Collectors.toSet());
		
		String oldUsername = "user";
		String newUsername = null;
		
		String uncodedOldPassword = "user";
		String uncodedNewPassword = ""; // 
		
		String oldEmail = "user@email";
		String newEmail = null;
		
		String oldFirstName = "User";
		String newFirstName = null;
		
		String oldLastName = "Update";
		String newLastName = null;
		
		Usuario entityToUpdate = Usuario.builder()
				.username(oldUsername)
				.password(this.encoder.encode(uncodedOldPassword))
				.email(oldEmail)
				.firstName(oldFirstName)
				.lastName(oldLastName)
				.roles(oldRoles)
				.build();
		
		Usuario dtoWithNewValues = Usuario.builder()
				.username(newUsername)
				.password(uncodedNewPassword)
				.email(newEmail)
				.firstName(newFirstName)
				.lastName(newLastName)
				.roles(newRoles)
				.build();
		
		when(this.userRepository.findByUsernameIgnoreCase(oldUsername))
			.thenReturn(Mono.just(entityToUpdate));
		when(this.userRepository.save(any(Usuario.class)))
			.thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
		
		Mono<Usuario> monoResult = service.update(oldUsername, dtoWithNewValues);
		
		StepVerifier.create(monoResult)
	    	.assertNext(userResult ->
	    		assertAll(
	    				() -> assertEquals(oldUsername, userResult.getUsername()),
	    				
	    				() -> assertTrue(
	    						encoder.matches(uncodedNewPassword, userResult.getPassword()),
	    						() -> String.format("The expected password (%s) encoded should match the recieved (%s).", 
	    								uncodedNewPassword, 
	    								userResult.getPassword())),
	    				
	    				() -> assertEquals(newEmail, userResult.getEmail()),
	    				() -> assertEquals(newFirstName, userResult.getFirstName()),
	    				() -> assertEquals(newLastName, userResult.getLastName()),
	    				() -> assertEquals(oldRoles, userResult.getRoles())))
	    	.expectComplete()
	    	.log()
	    	.verify();
		
	}
	
	/**
	 * Properties not sent in the request will translate to null value.
	 * Update via Http PATCH expect to update <i>only</i> sent properties (<b>non</b> <i>null</i>).
	 * (<i>Allowed fields:</i> { password, e-mail, firstName, lasName })
	 */
	@Test
	@DisplayName("Test user patch expected success behavior")
	void testGivenUserWithNewValues_ThenPatchUser_ExpectUserWithUpdatableNewValues_NotNull() {
		Set<Role> oldRoles = Stream.of(
					new Role(RoleEnum.ROLE_USER))
				.collect(Collectors.toSet());
		Set<Role> newRoles = Stream.of(
					new Role(RoleEnum.ROLE_USER), 
					new Role(RoleEnum.ROLE_ADMIN))
				.collect(Collectors.toSet());
		
		String oldUsername = "user";
		String newUsername = null;
		
		String uncodedOldPassword = "user";
		String uncodedNewPassword = "user2"; // 
		
		String oldEmail = "user@email";
		String newEmail = null;
		
		String oldFirstName = "User";
		String newFirstName = null;
		
		String oldLastName = "Update";
		String newLastName = null;
		
		Usuario entityToUpdate = Usuario.builder()
				.username(oldUsername)
				.password(this.encoder.encode(uncodedOldPassword))
				.email(oldEmail)
				.firstName(oldFirstName)
				.lastName(oldLastName)
				.roles(oldRoles)
				.build();
		
		Usuario dtoWithNewValues = Usuario.builder()
				.username(newUsername)
				.password(uncodedNewPassword)
				.email(newEmail)
				.firstName(newFirstName)
				.lastName(newLastName)
				.roles(newRoles)
				.build();
		
		when(this.userRepository.findByUsernameIgnoreCase(oldUsername))
			.thenReturn(Mono.just(entityToUpdate));
		when(this.userRepository.save(any(Usuario.class)))
			.thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
		
		Mono<Usuario> monoResult = service.patch(oldUsername, dtoWithNewValues);
		
		StepVerifier.create(monoResult)
	    	.assertNext(userResult ->
	    		assertAll(
	    				() -> assertEquals(oldUsername, userResult.getUsername()),
	    				
	    				() -> assertTrue(
	    						encoder.matches(uncodedNewPassword, userResult.getPassword()),
	    						() -> String.format("The expected password (%s) encoded should match the recieved (%s).", 
	    								uncodedNewPassword, 
	    								userResult.getPassword())),
	    				
	    				() -> assertEquals(oldEmail, userResult.getEmail()),
	    				() -> assertEquals(oldFirstName, userResult.getFirstName()),
	    				() -> assertEquals(oldLastName, userResult.getLastName()),
	    				() -> assertEquals(oldRoles, userResult.getRoles())))
	    	.expectComplete()
	    	.log()
	    	.verify();
	}
	
	@Test
	@DisplayName("Test user delete expected success behavior")
	void GivenUserUsername_ThenDeleteUser_ExpectEqualUserReturned() {
		String username = "user";
		String uncodedPassword = "user";
		String email = "user@email";
		String firstName = "User";
		String lastName = "ToDelete";
		Set<Role> userRoles = Stream.of(
					new  Role(RoleEnum.ROLE_USER))
				.collect(Collectors.toSet());
		
		Usuario userToBeDeleted = Usuario.builder()
				.username(username)
				.password(this.encoder.encode(uncodedPassword))
				.email(email)
				.firstName(firstName)
				.lastName(lastName)
				.roles(userRoles)
				.build();
		
		Usuario expectedUser = userToBeDeleted.toBuilder().build();
		
		when(this.userRepository.findByUsernameIgnoreCase(username))
			.thenReturn(Mono.just(userToBeDeleted));
		when(this.userRepository.delete(any(Usuario.class)))
			.thenReturn(Mono.empty());
		when(this.userRoleRepository.deleteAllByUserId(any()))
			.thenReturn(Mono.empty());
		
		Mono<Usuario> monoResult = this.service.delete(username);
		
		StepVerifier.create(monoResult)
			.assertNext(userResult -> 
				assertAll(
						() -> assertThat(userResult).usingRecursiveComparison().isEqualTo(expectedUser)))
			.expectComplete()
			.log()
			.verify();
		
	}
	
	// ~ Not expected behaviors
	// =====================================================================================================
	//@Test
	//@DisplayName("Test find a user by its username null")
	void testGivenNullUsername_ThenFindUserByUsername_ExpectEmpty() {
		
	}
	
	//@Log4j2
	//@Conte
	//@Nested
	//@ContextConfiguration(classes = { UserService.class })
	//@ExtendWith(SpringExtension.class)
	//@WebFluxTest
	//@SpringBootTest(classes = UserService.class)
	public class A {
		
		@Spy
		@Autowired
		private UserRepository userRepository;
		@InjectMocks
		private UserService service;
		
		@BeforeEach
		void setUp(TestInfo testInfo) {
			log.debug(testInfo.getDisplayName());
		}
		
		@Test
		@DisplayName("Random test")
		void testNull() {
			Usuario user = Usuario.builder().build();
			
			Mono<Usuario> monoResult = this.service.findByUsername(user.getUsername());
			
			StepVerifier.create(monoResult)
				.expectComplete()
				.log()
				.verify();
			
		}
		
	}
	
	// ~ User validations
	// =============================================================================================================
	
	@Test
	@DisplayName("Test user create with existing username")
	void testGivenUserWithExistingUsername_ThenCreateUser_ExpectUserExistsExceptionWithUsernameMessage() {
		Usuario user = Usuario.builder()
				.username("user")
				.password("user")
				.email("user@email")
				.firstName("User")
				.lastName("Test").build();
		
		when(userRepository.existsByUsername(user.getUsername()))
    		.thenReturn(Mono.just(true));
		when(userRepository.existsByEmail(user.getEmail()))
			.thenReturn(Mono.just(false));
	    
	    Mono<Usuario> monoResult = service.create(user);

	    StepVerifier.create(monoResult)
	    	.expectErrorMatches(error ->
	    		error instanceof ResponseStatusException 
	    				&& ((ResponseStatusException) error).getReason().equals("Username is taken."))
	    	.log()
	    	.verify();
	}
	
	@Test
	@DisplayName("Test user create with existing e-mail")
	void testGivenUserWithExistingUsername_ThenCreateUser_ExpectUserExistsExceptionWithEmailMessage() {
		Usuario user = Usuario.builder()
				.username("user")
				.password("user")
				.email("user@email")
				.firstName("User")
				.lastName("Test").build();
		
		when(userRepository.existsByUsername(user.getUsername()))
    		.thenReturn(Mono.just(false));
		when(userRepository.existsByEmail(user.getEmail()))
			.thenReturn(Mono.just(true));
	    
	    Mono<Usuario> monoResult = service.create(user);

	    StepVerifier.create(monoResult)
	    	.expectErrorMatches(error ->
	    		error instanceof ResponseStatusException 
	    				&& ((ResponseStatusException) error).getReason().equals("E-mail already in use."))
	    	.log()
	    	.verify();
	}
	
	@Test
	@DisplayName("Test user create password should not be null")
	void GivenUserWithNullPassword_ThenCreateUser_ExpectExceptionBeforeCreate() {
		Usuario user = Usuario.builder().username("").email("").build();
		
		when(userRepository.existsByUsername(anyString()))
			.thenReturn(Mono.just(false));
		when(userRepository.existsByEmail(anyString()))
			.thenReturn(Mono.just(false));
		
		Mono<Usuario> monoResult = this.service.create(user);
		
		StepVerifier.create(monoResult)
			.expectError(IllegalArgumentException.class)
			.log()
			.verify();
		
	}
	
	@Test
	@DisplayName("Test audit fields should not be updateable")
	void GivenUserWithNewAuditValues_ThenUpdateUser_ExpectValuesToBeIgnored() {
		String username = "user";
		
		Long oldId = 3L;
		Long newId = 1L;
		
		Long oldVersion = 5L;
		Long newVersion = 4L;
		
		Instant oldCreatedAt = ZonedDateTime.now()
				.minus(3L, ChronoUnit.MONTHS).toInstant(); // 3 months ago.
		Instant newCreatedAt = ZonedDateTime.ofInstant(oldCreatedAt, ZoneId.systemDefault())
				.minus(1L, ChronoUnit.YEARS).toInstant(); // A year before that.
		
		String oldCreatedBy = "Anonymous";
		String newCreatedBy = "admin";
		
		Instant oldLastModifiedAt = ZonedDateTime.now().minus(3L, ChronoUnit.MONTHS).toInstant(); // 3 months ago.
		Instant newLastModifiedAt = Instant.now().minus(5L, ChronoUnit.DAYS); // 5 days ago.
		
		String oldLastModifiedBy = "user";
		String newLastModifiedBy = "admin";
		/*
		Usuario entityToUpdate = Usuario.builder()
				.id(oldId)
				.version(oldVersion)
				.createdDate(oldCreatedAt)
				.lastModifiedDate(oldLastModifiedAt)
				.createdBy(oldCreatedBy)
				.lastModifiedBy(oldLastModifiedBy)
				.build();
		*/
		Usuario entityToUpdate = new Usuario();
		entityToUpdate.setId(oldId);
		entityToUpdate.setVersion(oldVersion);
		entityToUpdate.setCreatedDate(oldCreatedAt);
		entityToUpdate.setLastModifiedDate(oldLastModifiedAt);
		entityToUpdate.setCreatedBy(oldCreatedBy);
		entityToUpdate.setLastModifiedBy(oldLastModifiedBy);
		
		
		
		log.error(entityToUpdate);
		
		Usuario dtoWithNewValues = Usuario.builder()
				.id(newId)
				.version(newVersion)
				.createdDate(newCreatedAt)
				.lastModifiedDate(newLastModifiedAt)
				.createdBy(newCreatedBy)
				.lastModifiedBy(newLastModifiedBy)
				.build();
		log.error(dtoWithNewValues);
		
		when(this.userRepository.findByUsernameIgnoreCase(username))
			.thenReturn(Mono.just(entityToUpdate));
		when(this.userRepository.save(any(Usuario.class)))
			.thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
		
		Mono<Usuario> monoResult = service.update(username, dtoWithNewValues);
		
		StepVerifier.create(monoResult)
	    	.assertNext(userResult ->
	    		assertAll(
	    				() -> assertEquals(oldId, userResult.getId()),
	    				() -> assertEquals(oldVersion, userResult.getVersion()),
	    				() -> assertEquals(oldCreatedAt, userResult.getCreatedDate().get()),
	    				() -> assertEquals(oldLastModifiedAt, userResult.getLastModifiedDate().get()),
	    				() -> assertEquals(oldCreatedBy, userResult.getCreatedBy().get()),
	    				() -> assertEquals(oldLastModifiedBy, userResult.getLastModifiedBy().get())))
	    	.expectComplete()
	    	.log()
	    	.verify();
	}
	
}

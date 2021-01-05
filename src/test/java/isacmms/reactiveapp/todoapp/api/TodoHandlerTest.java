package isacmms.reactiveapp.todoapp.api;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import isacmms.reactiveapp.authapp.service.CustomReactiveUserDetailsService;
import isacmms.reactiveapp.authapp.util.JwtUtil;
import isacmms.reactiveapp.todoapp.api.AdminTodoHandler;
import isacmms.reactiveapp.todoapp.api.TodoRouter;
import isacmms.reactiveapp.todoapp.api.UserTodoHandler;
import isacmms.reactiveapp.todoapp.config.SecurityConfig;
import isacmms.reactiveapp.todoapp.model.Todo;
import isacmms.reactiveapp.todoapp.service.TodoService;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * TODO Tests:
 * Check @JsonIgnore fields. Expect null.
 * 
 * @author isacm
 *
 */
@Log4j2
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { TodoRouter.class, AdminTodoHandler.class, UserTodoHandler.class, TodoService.class, JwtUtil.class })
@WebFluxTest
@Import(SecurityConfig.class)
class TodoHandlerTest {

	//@Autowired
	//private ApplicationContext context;
	
	@MockBean
	private TodoService uTodoService;
	
	@MockBean
	private CustomReactiveUserDetailsService userDetailsService;
	
	
	//@MockBean
	//private JwtUtil jwtTokenUtil;
	
	//@MockBean//(value = TodoService.class)
	//private AdminTodoService aTodoService;
	
	@Autowired
	private WebTestClient webTestClient;

	@BeforeEach
    public void setUp() {
		//webTestClient = WebTestClient.bindToApplicationContext(context).configureClient().build();
		//webTestClient.mutate(csrf());
	}
	
	//@Test
	/*
	public void testAuth() {
		webTestClient.post().uri("/api/authenticate")
    	.bodyValue(new AuthenticationRequest("user", "user"))
    	.accept(MediaType.APPLICATION_JSON)
    	.exchange().expectStatus().isOk().expectBody(AuthenticationResponse.class)
    	.value(authResponse -> {
    		log.debug(authResponse.getToken());
    		this.authToken = authResponse.getToken();
    		Assertions.assertTrue(jwtTokenUtil.validateToken(this.authToken));
    		Assertions.assertTrue(new Date().before(authResponse.getExpiration()));
    	});
	}
	*/
	@WithMockUser(username = "user", password = "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlcyI6IlJPTEVfQURNSU4sUk9MRV9VU0VSIiwic3ViIjoiYWRtaW4iLCJpYXQiOjE1OTE2NDE0NzgsImV4cCI6MTU5NDIzMzQ3OH0.tHmoNpX7UbkeEJRoEgCJblKEfjTn4phoC9VfYZn_AXT-tWrS1IT4eJCGunkgYVY5cKEkXdNM3TgXVquSO9yUBw", authorities = {"ROLE_USER"})
	@Test
	public void testCreateTodo() {
	    Todo todo = new Todo(true, "Descrição teste");
	    Todo createdTodo = new Todo("5edecb8015224141b781b9d2", 0L, true, "Descrição teste", "user");
	    log.debug(createdTodo.getId());
	    when(uTodoService.create(any(Todo.class), anyString()))
	    	.thenReturn(Mono.just(createdTodo));
	    //webTestClient.get().cookie(name, value) csrf
	    webTestClient.post().uri("/api/todos")
	    	.accept(MediaType.APPLICATION_JSON)
	    	
	    	//.header(HttpHeaders.AUTHORIZATION, "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlcyI6IlJPTEVfQURNSU4sUk9MRV9VU0VSIiwic3ViIjoiYWRtaW4iLCJpYXQiOjE1OTE2NDE0NzgsImV4cCI6MTU5NDIzMzQ3OH0.tHmoNpX7UbkeEJRoEgCJblKEfjTn4phoC9VfYZn_AXT-tWrS1IT4eJCGunkgYVY5cKEkXdNM3TgXVquSO9yUBw")
	    	.bodyValue(todo)
	    	
	    	.exchange().expectStatus().isCreated().expectBody(Todo.class)
	    	.value(todoResponse -> {
	    		log.debug(todoResponse);
                assertEquals("5edecb8015224141b781b9d2", todoResponse.getId());
                assertEquals(null, todoResponse.get__v()); // @JsonIgnore
                assertEquals("Descrição teste", todoResponse.getDescription());
                assertEquals(true, todoResponse.getDone());
                assertEquals(null, todoResponse.getOwner()); // @JsonIgnore
            });
	}
	
	@WithMockUser(username = "user", password = "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlcyI6IlJPTEVfQURNSU4sUk9MRV9VU0VSIiwic3ViIjoiYWRtaW4iLCJpYXQiOjE1OTE2NDE0NzgsImV4cCI6MTU5NDIzMzQ3OH0.tHmoNpX7UbkeEJRoEgCJblKEfjTn4phoC9VfYZn_AXT-tWrS1IT4eJCGunkgYVY5cKEkXdNM3TgXVquSO9yUBw", authorities = "ROLE_USER")
	@Test
	public void testGetTodoById() {
	    Todo todo = new Todo(true, "Descrição teste", "user");
	    
	    when(uTodoService.findById("5edecb8015224141b781b9d2", "user"))
	    	.thenReturn(Mono.just(todo)); // found for authenticates user and existing todo
	    
	    webTestClient.get().uri("/api/todos/5edecb8015224141b781b9d2")
	    	.accept(MediaType.APPLICATION_JSON)
	    	//.header(HttpHeaders.AUTHORIZATION, this.authToken)
	    	.exchange().expectStatus().isOk().expectBody(Todo.class)
	    	.value(todoResponse -> {
                assertEquals(true, todoResponse.getDone());
                //assertEquals("user", todoResponse.getOwner());
                assertEquals("Descrição teste", todoResponse.getDescription());
            });
	}
	
	@WithMockUser(username = "user", password = "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlcyI6IlJPTEVfQURNSU4sUk9MRV9VU0VSIiwic3ViIjoiYWRtaW4iLCJpYXQiOjE1OTE2NDE0NzgsImV4cCI6MTU5NDIzMzQ3OH0.tHmoNpX7UbkeEJRoEgCJblKEfjTn4phoC9VfYZn_AXT-tWrS1IT4eJCGunkgYVY5cKEkXdNM3TgXVquSO9yUBw", authorities = "ROLE_USER")
	@Test
	public void testGivenTodoId_ThenFindTodoById_ExpectNullAuditFields() {
	    Todo todo = new Todo("5edecb8015224141b781b9d2", 2L, true, "Descrição teste", "user", Instant.now(), Instant.now(), "Anonymous", "user");
	    
	    when(uTodoService.findById("5edecb8015224141b781b9d2", "user"))
	    	.thenReturn(Mono.just(todo));
	    
	    webTestClient.get().uri("/api/todos/5edecb8015224141b781b9d2")
	    	.accept(MediaType.APPLICATION_JSON)
	    	.exchange().expectStatus().isOk().expectBody(Todo.class)
	    	.value(todoResponse -> {
                assertEquals(null, todoResponse.get__v());
                assertTrue(todoResponse.getCreatedDate().isEmpty());
                assertTrue(todoResponse.getCreatedBy().isEmpty());
                assertTrue(todoResponse.getLastModifiedBy().isEmpty());
                assertTrue(todoResponse.getLastModifiedDate().isEmpty());
            });
	}
	
}

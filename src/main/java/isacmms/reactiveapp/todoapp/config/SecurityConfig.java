package isacmms.reactiveapp.todoapp.config;

import java.time.Duration;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import isacmms.reactiveapp.authapp.filter.CustomServerBearerTokenAuthenticationConverter;
import isacmms.reactiveapp.authapp.service.CustomReactiveUserDetailsService;
import isacmms.reactiveapp.authapp.util.JwtUtil;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
	
	private static final String AUTH_WHITELIST = "/authenticate";
	
	private final CustomReactiveUserDetailsService userDetailsService;
	private final JwtUtil jwtTokenUtil;
	private final boolean CSRF = false; 

	public SecurityConfig(CustomReactiveUserDetailsService service, JwtUtil jwtUtil) {
		this.userDetailsService = service;
		this.jwtTokenUtil = jwtUtil;
	}

	/**
	 * Main configuration
	 * 
	 * @param http
	 * @return
	 * @throws Exception
	 */
	@Bean
	public SecurityWebFilterChain configure(final ServerHttpSecurity http) throws Exception {
		http
			.authorizeExchange(exchanges -> 
				exchanges
					.pathMatchers("/api/admin/**").hasRole("ADMIN")// .hasAuthority("ROLE_ADMIN")
					.pathMatchers("/api/**").hasAnyRole("ADMIN", "USER")
					.pathMatchers("/jwt/**").hasRole("ADMIN")
					.pathMatchers(HttpMethod.POST, AUTH_WHITELIST).permitAll()
					.pathMatchers("/**").authenticated()
					//.pathMatchers("/**").permitAll()
				)
			
				.addFilterAt(authenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
				
				.exceptionHandling()
					.authenticationEntryPoint((serverWebExchange, exception) -> Mono.fromRunnable(() -> {
						serverWebExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
					}))
					.accessDeniedHandler((serverWebExchange, exception) -> Mono.fromRunnable(() -> {
						serverWebExchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
					}))
					.and()

				.authenticationManager(jwtAuthManager())
				.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
				
				.formLogin().disable()
				.logout().disable()
				.httpBasic().disable()
				.cors()
					.configurationSource(corsWebFilter());

		if (!CSRF) {
			log.warn("CSRF is disabled!");
			http.csrf().disable();
		} else
			http.csrf()
				.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse());
		
		return http.build();
	}

	/**
	 * Authentication manager.
	 * auth-app domain.
	 * 
	 * @return
	 */
	@Bean("userDetailsAuthManager")
	@Qualifier(value = "userDetailsAuthManager")
	@Primary
	public ReactiveAuthenticationManager reactiveAuthenticationManager() {
		UserDetailsRepositoryReactiveAuthenticationManager userDetailsRepository = 
				new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
		userDetailsRepository.setPasswordEncoder(passwordEncoder());
		return userDetailsRepository;
	}
	
	/**
	 * Authorization manager.
	 * todo-app domain.
	 * @return
	 */
	@Bean("JwtAuthManager")
	@Qualifier(value = "JwtAuthManager")
	public ReactiveAuthenticationManager jwtAuthManager() {
		return new JwtReactiveAuthenticationManager(jwtTokenUtil);
	}
	
	/**
	 * Authorization filter.
	 * todo-app domain.
	 * @return
	 */
	@Bean
	public AuthenticationWebFilter authenticationWebFilter() {
		ServerAuthenticationConverter bearerConverter = new CustomServerBearerTokenAuthenticationConverter();
		AuthenticationWebFilter bearerAuthenticationWebFilter = new AuthenticationWebFilter(jwtAuthManager());
		
		bearerAuthenticationWebFilter.setServerAuthenticationConverter(bearerConverter);
		bearerAuthenticationWebFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/**"));

		return bearerAuthenticationWebFilter;
	}
	
	/**
	 * UserDetailsService for mocking user in memory.
	 * Test env.
	 * @return
	 */
	//@Bean
	//private MapReactiveUserDetailsService userDetailsService() {
	//	final UserDetails user = User
	//			.withUsername("user")
	//			.password(passwordEncoder().encode("password"))
	//			.roles("ADMIN")
	//			.authorities("ROLE_ADMIN")
	//			.build();
	//	return new MapReactiveUserDetailsService(user);
	//}
	

	/**
	 * Password encoder.
	 * Recommended strength 15 or more. 
	 * @return
	 */
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(14);
	}
	
	/**
	 * Cors configuration.
	 * TODO conferir a necessidade de bean no resto do código.
	 * Remover anotação @Bean caso não esteja sendo usado em outra parte de configuração.
	 * @return
	 */
	@Bean
	public CorsConfigurationSource corsWebFilter() {
		CorsConfiguration corsConfig = new CorsConfiguration();
		corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:8080"));
		corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "PUT", "DELETE"));
		corsConfig.setAllowedHeaders(Arrays.asList("Origin", "X-Requested-With", "Content-Type", "Accept", "Authorization"));
		corsConfig.setMaxAge(Duration.ofMinutes(10L));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig);

		return source;
	}

}

package isacmms.reactiveapp.authapp.api;

import java.net.URI;
import java.util.Optional;

import org.reactivestreams.Publisher;
import org.springframework.data.util.StreamUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import isacmms.reactiveapp.authapp.model.Usuario;
import isacmms.reactiveapp.authapp.service.UserService;
import isacmms.reactiveapp.todoapp.api.NotFoundException;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * 
 * @author isacm
 *
 */
@Log4j2
@Component
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserHandler {
	
	private static final String USER_NOT_FOUND = "User not found.";
	
	private final UserService service;
	private final Validator validator;
	private final ObjectMapper mapper;
	
	public UserHandler(UserService service, Validator validator, ObjectMapper mapper) {
		this.service = service;
		this.validator = validator;
		this.mapper = mapper;
	}

	public Mono<ServerResponse> get(ServerRequest req) {
		Optional<String> username = req.queryParam("username");
		Optional<String> email = req.queryParam("email");
		if (username.isPresent())
			return defaultOkOneResponse(this.service.findByUsername(username.get()));
		if (email.isPresent())
			return defaultOkOneResponse(this.service.findByEmail(email.get()));
		return defaultOkManyResponse(this.service.all());
	}
	
	public Mono<ServerResponse> getByEmail(ServerRequest req) {
		return defaultOkOneResponse(this.service.findByEmail(pathEmail(req)));
	}
	
	@Deprecated
	public Mono<ServerResponse> getByUsername(ServerRequest req) {
		return defaultOkOneResponse(this.service.findByUsername(pathUsername(req)));
	}
	
	public Mono<ServerResponse> create(ServerRequest req) {
		log.debug("> UserHandler.create()");
		return defaultCreatedResponse(
				extractBodyRequired(req)
					.doOnNext(this::validate)
					.flatMap(this.service::create));
	}
	
	public Mono<ServerResponse> update(ServerRequest req) {
		return defaultOkOneResponse(
				extractBodyRequired(req)
					.doOnNext(this::validate)
					.flatMap(usuario -> this.service.update(pathUsername(req), usuario)));
	}
	
	public Mono<ServerResponse> patch(ServerRequest req) {
		return defaultOkOneResponse(
				extractBodyRequired(req)
					.doOnNext(this::validateIgnoreNull)
					.flatMap(user -> this.service.patch(pathUsername(req), user)));
	}
	
	public Mono<ServerResponse> delete(ServerRequest req) {
		return defaultAcceptedResponse(this.service.delete(pathUsername(req)));
	}
	
	/**
	 * Default Http DELETE response.
	 * 
	 * @param publisher
	 * @return
	 */
	private static Mono<ServerResponse> defaultAcceptedResponse(Publisher<Usuario> publisher) {
		log.debug("> UserHandler.defaultAcceptedResponse()");
		return Mono.from(publisher)
				.flatMap(user -> 
					ServerResponse
						.accepted()
						.contentType(MediaType.APPLICATION_JSON)
						.body(Mono.just(user), Usuario.class))
				.switchIfEmpty(notFoundResponse());
	}
	
	/**
	 * Http GET one, PUT and PATCH response.
	 * 
	 * @param publisher
	 * @return
	 */
	private static Mono<ServerResponse> defaultOkOneResponse(Publisher<Usuario> publisher) {
		log.debug("> UserHandler.defaultOkOneResponse()");
		return Mono.from(publisher)
			.flatMap(user -> 
					ServerResponse
						.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(Mono.just(user), Usuario.class))
			.switchIfEmpty(notFoundResponse());
	}
	
	/**
	 * Default Http GET many response.
	 * 
	 * @param publisher
	 * @return
	 */
	private static Mono<ServerResponse> defaultOkManyResponse(Publisher<Usuario> publisher) {
		log.debug("> UserHandler.defaultOkManyResponse()");
		return ServerResponse
			.ok()
			.contentType(MediaType.APPLICATION_JSON)
			.body(publisher, Usuario.class);
	}
	
	/**
	 * Default Http POST response.
	 * 
	 * @param publisher
	 * @return
	 */
	private static Mono<ServerResponse> defaultCreatedResponse(Publisher<Usuario> publisher) {
		log.debug("> UserHandler.defaultCreatedResponse()");
		return Mono.from(publisher)
			.flatMap(user -> 
					ServerResponse
						.created(URI.create("/api/usuarios/" + user.getEmail()))
						.contentType(MediaType.APPLICATION_JSON)
						.body(Mono.just(user), Usuario.class));
	}
	
	/**
	 * Default Http NOT FOUND error.
	 * 
	 * @return
	 */
	private static Mono<ServerResponse> notFoundResponse() {
		return Mono.defer(() -> Mono.error(
				new NotFoundException(USER_NOT_FOUND)));
	}
	
	/**
	 * Extracts request's body. Body <i>must <b>not</b> be empty</i>.
	 * 
	 * @param req the requests to extract to body from.
	 * @return Mono of unmarshalled Usuario object or NoSuchElementException
	 */
	protected static Mono<Usuario> extractBodyRequired(ServerRequest req) {
		return req.bodyToMono(Usuario.class).single();
	}
	
	/**
	 * Extracts username from request's path variable.
	 * 
	 * @param req
	 * @return username
	 */
	private static String pathUsername(ServerRequest req) {
		return req.pathVariable("username");
	}
	
	/**
	 * Extracts email from request's path variable.
	 * 
	 * @param req
	 * @return email
	 */
	private static String pathEmail(ServerRequest req) {
		return req.pathVariable("email");
	}
	
	/**
	 * Entity field validation.
	 * Any validation error found will be mapped and an exception will be
	 * thrown as Http response <b><i>UNPROCESSABLE_ENTITY</i></b>.
	 * <i>null</i> fields will <b>not</b> be ignored.
	 * 
	 * @param user dto to validate.
	 * @return user dto validated.
	 */
	private Usuario validate(Usuario user) {
		log.debug("> UserHandler.validate()");
		return doValidation(user, false);
	}
	
	/**
	 * Entity field validation.
	 * Any validation error found will be mapped and an exception will be
	 * thrown as Http response <b><i>UNPROCESSABLE_ENTITY</i></b>.
	 * <i>null</i> fields <b>will</b> be ignored.
	 * <i>This method is specifically for patch strategy.</i>
	 * 
	 * @param user dto to validate.
	 * @return user dto validated.
	 */
	private Usuario validateIgnoreNull(Usuario user) {
		log.debug("> UserHandler.validateIgnoreNull()");
		return doValidation(user, true);
	}
	/**
	 * Field validation based on BeanPropertyBindingResult.
	 * Any validation error found will be mapped and an exception will be
	 * thrown as Http response <b><i>UNPROCESSABLE_ENTITY</i></b>.
	 * 
	 * @param user dto to validate.
	 * @param ignoreNull if null fields should be ignored.
	 * @return user dto validated.
	 */
	private Usuario doValidation(Usuario user, boolean ignoreNull) {
		log.debug("> UserHandler.doValidation()");
		final Errors errors = new BeanPropertyBindingResult(user, Usuario.class.getName());
		
		validator.validate(user, errors);
		
		if (errors == null || errors.getAllErrors().isEmpty())
			return user;
		else {
			final MultiValueMap<String, String> mappedErrors = 
					errors.getAllErrors().stream()
						.filter(err -> {
							if (ignoreNull)
								return err instanceof FieldError && ((FieldError)err).getRejectedValue() != null;
							return true;
						})
						.collect(StreamUtils.toMultiMap(
							err -> err instanceof FieldError ? 
									((FieldError) err).getField() : "Field error",
							err -> err instanceof FieldError ? 
									((FieldError) err).getDefaultMessage() : "Not valid"));
			
			if (mappedErrors.isEmpty()) {
				log.debug("Validation found errors but all were ignored.");
				errors.getAllErrors().forEach(log::debug);
				return user;
			}
			
			String errMsg = "";
			try {
				errMsg = mapper.writeValueAsString(mappedErrors);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				errMsg = mappedErrors.toString();
			}
			throw new ResponseStatusException(
						HttpStatus.UNPROCESSABLE_ENTITY, 
						errMsg);
		}
	}
}

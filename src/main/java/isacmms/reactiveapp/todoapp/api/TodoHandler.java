package isacmms.reactiveapp.todoapp.api;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.reactivestreams.Publisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import isacmms.reactiveapp.todoapp.model.Todo;
import isacmms.reactiveapp.todoapp.util.ObjectFields;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
abstract class TodoHandler {
	
	private static final String TODO_NOT_FOUND = "Todo not found.";
	
	private final Validator validator;
	private final ObjectMapper mapper;
	
	protected TodoHandler(Validator validator, ObjectMapper mapper) {
		this.validator = validator;
		this.mapper = mapper;
	}

	/**
	 * Método auxiliar para resposta padrão a métodos HTTP DELETE.
	 * 
	 * @param publisher Mono ou Flux obtido pelo retorno do service.
	 * @return Retorna o Mono de um ServerResponse configurado.
	 */
	protected static Mono<ServerResponse> defaultAcceptedResponse(Publisher<Todo> publisher) {
		return Mono.from(publisher)
			.flatMap(todo -> 
					ServerResponse
						.accepted()
						.contentType(MediaType.APPLICATION_JSON)
						.body(Mono.just(todo), Todo.class))
			.switchIfEmpty(notFoundResponse());
	}
	
	/**
	 * Http GET one, PUT and PATCH response.
	 * 
	 * @param publisher
	 * @return
	 */
	protected static Mono<ServerResponse> defaultOkOneResponse(Publisher<Todo> publisher) {
		return Mono.from(publisher)
			.flatMap(todo -> 
					ServerResponse
						.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(Mono.just(todo), Todo.class))
				.switchIfEmpty(notFoundResponse());
	}
	
	/**
	 * Default Http GET many response.
	 * 
	 * @param publisher
	 * @return
	 */
	protected static Mono<ServerResponse> defaultOkManyResponse(Publisher<Todo> publisher) {
		return ServerResponse
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(publisher, Todo.class);
	}
	
	/**
	 * Método auxiliar para resposta padrão a métodos HTTP POST
	 * 
	 * @param publisher
	 * @return
	 */
	protected static Mono<ServerResponse> defaultCreatedResponse(Publisher<Todo> publisher) {
		log.debug("> UserTodoHandler.defaultCreatedResponse()");
		return Mono.from(publisher)
				.flatMap(todo -> 
						ServerResponse
							.created(URI.create("/api/todos/" + todo.getId()))
							.contentType(MediaType.APPLICATION_JSON)
							.body(Mono.just(todo), Todo.class));
				//.switchIfEmpty(notFoundResponse());
	}
	
	/**
	 * Default Http NOT FOUND error.
	 * 
	 * @return
	 */
	protected static Mono<ServerResponse> notFoundResponse() {
		return Mono.defer(() -> Mono.error(
				new NotFoundException(TODO_NOT_FOUND)));
	}
	
	/**
	 * Extracts request's body. Body may be empty.
	 * 
	 * @param req the requests to extract to body from.
	 * @return Mono of unmarshalled Todo object or Mono.empty()
	 */
	protected static Mono<Todo> extractBody(ServerRequest req) {
		return req.bodyToMono(Todo.class);
	}
	
	/**
	 * Extracts request's body. Body <i>must <b>not</b> be empty</i>.
	 * 
	 * @param req the requests to extract to body from.
	 * @return Mono of unmarshalled Todo object or NoSuchElementException
	 */
	protected static Mono<Todo> extractBodyRequired(ServerRequest req) {
		return req.bodyToMono(Todo.class).single();
	}
	
	/**
	 * Extracts id from request path variables.
	 * 
	 * @param req
	 * @return id.
	 */
	protected static String id(ServerRequest req) {
		return req.pathVariable("id");
	}
	
	/**
	 * Método auxiliar para obter campos para serem usados de ordenação
	 * passados como parâmetros na requisição.
	 * 
	 * @param req Requisição recebida.
	 * @return Array de strings dos nomes dos campos passados na requisição.
	 */
	protected static String[] sortParams(ServerRequest req) {
		log.debug("> UserTodoHandler.sorts()");
		final Optional<String> sort = req.queryParam("sort");
		return sort.isPresent() ? sort.get().split(",") : new String[0];
	}
	
	/**
	 * O mesmo que regexParams, mas retorna um Example<Todo> ao invés de um mapa.
	 * 
	 * @param req Requisição recebida.
	 * @return Example.of(Todo) contendo as expressões no próprio campo do Todo.
	 */
	/*
	protected static Example<Todo> exampleParams(ServerRequest req) {
		log.debug("> UserTodoHandler.example()");
		final List<String> todoFields = ObjectFields.getNames(Todo.class);
		
		return Example.of(req
			.queryParams().keySet().stream()
			.filter(key -> key.endsWith("__ex") && todoFields.contains(key.split("__ex")[0]))
			.reduce(new Todo(), (acc, key) -> {
				log.debug("Key: " + key);
				String fieldName = key.split("__ex")[0];
				String regex = req.queryParam(key).get();
					try {
						Field field = acc.getClass().getDeclaredField(fieldName);
						field.setAccessible(true);
						field.set(acc, regex);
					} catch (NoSuchFieldException | SecurityException 
							| IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				return acc;
			}, (left, right) -> left));
	}
	*/
	
	/**
	 * Método auxiliar para mapear query parameters de regex na requisição.
	 * Considera parâmetros terminados em __regex.
	 * Campos não presentes no modelo serão ignorados.
	 * 
	 * @param req recebida com os parâmetros.
	 * @return Mapa final com nome dos campos como chave e suas respectivas
	 *         expressões como valor.
	 */
	protected static Map<String, String> regexParams(ServerRequest req) {
		log.debug("> UserTodoHandler.regex()");
		final List<String> todoFields = ObjectFields.getNames(Todo.class);
		
		final Map<String, String> rxs = req.queryParams().keySet().stream()
				.filter(key -> 
					key.endsWith("__regex") && 
					todoFields.contains(key.substring(0, key.indexOf("__regex"))))
				.collect(Collectors.toMap(
						key -> key.split("__regex")[0], 
						key -> req.queryParam(key).get()));
		return rxs;
	}
	
	/**
	 * Realiza validação dos campos do modelo usando BeanPropertyBindingResult e
	 * mapeia os erros específicos de cada campo.
	 * 
	 * @param todo a ser validado.
	 * @return Mapa final com nome dos campos como chave e mensagem de erro como
	 *         valor.
	 */
	public Todo validate(Todo todo) {
		log.debug("> UserTodoHandler.validate()");
		return doValidation(todo, false);
	}
	
	public Todo validateIgnoreNull(Todo todo) {
		log.debug("> UserTodoHandler.validate()");
		return doValidation(todo, true);
	}
	
	private Todo doValidation(Todo todo, boolean ignoreNull) {
		log.debug("> UserTodoHandler.doValidation()");
		final Errors errors = new BeanPropertyBindingResult(todo, Todo.class.getName());
		
		validator.validate(todo, errors);
		
		if (errors == null || errors.getAllErrors().isEmpty())
			return todo;
		else {
			final Map<String, String> mappedErrors = errors.getAllErrors()
					.stream()
					.filter(err -> {
						if (ignoreNull)
							return err instanceof FieldError && ((FieldError)err).getRejectedValue() != null;
						return true;
					})
					.collect(Collectors.toMap(
							err -> err instanceof FieldError ? 
									((FieldError) err).getField() : "Field error",
							err -> err instanceof FieldError ? 
									((FieldError) err).getDefaultMessage() : "Not valid"));
			
			if (mappedErrors.isEmpty()) {
				log.debug("Validation found errors but all were ignored.");
				errors.getAllErrors().forEach(log::debug);
				return todo;
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

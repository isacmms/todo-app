package isacmms.reactiveapp.todoapp;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mongodb.DuplicateKeyException;

import io.jsonwebtoken.MalformedJwtException;
import reactor.core.publisher.Mono;
/*
 * Not working.
 */
//@RestControllerAdvice
@Deprecated
public class GlobalErrorHandler {

	//@ExceptionHandler(MalformedJwtException.class)
	//@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Mono<Map<String, Object>> malformedJwtException(Throwable e) {
		return Mono.just(Map.of("message", "bad"));
    }
	
	//@ExceptionHandler(DuplicateKeyException.class)
	//@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	public Mono<Map<String, Object>> duplicateKeyException(Throwable e) {
		return Mono.just(Map.of("message", "bad"));
    }
	
}

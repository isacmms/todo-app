package isacmms.reactiveapp.authapp.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;

import isacmms.reactiveapp.todoapp.api.UnauthorizedException;
import reactor.core.publisher.Mono;

/**
 * Trips...
 * @author isacm
 * @deprecated Trips...
 */
@Deprecated
public class ResponseStatusExceptionUtil {
	
	public static <T extends ResponseStatusException, E> Mono<E> deferError(
			Class<T> exception, Class<E> returnType, @Nullable String message, @Nullable Throwable e) {
		try {
			Constructor<?> constructor;
			Object instance;
			if (message == null) {
				constructor = exception.getConstructor();
				instance = constructor.newInstance();
			} else if (e == null) {
				constructor = exception.getConstructor(String.class);
				instance = constructor.newInstance(message);
			} else {
				constructor = exception.getConstructor(String.class, Throwable.class);
				instance = constructor.newInstance(message, e);
			}
			final T finalInstance = exception.cast(instance);
			return Mono.defer(() -> Mono.error(finalInstance));
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException re) {
			
			return Mono.defer(() -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, message, e)));
		}
	}
	
	public static <T extends ResponseStatusException, E> Mono<E> deferError(@NotNull Class<T> exception, @NotNull Class<E> returnType, @Nullable String message) {
		return deferError(exception, returnType, message, null);
	}
	
	public static <T extends ResponseStatusException, E> Mono<E> deferError(@NotNull Class<T> exception, @NotNull Class<E> returnType) {
		return deferError(exception, returnType, null);
	}
	
	public static <T> Mono<T> deferUnauthorizedException(@NotNull Class<T> returnType, @NotNull String message, @Nullable Throwable e) {
		return Mono.defer(() -> Mono.error(new UnauthorizedException(message, e)));
	}
	
	public static <T> Mono<T> deferUnauthorizedException(@NotNull Class<T> returnType, @NotNull String message) {
		return Mono.defer(() -> Mono.error(new UnauthorizedException(message)));
	}
	
	public static <T> Mono<T> deferUnauthorizedException(@NotNull Class<T> returnType) {
		return Mono.defer(() -> Mono.error(UnauthorizedException::new));
	}
	
}

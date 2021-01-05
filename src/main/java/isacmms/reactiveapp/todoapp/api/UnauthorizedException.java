package isacmms.reactiveapp.todoapp.api;

import javax.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.server.ResponseStatusException;
/**
 * ResponseStatusException using HttpStatus 401 UNAUTHORIZED as a counterpart of {@link BadCredentialsException}
 * 
 * @author isacm
 *
 */
public class UnauthorizedException extends ResponseStatusException {
	
	private static final long serialVersionUID = 2L;

	/**
	 * Uses Unauthorized status default reason phrase.
	 */
	public UnauthorizedException() {
		this(HttpStatus.UNAUTHORIZED.getReasonPhrase());
	}
	
	public UnauthorizedException(@NotNull String message) {
		this(message, null);
	}
	
	public UnauthorizedException(@NotNull Throwable e) {
		this(HttpStatus.UNAUTHORIZED.getReasonPhrase(), e);
	}
	
	public UnauthorizedException(@NotNull String message, @Nullable Throwable e) {
		super(HttpStatus.UNAUTHORIZED, message, e);
	}

}

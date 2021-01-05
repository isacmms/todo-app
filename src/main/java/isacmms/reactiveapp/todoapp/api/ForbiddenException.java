package isacmms.reactiveapp.todoapp.api;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;

/**
 * ResponseStatusException using HttpStatus 403 FORBIDDEN
 * 
 * @author isacm
 *
 */
public class ForbiddenException extends ResponseStatusException {

	private static final long serialVersionUID = 2L;

	/**
	 * Uses Forbidden status default reason phrase.
	 */
	public ForbiddenException() {
		this(HttpStatus.FORBIDDEN.getReasonPhrase());
	}
	
	public ForbiddenException(@Nullable String message) {
		super(HttpStatus.FORBIDDEN, message);
	}
	
}

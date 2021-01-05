package isacmms.reactiveapp.todoapp.api;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;

public class NotFoundException extends ResponseStatusException {

	private static final long serialVersionUID = 7580015661051012554L;

	public NotFoundException() {
		this(HttpStatus.NOT_FOUND.getReasonPhrase());
	}
	
	public NotFoundException(@Nullable String message) {
		super(HttpStatus.NOT_FOUND, message);
		
	}
	
}

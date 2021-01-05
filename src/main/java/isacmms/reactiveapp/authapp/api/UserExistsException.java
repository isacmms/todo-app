package isacmms.reactiveapp.authapp.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UserExistsException extends ResponseStatusException {

	private static final long serialVersionUID = -6627443787140737674L;
	
	public UserExistsException() {
		this(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase());
	}
	
	public UserExistsException(String message) {
		super(HttpStatus.UNPROCESSABLE_ENTITY, message);
	}

}

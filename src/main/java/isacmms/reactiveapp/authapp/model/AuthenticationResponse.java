package isacmms.reactiveapp.authapp.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticationResponse {

	private final String token;
	private final Date expiration;
	
}

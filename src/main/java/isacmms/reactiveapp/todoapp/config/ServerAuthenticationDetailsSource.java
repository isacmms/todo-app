package isacmms.reactiveapp.todoapp.config;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.web.server.ServerWebExchange;

public class ServerAuthenticationDetailsSource
		implements AuthenticationDetailsSource<ServerWebExchange, ServerAuthenticationDetails> {

	// ~ Methods
	// ========================================================================================================

	/**
	 * @param context the {@code HttpServerRequest} object.
	 * @return the {@code ServerAuthenticationDetails} containing information about the
	 *         current request
	 */
	public ServerAuthenticationDetails buildDetails(ServerWebExchange context) {
		return new ServerAuthenticationDetails(context);
	}
}

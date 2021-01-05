package isacmms.reactiveapp.todoapp.config;

import java.io.Serializable;

import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ServerAuthenticationDetails implements Serializable {

	private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID + 1;

	// ~ Instance fields
	// ================================================================================================

	private final String remoteAddress;
	private String sessionId;

	// ~ Constructors
	// ===================================================================================================

	/**
	 * Records the remote address and will also set the session Id if a session already
	 * exists (it won't create one).
	 *
	 * @param exchange that the authentication request was received from
	 */
	public ServerAuthenticationDetails(ServerWebExchange exchange) {
		log.debug("> ServerAuthenticationDetails");
		this.remoteAddress = exchange.getRequest().getRemoteAddress().toString();
		exchange.getSession()
			.doOnNext(session -> {
				this.sessionId = (session != null) ? session.getId() : null;
			}).subscribe();
	}

	/**
	 * Constructor to add Jackson2 serialize/deserialize support
	 *
	 * @param remoteAddress remote address of current request
	 * @param sessionId session id
	 */
	private ServerAuthenticationDetails(final String remoteAddress, String sessionId) {
		this.remoteAddress = remoteAddress;
		this.sessionId = sessionId;
	}

	// ~ Methods
	// ========================================================================================================

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ServerAuthenticationDetails) {
			ServerAuthenticationDetails rhs = (ServerAuthenticationDetails) obj;

			if ((remoteAddress == null) && (rhs.getRemoteAddress() != null)) {
				return false;
			}

			if ((remoteAddress != null) && (rhs.getRemoteAddress() == null)) {
				return false;
			}

			if (remoteAddress != null) {
				if (!remoteAddress.equals(rhs.getRemoteAddress())) {
					return false;
				}
			}

			if ((sessionId == null) && (rhs.getSessionId() != null)) {
				return false;
			}

			if ((sessionId != null) && (rhs.getSessionId() == null)) {
				return false;
			}

			if (sessionId != null) {
				if (!sessionId.equals(rhs.getSessionId())) {
					return false;
				}
			}

			return true;
		}

		return false;
	}

	/**
	 * Indicates the TCP/IP address the authentication request was received from.
	 *
	 * @return the address
	 */
	public String getRemoteAddress() {
		return remoteAddress;
	}

	/**
	 * Indicates the <code>WebSession</code> id the authentication request was
	 * received from.
	 *
	 * @return the session ID
	 */
	public String getSessionId() {
		return sessionId;
	}

	@Override
	public int hashCode() {
		int code = 7654;

		if (this.remoteAddress != null) {
			code = code * (this.remoteAddress.hashCode() % 7);
		}

		if (this.sessionId != null) {
			code = code * (this.sessionId.hashCode() % 7);
		}

		return code;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString()).append(": ");
		sb.append("RemoteIpAddress: ").append(this.getRemoteAddress()).append("; ");
		sb.append("SessionId: ").append(this.getSessionId());

		return sb.toString();
	}
}

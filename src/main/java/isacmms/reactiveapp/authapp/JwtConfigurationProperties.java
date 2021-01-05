package isacmms.reactiveapp.authapp;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties("auth.jwt")
public class JwtConfigurationProperties {

	/**
	 * <p>Enable the use of a fixed static key that won't change over
	 * application executions.</p>
	 * 
	 * <p>The secret key can be configured by setting secret property.</p>
	 * 
	 * <p>Defaults to <i>false</i></p>
	 */
	private boolean useStaticSecret;

	/**
	 * <p>Static fixed key the won't change over application execution.</p>
	 * <p>use-static-secret <i>must</i> be enabled.</p>
	 * 
	 * <p>Defaults to <i>secret</i></p>
	 */
	private String secret;
	
	/**
	 * <p>JWT expiration time. <i>In minutes.</i></p>
	 * 
	 * <p>Defaults to <i>5</i> minutes.</p>
	 */
	private long expTime;
	
	/**
	 * <p>JWT expiration time for remember-me authentication.
	 * <i>In days.<i/></p>
	 * 
	 * <p><i>null</i> is disabled.</p>
	 * <p>Defaults to <i>null</i></p>
	 * 
	 */
	private long expTimeRememberMe;
	
	/**
	 * <p></p>
	 */
	private SignatureAlgorithm signatureAlgorithm;
	
}

package isacmms.reactiveapp.authapp.util;

import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;

/**
 * 
 * @author isacm
 *
 */
@Log4j2
@Component
public class JwtUtil {

	public static final String AUTHORITIES_KEY = "roles";
	
	private final boolean USE_STATIC_SECRET;
	private final String SECRET;
	private final long EXP_TIME;
	private final Long EXP_TIME_REMEMBER_ME;
	private final SignatureAlgorithm SIGN_ALGORITHM;
	
	private Key key;
	
	/**
	 * 
	 * @param use_static_secret boolean
	 * @param secret should not be null String
	 * @param signatureAlgorithm {@link SignatureAlgorithm} enum
	 * @param exp_time in minutes long
	 * @param exp_time_remember_me in days Long
	 */
	public JwtUtil(@Value("${auth.jwt.use-static-secret:#{false}}") boolean use_static_secret,
			@Value("${auth.jwt.secret:}") @NotNull String secret,
			@Value("${auth.jwt.signature-algorithm:HS512}") SignatureAlgorithm signatureAlgorithm,
			@Value("${auth.jwt.exp-time:#{5}}") long exp_time,
			@Value("${auth.jwt.exp-time-remember-me:#{null}}") Long exp_time_remember_me) {
		
		this.USE_STATIC_SECRET = use_static_secret;
		this.SECRET = secret;
		this.SIGN_ALGORITHM = signatureAlgorithm;
		
		this.EXP_TIME = 1000L * 60L * exp_time;
		
		if (exp_time_remember_me != null)
			this.EXP_TIME_REMEMBER_ME = 1000L * 60L * 60L * 24L * exp_time_remember_me;
		else {
			this.EXP_TIME_REMEMBER_ME = null;
			log.debug("Remember-me authentication is disabled.");
		}
	}
	
	@PostConstruct
	public void init() {
		byte[] keyBytes;
		
		if (!USE_STATIC_SECRET) {
			log.debug("Using a runtime generated JWT secret key.");
			SecretKey secret = Keys.secretKeyFor(SIGN_ALGORITHM);
			keyBytes = secret.getEncoded();
		} else {
			log.warn("Using a Base64-encoded static JWT secret key.");
			keyBytes = Encoders.BASE64.encode(SECRET.getBytes()).getBytes();
		}
		key = Keys.hmacShaKeyFor(keyBytes);
	}
	
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}
	
	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}
	
	public Set<String> extractAuthorities(String token) {
		String serializedRoles = extractClaim(token, 
				claims -> claims.get(AUTHORITIES_KEY, String.class));
		
		if (serializedRoles == null)
			return new HashSet<>();
		
		// remove trailing and leading characters for string with brackets from list serialization
		// String[] roles = serializedRoles.replaceAll("^.|.$/g", "").split(",");
		return  Arrays.stream(serializedRoles.split(","))
				.collect(Collectors.toSet());
	}
	
	private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}
	
	private Claims extractAllClaims(String token) throws MalformedJwtException {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
	}
	
	public String generateToken(UserDetails userDetails, boolean rememberme) {
		Map<String, Object> claims = new HashMap<>();
		
		String authorities = userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(","));
		
		claims.put(AUTHORITIES_KEY, authorities);
		return createToken(claims, userDetails.getUsername(), rememberme);
	}
	
	public String refreshToken(String token, boolean rememberme) {
		Map<String, Object> claims = new HashMap<>();
		
		claims.put(AUTHORITIES_KEY, extractAuthorities(token));
		return createToken(claims, extractUsername(token), rememberme);
	}
	
	private String createToken(Map<String, Object> claims, String subject, boolean rememberme) {
		/*
		 *  <1> claims - claims primeiro porque quando setta subject sem claim, claims defaults são gerados
		 *  <2> subject
		 *  <3> issuedAt
		 *  <4> expiration
		 *  <5> sign
		 */
		
		return Jwts.builder()
				.setClaims(claims) // <1>
				.setSubject(subject) // <2>
				.setIssuedAt(new Date( System.currentTimeMillis() )) // <3>
				.setExpiration(new Date( System.currentTimeMillis() + ( rememberme && EXP_TIME_REMEMBER_ME != null ? EXP_TIME_REMEMBER_ME : EXP_TIME ) )) // <4>
				.signWith(key, SIGN_ALGORITHM) // <5>
				.compact();
	}
	
	public Boolean validateToken(String token) {
		return !isTokenExpired(token);
	}
	
	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}
	
	private Boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}
	
}

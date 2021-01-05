package isacmms.reactiveapp.authapp.model;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonPropertyOrder({ 
	"id", "_id", "__v", "username", "password", "email", "firstName", "lastName", "fullName", "authorities", 
	"new", "createdDate", "createdBy", "lastModifiedDate", "lastModifiedBy", 
	"accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled"
})
@Table(value = "users")
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Usuario extends BaseEntity implements UserDetails {

	private static final long serialVersionUID = 7L;

	@Builder
	public Usuario(Long id, Long version, String username, String password, String email, String firstName, 
			String lastName, Instant createdDate, Instant lastModifiedDate, String createdBy, String lastModifiedBy, Set<Role> roles,
			boolean accountNonExpired, boolean accountNonLocked, boolean credentialsNonExpired, boolean enabled) {
		super(id, version, createdDate, lastModifiedDate, createdBy, lastModifiedBy);
		this.username = username;
		this.password = password;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.roles = roles == null ? new HashSet<>() : roles;
		this.accountNonExpired = accountNonExpired;
		this.accountNonLocked = accountNonLocked;
		this.credentialsNonExpired = credentialsNonExpired;
		this.enabled = enabled;
	}
	
	public Usuario(String username, String password, String email, String firstName, String lastName) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.roles = new HashSet<>();
	}
	
	

	@NotBlank
	@Size(min = 8, max = 20)
	@Setter
	@Column(value = "username")
	private String username;
	
	@JsonProperty(access = Access.WRITE_ONLY)
	@NotBlank
	@Size(min = 8, max = 20)
	@Setter
	@Column(value = "password")
	private String password;
	
	@NotBlank
	@Email
	@Setter
	@Column(value = "email")
	private String email;
	
	@NotBlank
	@Setter
	@Column(value = "first_name")
	private String firstName;
	
	@NotBlank
	@Setter
	@Column(value = "last_name")
	private String lastName;
	
	public String getFullName() {
		return String.format("%s %s", firstName, lastName);
	}
	
	public void setFullName(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}
	
	@JsonProperty("authorities")
	@Builder.Default
	@Transient
	private final Set<Role> roles = new HashSet<>();
	
	/**
	 * For Jackson deserialization. Ignores roles deserialized as null.
	 * @param roles
	 */
	@JsonSetter(contentNulls = Nulls.SKIP)
	public void setRoles(Set<Role> roles) {
		if (!this.roles.isEmpty())
			this.clearRoles();
		this.roles.addAll(roles);
	}
	
	@JsonIgnore
	@Setter
	@Builder.Default
	@Column(value = "account_non_expired")
	private boolean accountNonExpired = true;

	@JsonIgnore
	@Setter
	@Builder.Default
	@Column(value = "account_non_locked")
	private boolean accountNonLocked = true;

	@JsonIgnore
	@Setter
	@Builder.Default
	@Column(value = "credentials_non_expired")
	private boolean credentialsNonExpired = true;
	
	@JsonIgnore
	@Setter
	@Builder.Default
	@Column(value = "enabled")
	private boolean enabled = true;
	
	@JsonIgnore
	public Collection<GrantedAuthority> getAuthorities() {
		return this.roles.stream()
				.map(role -> new SimpleGrantedAuthority(role.getName().name()))
				.collect(Collectors.toSet());
	}
	
	public void clearRoles() {
		this.roles.clear();
	}
	
}

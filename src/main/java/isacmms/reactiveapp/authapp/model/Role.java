package isacmms.reactiveapp.authapp.model;

import java.io.Serializable;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Table(value = "roles")
@Data
@EqualsAndHashCode(callSuper = false)
//@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(content = Include.NON_NULL, value = Include.NON_NULL)
public class Role extends BaseEntity implements Serializable {
	
	private static final long serialVersionUID = 3L;
	
	/*
	public Role(String name) {
		log.error("> Role.constructor()");
		log.error(name);
		Optional<RoleEnum> role = 
				EnumSet.allOf(RoleEnum.class).stream()
				.filter(v -> v.name().equals(name)).findAny();
		log.error(role.isPresent());
		if (role.isPresent())
			this.name = RoleEnum.valueOf(name);
		else
			this.name = null;
	}
	*/
	
	@JsonCreator(mode = Mode.DELEGATING)
	public Role(RoleEnum name) {
		this.name = name;
	}
	
	@JsonInclude(content = Include.NON_NULL, value = Include.NON_NULL)
	@Column(value = "name")
	private RoleEnum name;
	
	@JsonValue
	public String getNameValue() {
		return this.name.name();
	}

	@JsonInclude(content = Include.NON_NULL, value = Include.NON_NULL)
	public enum RoleEnum {
		ROLE_ADMIN,
		ROLE_USER,
		@JsonEnumDefaultValue
		UNKNOWN;
	}
}

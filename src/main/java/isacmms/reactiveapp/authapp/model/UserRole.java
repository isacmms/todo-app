package isacmms.reactiveapp.authapp.model;

import java.io.Serializable;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Table(value = "users_roles")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserRole extends BaseEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Column(value = "user_id")
	private Long userId;
	
	@Column(value = "role_id")
	private Long roleId;
	
}

package isacmms.reactiveapp.todoapp.model;

import java.time.Instant;

import javax.validation.constraints.NotBlank;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Document(collection = "todos")
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class Todo extends BaseEntity {
	
	private Boolean done;
	
	@NotBlank
	private String description;

	@JsonIgnore
	private String owner;
	
	public Todo(Boolean done, String description) {
		this.done = done;
		this.description = description;
	}
	
	public Todo(String id, Long version, Boolean done, String description, String owner) {
		super(id, version);
		this.done = done;
		this.description = description;
		this.owner = owner;
	}
	
	public Todo(String id, Long version, Boolean done, String description, String owner, Instant createdAt, Instant lastModifiedAt, String createdBy, String lastModifiedBy) {
		super(id, version, createdAt, lastModifiedAt, createdBy, lastModifiedBy);
		this.done = done;
		this.description = description;
		this.owner = owner;
	}
	
}

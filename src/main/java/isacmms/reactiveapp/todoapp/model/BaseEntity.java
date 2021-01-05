package isacmms.reactiveapp.todoapp.model;

import java.time.Instant;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Auditable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
abstract class BaseEntity implements Auditable<String, String, Instant> {

	@JsonProperty("_id")
	@JsonSerialize(using = ToStringSerializer.class)
	@Getter
	@Setter
	@Id
	private ObjectId _id;
	
	@JsonIgnore
	@Setter(onMethod = @__({@Override}))
	private String createdBy;
	
	@JsonIgnore
	@Setter(onMethod = @__({@Override}))
	private Instant createdDate;
	
	@JsonIgnore
	@Setter(onMethod = @__({@Override}))
	private String lastModifiedBy;
	
	@JsonIgnore
	@Setter(onMethod = @__({@Override}))
	private Instant lastModifiedDate;
	
	@JsonIgnore
	@Getter
	@Setter
	@Version
	private Long __v;
	
	public BaseEntity(String id, Long version) {
		this(id, version, null, null, null, null);
	}
	
	public BaseEntity(String _id, Long version, Instant createdAt, Instant lastModifiedAt, String createdBy, String lastModifiedBy) {
		this._id = new ObjectId(_id);
		this.__v = version;
		this.createdDate = createdAt;
		this.lastModifiedDate = lastModifiedAt;
		this.createdBy = createdBy;
		this.lastModifiedBy = lastModifiedBy;
	}
	
	@JsonIgnore
	@Override
	public String getId() {
		if (this.get_id() == null)
			return null;
		return this.get_id().toHexString();
	}
	
	@JsonIgnore
	@Override
	public boolean isNew() {
		return _id == null;
	}

	@JsonIgnore
	@Override
	public Optional<String> getCreatedBy() {
		if (createdBy == null)
			return Optional.empty();
		return Optional.of(createdBy);
	}
	
	@JsonIgnore
	@Override
	public Optional<Instant> getCreatedDate() {
		if (createdDate == null)
			return Optional.empty();
		return Optional.of(createdDate);
	}
	
	@JsonIgnore
	@Override
	public Optional<String> getLastModifiedBy() {
		if (lastModifiedBy == null)
			return Optional.empty();
		return Optional.of(lastModifiedBy);
	}
	
	@JsonIgnore
	@Override
	public Optional<Instant> getLastModifiedDate() {
		if (lastModifiedDate == null)
			return Optional.empty();
		return Optional.of(lastModifiedDate);
	}
	
}

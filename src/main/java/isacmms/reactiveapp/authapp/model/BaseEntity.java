package isacmms.reactiveapp.authapp.model;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Auditable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

//@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode
abstract class BaseEntity implements Auditable<String, Long, Instant> {
	
	@JsonIgnore
	@Getter(onMethod = @__({@Override}))
	@Setter
	@Id
	private Long id;
	
	@JsonIgnore
	@Getter
	@Setter
	@Version
	private Long version;
	
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
	
	protected BaseEntity(Long id, Long version, Instant createdDate, Instant lastModifiedDate, String createdBy, String lastModifiedBy) {
		this.id = id;
		this.version = version;
		this.createdDate = createdDate;
		this.lastModifiedDate = lastModifiedDate;
		this.createdBy = createdBy;
		this.lastModifiedBy = lastModifiedBy;
	}
	
	protected BaseEntity(Long id, Instant createdDate, Instant lastModifiedDate, String createdBy, String lastModifiedBy) {
		this.id = id;
		this.createdDate = createdDate;
		this.lastModifiedDate = lastModifiedDate;
		this.createdBy = createdBy;
		this.lastModifiedBy = lastModifiedBy;
	}

	@JsonIgnore
	@Override
	public boolean isNew() {
		return this.id == null;
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

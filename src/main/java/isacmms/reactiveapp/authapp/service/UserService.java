package isacmms.reactiveapp.authapp.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import isacmms.reactiveapp.authapp.api.UserExistsException;
import isacmms.reactiveapp.authapp.model.Role;
import isacmms.reactiveapp.authapp.model.UserRole;
import isacmms.reactiveapp.authapp.model.Usuario;
import isacmms.reactiveapp.authapp.model.Role.RoleEnum;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@Service
public class UserService {
	
	private static final String USERNAME_TAKEN = "Username is taken.";
	private static final String EMAIL_IN_USE = "E-mail already in use.";
	private static final Set<RoleEnum> basicAuthorities = new HashSet<>();

	private final UserRepository userRepository;
	//private final RoleRepository roleRepository;
	private final UserRoleRepository userRoleRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	
	public UserService(UserRepository userRepository, 
			BCryptPasswordEncoder passwordEncoder/*, RoleRepository roleRepository*/, UserRoleRepository userRoleRepository) {
		this.userRepository = userRepository;
		//this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.userRoleRepository = userRoleRepository;
	}
	
	@PostConstruct
	public void init() {
		basicAuthorities.add(RoleEnum.ROLE_USER);
		//basicAuthorities.add(RoleEnum.ROLE_ADMIN);
	}
	
	/**
	 * Get all users.
	 * 
	 * @return all users.
	 */
	public Flux<Usuario> all() {
		log.debug("> UserService.findAll()");
		return this.userRepository.allWithRoles();
	}
	
	/**
	 * Search a user by its id.
	 * 
	 * @param id of the user.
	 * @return found user or empty.
	 */
	/*
	@Deprecated
	public Mono<Usuario> findById(final Long id) {
		log.debug("> UserService.findById()");
		return this.userRepository.findById(id);
	}
	*/
	/**
	 * Search a user by its username.
	 * 
	 * @param username of the user.
	 * @return found user or empty.
	 */
	public Mono<Usuario> findByUsername(final String username) {
		log.debug("> UserService.findByUsername()");
		return this.userRepository.findByUsernameIgnoreCase(username);
	}
	
	/**
	 * Find a user by its email.
	 * 
	 * @param email of the user.
	 * @return found user or empty.
	 */
	public Mono<Usuario> findByEmail(final String email) {
		log.debug("> UserService.findByEmail()");
		return this.userRepository.findByEmailIgnoreCase(email);
	}

	/**
	 * Creates a new user.
	 * Id will always be null.
	 * User dto will have its authorities overwritten with basic ones.
	 * Password will be encoded.
	 * Field validation is handled by the entity.
	 * 
	 * @param dto user to be created.
	 * @return entity user created.
	 */
	@Transactional
	public Mono<Usuario> create(@NotNull final Usuario dto) {
		log.debug("> UserService.create()");
		return userExistsHandler(dto)
				.doOnNext(this::prepareCreate)
				.flatMap(this::determineRoles)
				.flatMap(this.userRepository::save)
				.flatMap(this::saveRoles);
	}
	
	private Mono<Usuario> saveRoles(@NotNull Usuario user) {
		return Flux.fromIterable(user.getRoles())
				.map(role -> {
					log.error("creating UserRole");
					log.error("user id: " + user.getId());
					log.error("role id: " + role.getId());
					return new UserRole(user.getId(), role.getId());
				})
				.collectList()
				.flatMapMany(this.userRoleRepository::saveAll)
				.then(Mono.just(user));
	}
	
	/**
	 * User dto sanitization before create.
	 * Id will always be null.
	 * User dto will have its authorities overwritten with basic ones.
	 * Field validation is handled by the entity.
	 * 
	 * @param dto
	 * @return
	 */
	private void prepareCreate(@NotNull final Usuario dto) {
		log.debug("> UserService.prepareCreate()");
		if (dto.getId() != null)
			dto.setId(null);
		dto.setPassword(passwordEncoder.encode(dto.getPassword()));
	}
	
	/**
	 * Gives a user basic authorities and will accept authorities from endpoint request if 
	 * overwriteAuthorities is true (<i>default false</i>).
	 * UserService.basicAuthorities defines what is default.
	 * 
	 * @param dto user.
	 * @param overwriteAuthorities if dto authority collection should be overwritten.
	 * @return dto user with authorities.
	 */
	private Mono<Usuario> determineRoles(@NotNull final Usuario dto, final boolean overwriteAuthorities) {
		log.debug("> UserService.determineRoles()");
		
		Set<RoleEnum> authoritiesToInsert = new HashSet<>(basicAuthorities);
		log.debug(dto);
		log.debug(authoritiesToInsert);
		if (!dto.getRoles().isEmpty()) {
			if (!overwriteAuthorities) {
				List<RoleEnum> extraRoles = dto.getRoles().stream()
						.map(Role::getName)
						.collect(Collectors.toList());
				authoritiesToInsert.addAll(extraRoles);
			}
			dto.clearRoles();
		}
		log.debug(dto);
		return 
			Mono.zip(
				Mono.just(dto),
				this.userRepository.findAllRolesByName(authoritiesToInsert).collectList(),
				(t1, t2) -> {
					t1.getRoles().addAll(t2);
					return t1;
				}
			);
	}
	
	private Mono<Usuario> determineRoles(@NotNull final Usuario dto) {
		return determineRoles(dto, false);
	}
	
	/**
	 * Checks if username and email are in use.
	 * 
	 * @param dto with username and email to check.
	 * @return Tuple2 with the result values.
	 * <i>T1 is relative to username and T2 is relative to e-mail.</i>
	 */
	public Mono<Usuario> userExistsHandler(@NotNull final Usuario dto) {
		log.debug("> UserService.userExists()");
		return 
				Mono.zip(
					this.userRepository.existsByUsername(dto.getUsername()), 
					this.userRepository.existsByEmail(dto.getEmail()),
					Mono.just(dto)
				)
				.handle((tuple, sink) -> {
					if (tuple.getT1())
						sink.error(new UserExistsException(USERNAME_TAKEN));
					else if (tuple.getT2())
						sink.error(new UserExistsException(EMAIL_IN_USE));
					else
						sink.next(tuple.getT3());
				});
	}
	
	/**
	 * Update a user by overwriting its fields.
	 * <b>all</b> fields will be overwritten and
	 * <i>null</i> values will <b>not</b> be ignored.
	 * 
	 * @param username of the user.
	 * @param dto user with new values.
	 * @return entity user saved or empty if not found.
	 */
	@Transactional
	public Mono<Usuario> update(@NotNull final String username, @NotNull final Usuario dto) {
		log.debug("> UserService.update()");
		return this.userRepository.findByUsernameIgnoreCase(username)
				.doOnNext(entity -> this.updateData(entity, dto))
				.flatMap(this.userRepository::save);
	}
	
	/**
	 * Update user's name, email and password.
	 * <i>null</i> fields will <b>not</b> be ignored.
	 * 
	 * @param entity user to be updated.
	 * @param dto user with new values.
	 * @return entity user updated.
	 */
	private Usuario updateData(@NotNull final Usuario entity, @NotNull final Usuario dto) {
		int a = 100;
		char[] str = ("" + a).toCharArray();
		log.error(str);
		int sum = 0;
		for (char b : str) {
			log.error(b);
			log.error(b - '0');
			sum += b - '0';
			log.error(sum);
		}
		log.error(sum);
		if (dto.getPassword() != null)
			entity.setPassword(passwordEncoder.encode(dto.getPassword()));
		entity.setFirstName(dto.getFirstName());
		entity.setLastName(dto.getLastName());
		entity.setEmail(dto.getEmail());
		//entity.getRoles().addAll(dto.getRoles());
		return entity;
	}
	
	/**
	 * Update a user's fields.
	 * <i>null</i> fields <b>will</b> be ignored.
	 * 
	 * @param username of the user.
	 * @param dto user with new values.
	 * @return entity user saved or empty if not found.
	 */
	@Transactional
	public Mono<Usuario> patch(@NotNull final String username, @NotNull final Usuario dto) {
		log.debug("> UserService.patch()");
		return this.userRepository.findByUsernameIgnoreCase(username)
				.doOnNext(entity -> this.patchData(entity, dto))
				.flatMap(this.userRepository::save);
	}
	
	/**
	 * Update user's name, email and password.
	 * <i>null</i> fields <b>will</b> be ignored.
	 * 
	 * @param entity user to be updated.
	 * @param dto user with new values.
	 * @return entity user updated.
	 */
	private Usuario patchData(@NotNull final Usuario entity, @NotNull final Usuario dto) {
		if (dto.getPassword() != null)
			entity.setPassword(
					passwordEncoder.encode(dto.getPassword()));
		
		if (dto.getFirstName() != null)
			entity.setFirstName(dto.getFirstName());
		
		if (dto.getLastName() != null)
			entity.setLastName(dto.getLastName());
		
		if (dto.getEmail() != null)
			entity.setEmail(dto.getEmail());
		
		return entity;
	}
	
	/**
	 * Delete a user and it's related authorities.
	 * 
	 * @param username of the user.
	 * @return user deleted or empty if not found.
	 */
	@Transactional
	public Mono<Usuario> delete(@NotNull final String username) {
		log.debug("> UserService.delete()");
		return this.userRepository.findByUsernameIgnoreCase(username)
			.flatMap(user -> this.userRepository.delete(user).thenReturn(user))
			.flatMap(user -> 
				this.userRoleRepository.deleteAllByUserId(user.getId()).thenReturn(user));
							
	}
	
}

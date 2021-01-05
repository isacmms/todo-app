package isacmms.reactiveapp.todoapp.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import isacmms.reactiveapp.todoapp.api.event.TodoCreatedEvent;
import isacmms.reactiveapp.todoapp.model.Todo;
import isacmms.reactiveapp.todoapp.util.ObjectFields;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@Service
public class TodoService implements AdminTodoService, UserTodoService {
	
	private final ApplicationEventPublisher publisher;
	private final TodoRepository repository;
	
	public TodoService(ApplicationEventPublisher publisher, TodoRepository repository) {
		this.publisher = publisher;
		this.repository = repository;
	}
	
	/**
	 * Busca todos os registros usando regex que um usuário possui.
	 * Username será incluído como regex para owner de Todo.
	 * 
	 * @param owner para restringir a busca.
	 * @param rxs mapa<campo, regex> para ser utilizado na busca.
	 * Campos não existentes serão ignorados.
	 * @param sortProperties
	 * @return todos os registros encontrados.
	 */
	@Override
	public Flux<Todo> findAll(String owner, Map<String, String> rxs, String... sortProperties) {
		log.debug("> TodoService.findAll(username, regexs, ...sortProperties)");
		
		log.debug("Expressions: ");
		rxs.forEach(log::debug);
		
		rxs.put("owner", owner);
		return this.repository.findByRegex(rxs, sort(sortProperties));
	}
	
	/**
	 * Busca todos os registros.
	 * 
	 * @param sortProperties strings de nomes de campos solicitados.
	 * Strings iniciadas com sinal de menor (-) serão de ordem decrescente.
	 * Campos não existentes no objeto serão ignorados.
	 * @return todos os registros encontrados.
	 */
	@Override
	public Flux<Todo> findAllIgnoreOwnership(String... sortProperties) {
		log.debug("> TodoService.findAllIgnoreOwnership(String... sortProperties)");
		return this.findAllIgnoreOwnership(new HashMap<>(), sortProperties);
	}
	
	/**
	 * Busca todos os registros usando regex.
	 * 
	 * @param rxs mapa<campo, regex> para ser utilizado na busca.
	 * Campos não existentes serão ignorados.
	 * @param sortProperties
	 * @return todos os registros encontrados.
	 */
	@Override
	public Flux<Todo> findAllIgnoreOwnership(Map<String, String> rxs, String... sortProperties) {
		log.debug("> TodoService.findAllIgnoreOwnership(regexs, ...sortProperties)");
		
		log.debug("Expressions: ");
		rxs.forEach(log::debug);
		
		return this.repository.findByRegex(rxs, sort(sortProperties));
	}
	
	/**
	 * Busca de registro por id.
	 * 
	 * @param id do registro a ser buscado.
	 * @param owner para restringir a busca.
	 * @return registro encontrado.
	 */
	@Override
	public Mono<Todo> findById(String id, String owner) {
		log.debug("> TodoService.findById()");
		return this.repository.findBy_idInAndOwner(id, owner);
	}
	
	/**
	 * Busca de registro por id.
	 * 
	 * @param id do registro a ser buscado.
	 * @return registro encontrado.
	 */
	@Override
	public Mono<Todo> findByIdIgnoreOwnership(String id) {
		log.debug("> TodoService.findByIdIgnoreOwnership()");
		return this.repository.findById(id);
	}
	
	/**
	 * Criação de novo registro.
	 * 
	 * @param todo Objeto com campos e valores para inserção.
	 * @param owner da entidade a ser criada.
	 * @return entidade <b>salva</b> no banco de dados.
	 */
	@Override
	public Mono<Todo> create(Todo todo, String owner) {
		log.debug("> TodoService.create()");
		log.debug("Owner: ");
		log.debug(owner);
		
		if (todo.get_id() != null)
			todo.set_id(null);
		
		if (todo.getDone() == null)
			todo.setDone(false);
		
		todo.setOwner(owner);
		
        return this.repository.save(todo)
            .doOnSuccess(t -> this.publisher.publishEvent(new TodoCreatedEvent(t)));
    }
	
	/**
	 * Atualização de registro por id.
	 * <b> todos os campos serão sobrescritos</b>
	 * 
	 * @param id do registro a ser atualizado.
	 * @param dto com os campos e valores para atualização.
	 * @param owner para restringir a atualização.
	 * @return entidade atualizada <b>salva</b> no banco de dados.
	 */
	@Override
	public Mono<Todo> update(String id, Todo dto, String owner) {
		log.debug("> TodoService.update()");
		return this.repository
				.findBy_idInAndOwner(id, owner)
				.flatMap(entity -> 
						this.repository.save(updateData(entity, dto)));
	}
	
	@Override
	public Mono<Todo> updateIgnoreOwnership(String id, Todo dto) {
		log.debug("> TodoService.updateIgnoreOwnership()");
		return this.repository
			.findById(id)
			.flatMap(entity -> 
					this.repository.save(updateData(entity, dto)));
	}
	
	/**
	 * Métodos auxiliar para atualização de entidade.
	 * <b>Todos os campos serão sobrescritos</b>
	 * 
	 * @param entity a ser atualizada.
	 * @param obj com os campos para atualização.
	 * @return entidade atualizada ainda <b>não salva</b> no banco de dados.
	 */
	private Todo updateData(Todo entity, Todo obj) {
		log.debug("> TodoService.updateData()");
		entity.setDescription(obj.getDescription());
		entity.setDone(obj.getDone());
		return entity;
	}
	
	/**
	 * Atualização de registro por meio de patch.
	 * Campos nulos serão <b>ignorados</b>
	 * 
	 * @param id da entidade a ser atualizada.
	 * @param dto com o campos para atualização da entidade.
	 * @param owner para retringir a atualização.
	 * @return entidade atualizada.
	 */
	@Override
	public Mono<Todo> patch(String id, Todo dto, String owner) {
		log.debug("> TodoService.patch()");
		log.debug(dto);
		return this.repository
			.findBy_idInAndOwner(id, owner)
			.flatMap(entity ->
					this.repository.save(patchData(entity, dto)));
	}
	
	@Override
	public Mono<Todo> patchIgnoreOwnership(String id, Todo dto) {
		log.debug("> TodoService.patchIgnoreOwnership()");
		log.debug(dto);
		return this.repository
			.findById(id)
			.flatMap(entity ->
					this.repository.save(patchData(entity, dto)));
	}
	
	/**
	 * Método auxiliar para update por meio de patch.
	 * 
	 * @param entity a ser atualizada.
	 * @param obj com campos para atualização.
	 * @return entidade atualizada ainda <b>não salva</b> no banco de dados.
	 */
	private Todo patchData(Todo entity, Todo obj) {
		log.debug("> TodoService.patchData()");
		log.debug(entity);
		
		if (obj.getDescription() != null && 
				!obj.getDescription().equals(entity.getDescription()))
			entity.setDescription(obj.getDescription());
		
		if (obj.getDone() != null && 
				!obj.getDone().equals(entity.getDone()))
			entity.setDone(obj.getDone());
		
		return entity;
	}
	
	/**
	 * Deleção de registro por id.
	 * 
	 * @param id do registro a ser deletado.
	 * @param owner para restringir a deleção.
	 * @return registro deletado.
	 */
	@Override
	public Mono<Todo> delete(String id, String owner) {
		log.debug("> TodoService.delete()");
		return this.repository
			.findBy_idInAndOwner(id, owner)
			.flatMap(todo -> 
					this.repository
						.deleteById(todo.getId())
						.thenReturn(todo));
	}
	
	@Override
	public Mono<Todo> deleteIgnoreOwnership(String id) {
		log.debug("> TodoService.delete()");
		return this.repository
			.findById(id)
			.flatMap(todo -> 
					this.repository
						.deleteById(todo.getId())
						.thenReturn(todo));
	}
	
	/**
	 * Deleção de todos os registros.
	 * 
	 * @return todos os registros deletados.
	 */
	@Override
	public Flux<Todo> deleteAll() {
		log.debug("> TodoService.deleteAll()");
		return this.repository.findAll()
			.flatMap(todo -> 
					this.repository
						.deleteById(todo.getId())
						.thenReturn(todo));
	}
	
	/**
	 * Método auxiliar para gerar ordenação por campos.
	 * 
	 * @param sortProperties strings de nomes dos campos para ordenação.
	 * Strings iniciadas com sinal de menor (-) serão de ordem <b>decrescente</b>.
	 * Campos não existentes serão <b>ignorados</b>.
	 * @return Objeto Sort configurado para os campos solicitados que existem no objeto.
	 */
	private static Sort sort(String... sortProperties) {
		log.debug("> TodoService.sort()");
		
		List<String> todoFields = ObjectFields.getNames(Todo.class);
		
		log.debug("Requested properties: ");
		Arrays.stream(sortProperties)
			.forEach(prop -> log.debug("\t"+prop));
		
		log.debug("Available properties: ");
		todoFields.stream()
			.forEach(prop -> log.debug("\t"+prop));
		
		log.debug("Matching properties: ");
		Arrays.stream(sortProperties)
			.filter(prop -> prop.startsWith("-") ? todoFields.contains(prop.substring(1)) : todoFields.contains(prop))
			.forEach(prop -> log.debug("\t"+prop));
		
		return Sort.by(
				Arrays.stream(sortProperties)
					.filter(prop -> prop.startsWith("-") ? todoFields.contains(prop.substring(1)) : todoFields.contains(prop))
					.map(prop -> new Order(
							prop.startsWith("-") ? Direction.DESC : Direction.ASC, 
							prop.startsWith("-") ? prop.substring(1) : prop))
					.collect(Collectors.toList()));
	}

}

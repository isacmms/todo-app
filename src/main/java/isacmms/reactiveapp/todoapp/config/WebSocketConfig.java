package isacmms.reactiveapp.todoapp.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import isacmms.reactiveapp.todoapp.api.event.TodoCreatedEvent;
import isacmms.reactiveapp.todoapp.api.event.TodoCreatedEventPublisher;
import isacmms.reactiveapp.todoapp.model.Todo;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

@Log4j2
@Configurable
public class WebSocketConfig {

	/**
	 * Criar bean executor ???
	 * @return
	 */
	@Bean
    Executor executor() {
        return Executors.newSingleThreadExecutor();
    }
	
	/**
	 * Cria endpoint websocket ???
	 * @param webSocketHandler
	 * @return
	 */
	@Bean
	public HandlerMapping webSocketHandlerMapping(WebSocketHandler webSocketHandler) {
		return new SimpleUrlHandlerMapping() {
			{
				setUrlMap(Collections.singletonMap("/ws/todos", webSocketHandler));
				setOrder(10); // 1?
			}
		};
	}
	
	/**
	 * WebSocketHandlerAdapter ???
	   @Bean
       WebSocketHandlerAdapter webSocketHandlerAdapter() {
		   return new WebSocketHandlerAdapter();
	   }
	 */
	
	
	/**
	 * Versão ????
	 * @param objectMapper Não está sendo usado
	 * @param eventPublisher
	 * @return
	 */
	/*
	@Bean
	public WebSocketHandler webSocketHandler(ObjectMapper objectMapper, TodoCreatedEventPublisher eventPublisher) {
		Flux<TodoCreatedEvent> publish = Flux.create(eventPublisher).share();
		return session ->
			session.send(publish
						.map( evt -> 
								evt.getSource().toString()) // == Todo.toString()
						.map(msg -> 
								session.textMessage(msg)));
	}
	*/
	
	/**
	 * Publica TodoCreatedEvent ???
	 * @param objectMapper
	 * @param eventPublisher
	 * @return
	 */
	@Bean
    WebSocketHandler webSocketHandler(ObjectMapper objectMapper, TodoCreatedEventPublisher eventPublisher) {
        Flux<TodoCreatedEvent> publish = Flux.create(eventPublisher).share(); // <7>

        return session -> {
            Flux<WebSocketMessage> messageFlux = publish
            		.map(evt -> {
	            		try {
	            			Todo todo = (Todo) evt.getSource(); // <1>
	            			Map<String, String> data = new HashMap<>(); // <2>
	            			data.put("id", todo.getId());
	            			return objectMapper.writeValueAsString(data); // <3>
	            		} catch (JsonProcessingException e) {
	            			throw new RuntimeException(e);
	            		}
	            	})
	            	.map(str -> {
	            		log.info("sending " + str);
	            		return session.textMessage(str);
		           });

            return session.send(messageFlux);
        };
    }
}

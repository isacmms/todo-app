package isacmms.reactiveapp.todoapp.api.event;

import org.springframework.context.ApplicationEvent;

import isacmms.reactiveapp.todoapp.model.Todo;

public class TodoCreatedEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	public TodoCreatedEvent(Todo source) {
		super(source);
	}

}

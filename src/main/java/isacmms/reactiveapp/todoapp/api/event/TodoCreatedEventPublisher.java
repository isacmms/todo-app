package isacmms.reactiveapp.todoapp.api.event;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import reactor.core.publisher.FluxSink;

@Component
public class TodoCreatedEventPublisher implements ApplicationListener<TodoCreatedEvent>, Consumer<FluxSink<TodoCreatedEvent>> {
	
	private final Executor executor;
	private final BlockingQueue<TodoCreatedEvent> queue = new LinkedBlockingQueue<>();
	
	public TodoCreatedEventPublisher(Executor executor) {
		this.executor = executor;
	}
	
	@Override
	public void accept(FluxSink<TodoCreatedEvent> sink) {
		this.executor.execute(() -> {
            while (true)
                try {
                    TodoCreatedEvent event = queue.take(); // <5>
                    sink.next(event); // <6>
                }
                catch (InterruptedException e) {
                    ReflectionUtils.rethrowRuntimeException(e);
                }
        });
	}

	@Override
	public void onApplicationEvent(TodoCreatedEvent event) {
		this.queue.offer(event);
	}

}

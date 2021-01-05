package isacmms.reactiveapp.todoapp.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ObjectFields {
	
	/**
	 * Captura o nome dos campos declarados de um objeto por meio de reflection.
	 * @param clazz Object.class do objecto em questão.
	 * @return Lista de Strings dos nomes dos campos do objeto.
	 */
	public static List<String> getNames(Class<?> clazz) {
		log.debug("> ObjectFields.get()");
		
		return Arrays.stream(
				clazz.getDeclaredFields())
					.map(field -> field.getName())
					.collect(Collectors.toList());
	}
	
}

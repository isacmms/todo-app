package isacmms.reactiveapp.authapp.model.converter;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.r2dbc.postgresql.codec.Json;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Row;
import isacmms.reactiveapp.authapp.model.Role;
import isacmms.reactiveapp.authapp.model.UserRole;
import isacmms.reactiveapp.authapp.model.Usuario;
import isacmms.reactiveapp.authapp.model.Role.RoleEnum;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Deprecated
@Log4j2
//@ReadingConverter
public class RoleReadConverter /*implements Converter<Row, Role>*/ {
	
	//@Autowired
	private ObjectMapper mapper;
	
	public RoleReadConverter() {
	}

	//@Override
	public Role convert(Row source) {
		log.debug("> RoleReadConverter.convert()");
		
		try {
			//log.error("=============================================================================================");
			log.error(mapper.writeValueAsString(source));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		
		Role role = new Role(RoleEnum.valueOf(source.get("name", String.class)));
		//log.error(role);
		Set<Role> roles = source.get("name", Set.class);
		//log.error(roles);
		/*
		try {
			//log.error("=============================================================================================");
			log.error(mapper.writeValueAsString(source));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		*/
		//return new Usuario();
		
		Usuario user = new Usuario(
				source.get("id", Long.class), 
				source.get("version", Long.class), 
				
				source.get("username", String.class), 
				source.get("password", String.class), 
				source.get("email", String.class), 
				source.get("first_name", String.class), 
				source.get("last_name", String.class), 
				
				source.get("created_date", Instant.class), 
				source.get("last_modified_date", Instant.class),
				source.get("created_by", String.class), 
				source.get("last_modified_by", String.class), 
				
				source.get("name", Set.class),
				
				source.get("account_non_expired", boolean.class),
				source.get("account_non_locked", boolean.class), 
				source.get("credentials_non_expired", boolean.class), 
				source.get("enabled", boolean.class));
				
	    //return user;
	    
		return null;
	}

}

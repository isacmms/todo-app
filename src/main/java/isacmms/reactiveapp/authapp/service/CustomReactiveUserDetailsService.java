package isacmms.reactiveapp.authapp.service;

import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;

public interface CustomReactiveUserDetailsService extends ReactiveUserDetailsService, ReactiveUserDetailsPasswordService {
	
}

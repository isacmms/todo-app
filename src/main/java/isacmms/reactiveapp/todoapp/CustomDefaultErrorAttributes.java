package isacmms.reactiveapp.todoapp;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CustomDefaultErrorAttributes extends DefaultErrorAttributes {
	
	private final boolean includeException;
	
	public CustomDefaultErrorAttributes() {
		this.includeException = false;
	}

	@Override
	public Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
		log.debug("> CustomDefaultErrorAttributes.getErrorAttributes()");
		Map<String, Object> errorAttributes = new LinkedHashMap<>();
		errorAttributes.put("timestamp", new Date());
		errorAttributes.put("path", request.path());
		Throwable error = getError(request);
		MergedAnnotation<ResponseStatus> responseStatusAnnotation = MergedAnnotations
				.from(error.getClass(), SearchStrategy.TYPE_HIERARCHY).get(ResponseStatus.class);
		HttpStatus errorStatus = determineHttpStatus(error, responseStatusAnnotation);
		errorAttributes.put("status", errorStatus.value());
		errorAttributes.put("error", errorStatus.getReasonPhrase());
		errorAttributes.put("message", determineMessage(error, responseStatusAnnotation));
		errorAttributes.put("requestId", request.exchange().getRequest().getId());
		handleException(errorAttributes, determineException(error), includeStackTrace);
		return errorAttributes;
	}
	
	private HttpStatus determineHttpStatus(Throwable error, MergedAnnotation<ResponseStatus> responseStatusAnnotation) {
		if (error instanceof ResponseStatusException) {
			return ((ResponseStatusException) error).getStatus();
		}
		if (error instanceof AuthenticationException) {
			return HttpStatus.UNAUTHORIZED;
		}
		return responseStatusAnnotation.getValue("code", HttpStatus.class).orElse(HttpStatus.BAD_REQUEST);
	}
	
	private String determineMessage(Throwable error, MergedAnnotation<ResponseStatus> responseStatusAnnotation) {
		if (error instanceof WebExchangeBindException) {
			return error.getMessage();
		}
		if (error instanceof ResponseStatusException) {
			return ((ResponseStatusException) error).getReason();
		}
		String reason = responseStatusAnnotation.getValue("reason", String.class).orElse("");
		if (StringUtils.hasText(reason)) {
			return reason;
		}
		return (error.getMessage() != null) ? error.getMessage() : "";
	}
	
	private void handleException(Map<String, Object> errorAttributes, Throwable error, boolean includeStackTrace) {
		if (includeException) {
			errorAttributes.put("exception", error.getClass().getName());
		}
		if (includeStackTrace) {
			addStackTrace(errorAttributes, error);
		}
		if (error instanceof BindingResult) {
			BindingResult result = (BindingResult) error;
			if (result.hasErrors()) {
				errorAttributes.put("errors", result.getAllErrors());
			}
		}
	}
	
	private Throwable determineException(Throwable error) {
		if (error instanceof ResponseStatusException) {
			return (error.getCause() != null) ? error.getCause() : error;
		}
		return error;
	}
	
	private void addStackTrace(Map<String, Object> errorAttributes, Throwable error) {
		StringWriter stackTrace = new StringWriter();
		error.printStackTrace(new PrintWriter(stackTrace));
		stackTrace.flush();
		errorAttributes.put("trace", stackTrace.toString());
	}

/*
	@Override
	protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
		log.debug("> CustomDefaultErrorAttributes.getRoutingFunction()");
		return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
	}

	private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
		log.debug("> CustomDefaultErrorAttributes.renderErrorResponse()");
		Map<String, Object> errorPropertiesMap = getErrorAttributes(request, true);
		
		if ((int) errorPropertiesMap.get("status") == HttpStatus.INTERNAL_SERVER_ERROR.value()) 
			overrideErrorAttributes(errorPropertiesMap, request);

		HttpStatus status = HttpStatus.valueOf((int) errorPropertiesMap.get("status"));
		return ServerResponse
				.status(status)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Mono.just(errorPropertiesMap), Map.class);
	}

	private void overrideErrorAttributes(Map<String, Object> errorPropertiesMap, ServerRequest request) {
		log.debug("> CustomDefaultErrorAttributes.overrideErrorAttributes()");
		Throwable e = this.getError(request);
		HttpStatus status;
		if (e instanceof AuthenticationException) {
			status = HttpStatus.UNAUTHORIZED;
		} else
			status = HttpStatus.BAD_REQUEST;
		errorPropertiesMap.put("status", status.value());
		errorPropertiesMap.put("error", status.getReasonPhrase());
		//return errorPropertiesMap;
	}
*/
}

package skaro.pokedex.worker.configuration.commands.prefix;

import java.lang.invoke.MethodHandles;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;
import skaro.pokedex.sdk.worker.command.validation.ValidationFilter;

@Component
public class PrefixCharacterLimitFilter implements ValidationFilter {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private int maxLength = 4;
	
	@Override
	public Mono<AnsweredWorkRequest> filter(WorkRequest request) {
		return Mono.justOrEmpty(request.getArguments().stream().findFirst())
			.flatMap(this::verifyCharacterLimit);
	}

	private Mono<AnsweredWorkRequest> verifyCharacterLimit(String newPrefix) {
		int prefixLength = StringUtils.length(newPrefix);
		if(prefixLength <= maxLength) {
			return Mono.empty();
		}
		
		LOG.warn("Your new prefix cannot be more than {} characters", maxLength);
		AnsweredWorkRequest answer = new AnsweredWorkRequest();
		answer.setStatus(WorkStatus.BAD_REQUEST);
		return Mono.just(answer);
	}
	
}

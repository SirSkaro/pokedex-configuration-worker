package skaro.pokedex.worker.configuration.commands.prefix;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import discord4j.rest.http.client.ClientResponse;
import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.discord.DiscordMessageDirector;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;
import skaro.pokedex.sdk.worker.command.validation.ValidationFilter;

@Component
public class PrefixCharacterLimitFilter implements ValidationFilter {
	private int maxLength;
	private DiscordMessageDirector<PrefixCharacterLimitMessageContent> messageDirector;
	
	public PrefixCharacterLimitFilter(DiscordMessageDirector<PrefixCharacterLimitMessageContent> messageDirector) {
		this.maxLength = 4;
		this.messageDirector = messageDirector;
	}

	@Override
	public Mono<AnsweredWorkRequest> filter(WorkRequest request) {
		if(meetsCharacterLimit(request)) {
			return Mono.empty();
		}
		
		return sendInvalidRequestResponse(request)
				.thenReturn(createAnswer(request));
	}

	private boolean meetsCharacterLimit(WorkRequest request) {
		return request.getArguments().stream()
				.findFirst()
				.map(StringUtils::length)
				.map(prefixLength -> prefixLength <= maxLength)
				.orElse(false);
	}
	
	private Mono<ClientResponse> sendInvalidRequestResponse(WorkRequest request) {
		PrefixCharacterLimitMessageContent messageContent = new PrefixCharacterLimitMessageContent();
		messageContent.setMaxLength(maxLength);
		messageContent.setLanguage(request.getLanguage());
		
		return messageDirector.createDiscordMessage(messageContent, request.getChannelId());
	}
	
	private AnsweredWorkRequest createAnswer(WorkRequest request) {
		AnsweredWorkRequest answer = new AnsweredWorkRequest();
		answer.setStatus(WorkStatus.BAD_REQUEST);
		answer.setWorkRequest(request);
		
		return answer;
	}
	
}

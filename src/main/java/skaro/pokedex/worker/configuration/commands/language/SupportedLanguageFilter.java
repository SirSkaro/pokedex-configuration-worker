package skaro.pokedex.worker.configuration.commands.language;

import org.springframework.stereotype.Component;

import discord4j.rest.http.client.ClientResponse;
import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.client.Language;
import skaro.pokedex.sdk.discord.DiscordMessageDirector;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;
import skaro.pokedex.sdk.worker.command.validation.ValidationFilter;

@Component
public class SupportedLanguageFilter implements ValidationFilter {
	private DiscordMessageDirector<SupportedLanguageMessageContent> messageDirector;
	
	public SupportedLanguageFilter(DiscordMessageDirector<SupportedLanguageMessageContent> messageDirector) {
		this.messageDirector = messageDirector;
	}

	@Override
	public Mono<AnsweredWorkRequest> filter(WorkRequest request) {
		if(isSupportedLanguage(request)) {
			return Mono.empty();
		}
		
		return sendInvalidRequestResponse(request)
				.thenReturn(createAnswer(request));
	}
	
	private boolean isSupportedLanguage(WorkRequest request) {
		String newLanguage = request.getArguments().get(0);
		return Language.getLanguage(newLanguage).isPresent();
	}
	
	private Mono<ClientResponse> sendInvalidRequestResponse(WorkRequest request) {
		SupportedLanguageMessageContent messageContent = new SupportedLanguageMessageContent();
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

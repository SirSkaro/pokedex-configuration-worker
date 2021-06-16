package skaro.pokedex.worker.configuration.commands.language;

import static skaro.pokedex.worker.configuration.commands.language.LanguageCommandConfiguration.LANGUAGE_EXPECTED_ARGUMENT_FILTER_BEAN;
import static skaro.pokedex.worker.configuration.commands.prefix.PrefixCommandConfiguration.ADMIN_ROLE_FILTER;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.client.Language;
import skaro.pokedex.sdk.client.guild.GuildServiceClient;
import skaro.pokedex.sdk.client.guild.GuildSettings;
import skaro.pokedex.sdk.discord.DiscordMessageDirector;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;
import skaro.pokedex.sdk.worker.command.Command;
import skaro.pokedex.sdk.worker.command.validation.Filter;
import skaro.pokedex.sdk.worker.command.validation.ValidationFilterChain;
import skaro.pokedex.sdk.worker.command.validation.common.DiscordPermissionsFilter;
import skaro.pokedex.sdk.worker.command.validation.common.ExpectedArgumentsFilter;

@Component("languageCommand")
@ValidationFilterChain({
	@Filter(value = ExpectedArgumentsFilter.class, beanName = LANGUAGE_EXPECTED_ARGUMENT_FILTER_BEAN),
	@Filter(SupportedLanguageFilter.class),
	@Filter(value = DiscordPermissionsFilter.class, beanName = ADMIN_ROLE_FILTER)
})
public class LanguageCommand implements Command {
	private static final int LANGUAGE_ARGUMENT_INDEX = 0;
	
	private DiscordMessageDirector<LanguageChangeMessageContent> director;
	private GuildServiceClient client;
	
	public LanguageCommand(DiscordMessageDirector<LanguageChangeMessageContent> director, GuildServiceClient client) {
		this.director = director;
		this.client = client;
	}

	@Override
	public Mono<AnsweredWorkRequest> execute(WorkRequest request) {
		Language newLanguage = getLanguageFromArguments(request);
		LanguageChangeMessageContent messageContent = new LanguageChangeMessageContent();
		messageContent.setLanguage(newLanguage);
		
		return updateGuildSettings(request, newLanguage)
				.then(Mono.defer(() -> director.createDiscordMessage(messageContent, request.getChannelId())))
				.thenReturn(createAnswer(request));
	}
	
	private Language getLanguageFromArguments(WorkRequest request) {
		String languageArgument = request.getArguments().get(LANGUAGE_ARGUMENT_INDEX);
		return Language.getLanguage(languageArgument).get();
	}
	
	private Mono<GuildSettings> updateGuildSettings(WorkRequest request, Language newLanguage) {
		String guildId = request.getGuildId();
		
		return client.getSettings(guildId)
				.flatMap(guildSettings -> updateGuildLanguage(guildSettings, guildId, newLanguage))
				.switchIfEmpty(Mono.defer(() -> createNewGuildSettings(guildId, newLanguage)));
	}
	
	private AnsweredWorkRequest createAnswer(WorkRequest request) {
		AnsweredWorkRequest answer = new AnsweredWorkRequest();
		answer.setStatus(WorkStatus.SUCCESS);
		answer.setWorkRequest(request);
		
		return answer;
	}
	
	private Mono<GuildSettings> updateGuildLanguage(GuildSettings guildSettings, String guildId, Language newLanguage) {
		guildSettings.setLanguage(newLanguage);
		return client.saveSettings(guildId, guildSettings);
	}
	
	private Mono<GuildSettings> createNewGuildSettings(String guildId, Language language) {
		GuildSettings newSettings = new GuildSettings();
		newSettings.setLanguage(language);
		
		return client.saveSettings(guildId, newSettings);
	}

}

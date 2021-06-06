package skaro.pokedex.worker.configuration.commands.prefix;

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

@Component("prefixCommand")
@ValidationFilterChain({
	@Filter(ExpectedArgumentsFilter.class),
	@Filter(PrefixCharacterLimitFilter.class),
	@Filter(value = DiscordPermissionsFilter.class, beanName = ADMIN_ROLE_FILTER)
})
public class PrefixCommand implements Command {
	private DiscordMessageDirector<PrefixChangeMessageContent> director;
	private GuildServiceClient client;
	
	public PrefixCommand(DiscordMessageDirector<PrefixChangeMessageContent> director, GuildServiceClient client) {
		this.director = director;
		this.client = client;
	}

	@Override
	public Mono<AnsweredWorkRequest> execute(WorkRequest request) {
		String newPrefix = request.getArguments().get(0);
		PrefixChangeMessageContent messageContent = new PrefixChangeMessageContent();
		messageContent.setLanguage(request.getLanguage());
		messageContent.setNewPrefix(newPrefix);
		
		return updateGuildSettings(request, newPrefix)
			.then(Mono.defer(() -> director.createDiscordMessage(messageContent, request.getChannelId())))
			.thenReturn(createAnswer(request));
	}
	
	private Mono<GuildSettings> updateGuildSettings(WorkRequest request, String newPrefix) {
		String guildId = request.getGuildId();
		
		return client.getSettings(guildId)
				.flatMap(guildSettings -> updateGuildPrefix(guildSettings, guildId, newPrefix))
				.switchIfEmpty(createNewGuildSettings(guildId, newPrefix, request.getLanguage()));
	}
	
	private Mono<GuildSettings> updateGuildPrefix(GuildSettings guildSettings, String guildId, String newPrefix) {
		guildSettings.setPrefix(newPrefix);
		return client.saveSettings(guildId, guildSettings);
	}
	
	private Mono<GuildSettings> createNewGuildSettings(String guildId, String newPrefix, Language language) {
		GuildSettings newSettings = new GuildSettings();
		newSettings.setPrefix(newPrefix);
		newSettings.setLanguage(language);
		
		return client.saveSettings(guildId, newSettings);
	}
	
	private AnsweredWorkRequest createAnswer(WorkRequest request) {
		AnsweredWorkRequest answer = new AnsweredWorkRequest();
		answer.setStatus(WorkStatus.SUCCESS);
		answer.setWorkRequest(request);
		
		return answer;
	}
	
}

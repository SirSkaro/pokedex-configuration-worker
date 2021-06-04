package skaro.pokedex.worker.configuration.commands.prefix;

import static skaro.pokedex.worker.configuration.commands.prefix.PrefixCommandConfiguration.ADMIN_ROLE_FILTER;
import static skaro.pokedex.worker.configuration.commands.prefix.PrefixCommandConfiguration.COMMAND_LOCALE_SPEC_BEAN;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Mono;
import skaro.pokedex.sdk.client.Language;
import skaro.pokedex.sdk.client.guild.GuildServiceClient;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;
import skaro.pokedex.sdk.worker.command.Command;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedLocaleSpec;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedSpec;
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

	private Router router;
	private DiscordEmbedLocaleSpec localeSpec;
	private GuildServiceClient client;
	
	public PrefixCommand(Router router, @Qualifier(COMMAND_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec localeSpec, GuildServiceClient client) {
		this.router = router;
		this.localeSpec = localeSpec;
		this.client = client;
	}

	@Override
	public Mono<AnsweredWorkRequest> execute(WorkRequest request) {
		DiscordEmbedSpec embedSpec = localeSpec.getEmbedSpecs().get(Language.ENGLISH);
		
		MessageCreateRequest response = MessageCreateRequest.builder()
				.embed(EmbedData.builder()
						.title(String.format(embedSpec.getTitle(), request.getArguments().get(0)))
						.build())
				.build();
		
		AnsweredWorkRequest answer = new AnsweredWorkRequest();
		answer.setStatus(WorkStatus.SUCCESS);
		answer.setWorkRequest(request);
		
		return Routes.MESSAGE_CREATE.newRequest(request.getChannelId())
			.body(response)
			.exchange(router)
			.mono()
			.thenReturn(answer);
	}

}

package skaro.pokedex.worker.configuration.commands.prefix;

import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.MessageCreateRequest;
import skaro.pokedex.sdk.discord.MessageBuilder;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedLocaleSpec;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedSpec;

public class PrefixChangeMessageBuilder implements MessageBuilder<PrefixChangeMessageContent> {
	private DiscordEmbedLocaleSpec localeSpec;

	public PrefixChangeMessageBuilder(DiscordEmbedLocaleSpec localeSpec) {
		this.localeSpec = localeSpec;
	}

	@Override
	public MessageCreateRequest populateFrom(PrefixChangeMessageContent messageContent) {
		DiscordEmbedSpec embedSpec = localeSpec.getEmbedSpecs().get(messageContent.getLanguage());

		return MessageCreateRequest.builder()
				.embed(EmbedData.builder()
						.color(localeSpec.getColor())
						.title(formatTitle(messageContent, embedSpec))
						.build())
				.build();
	}
	
	private String formatTitle(PrefixChangeMessageContent messageContent, DiscordEmbedSpec embedSpec) {
		return String.format(embedSpec.getTitle(), messageContent.getNewPrefix());
	}

}

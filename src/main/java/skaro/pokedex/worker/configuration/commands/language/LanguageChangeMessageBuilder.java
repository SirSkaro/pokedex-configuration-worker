package skaro.pokedex.worker.configuration.commands.language;

import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.MessageCreateRequest;
import skaro.pokedex.sdk.discord.MessageBuilder;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedLocaleSpec;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedSpec;

public class LanguageChangeMessageBuilder implements MessageBuilder<LanguageChangeMessageContent> {
	private DiscordEmbedLocaleSpec localeSpec;
	
	public LanguageChangeMessageBuilder(DiscordEmbedLocaleSpec localeSpec) {
		this.localeSpec = localeSpec;
	}

	@Override
	public MessageCreateRequest populateFrom(LanguageChangeMessageContent messageContent) {
		DiscordEmbedSpec embedSpec = localeSpec.getEmbedSpecs().get(messageContent.getLanguage());

		EmbedData embed = EmbedData.builder()
				.color(localeSpec.getColor())
				.title(formatTitle(messageContent, embedSpec))
				.build();
		
		return MessageCreateRequest.builder()
				.embed(embed)
				.build();
	}
	
	private String formatTitle(LanguageChangeMessageContent messageContent, DiscordEmbedSpec embedSpec) {
		return String.format(embedSpec.getTitle(), messageContent.getLanguage().getName());
	}

}

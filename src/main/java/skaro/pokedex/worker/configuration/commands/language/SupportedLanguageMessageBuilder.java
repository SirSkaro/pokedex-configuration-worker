package skaro.pokedex.worker.configuration.commands.language;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedThumbnailData;
import discord4j.discordjson.json.MessageCreateRequest;
import skaro.pokedex.sdk.client.Language;
import skaro.pokedex.sdk.discord.MessageBuilder;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedLocaleSpec;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedSpec;

public class SupportedLanguageMessageBuilder implements MessageBuilder<SupportedLanguageMessageContent> {
	private DiscordEmbedLocaleSpec localeSpec;
	
	public SupportedLanguageMessageBuilder(DiscordEmbedLocaleSpec localeSpec) {
		this.localeSpec = localeSpec;
	}

	@Override
	public MessageCreateRequest populateFrom(SupportedLanguageMessageContent messageContent) {
		DiscordEmbedSpec embedSpec = localeSpec.getEmbedSpecs().get(messageContent.getLanguage());

		EmbedData embed = EmbedData.builder()
				.color(localeSpec.getColor())
				.title(embedSpec.getTitle())
				.description(formatDescription(messageContent, embedSpec))
				.thumbnail(EmbedThumbnailData.builder()
						.url(localeSpec.getThumbnail().toString())
						.build())
				.build();
		
		return MessageCreateRequest.builder()
				.embed(embed)
				.build();
	}
	
	private String formatDescription(SupportedLanguageMessageContent messageContent, DiscordEmbedSpec embedSpec) {
		String supportedLanguageList = Stream.of(Language.values())
				.map(language -> String.format("%s%s (%s)", ":small_blue_diamond:" , language.getName(), language.getAbbreviation()))
				.collect(Collectors.joining("\n"));
		
		return String.format(embedSpec.getDescription(), supportedLanguageList);
	}

}

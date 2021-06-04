package skaro.pokedex.worker.configuration.commands.prefix;

import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedThumbnailData;
import discord4j.discordjson.json.MessageCreateRequest;
import skaro.pokedex.sdk.discord.MessageBuilder;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedLocaleSpec;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedSpec;

public class PrefixCharacterLimitMessageBuilder implements MessageBuilder<PrefixCharacterLimitMessageContent> {

	private DiscordEmbedLocaleSpec localeSpec;
	
	public PrefixCharacterLimitMessageBuilder(DiscordEmbedLocaleSpec localeSpec) {
		this.localeSpec = localeSpec;
	}


	@Override
	public MessageCreateRequest populateFrom(PrefixCharacterLimitMessageContent messageContent) {
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
	
	private String formatDescription(PrefixCharacterLimitMessageContent messageContent, DiscordEmbedSpec embedSpec) {
		return String.format(embedSpec.getDescription(), messageContent.getMaxLength());
	}

}

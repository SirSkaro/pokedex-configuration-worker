package skaro.pokedex.worker.configuration.commands.language;

import skaro.pokedex.sdk.client.Language;
import skaro.pokedex.sdk.discord.MessageContent;

public class SupportedLanguageMessageContent implements MessageContent {
	private Language language;
	
	public void setLanguage(Language language) {
		this.language = language;
	}
	@Override
	public Language getLanguage() {
		return language;
	}

}

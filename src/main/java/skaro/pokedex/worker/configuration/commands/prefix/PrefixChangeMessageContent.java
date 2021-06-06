package skaro.pokedex.worker.configuration.commands.prefix;

import skaro.pokedex.sdk.client.Language;
import skaro.pokedex.sdk.discord.MessageContent;

public class PrefixChangeMessageContent implements MessageContent {

	private Language language;
	private String newPrefix;
	
	public String getNewPrefix() {
		return newPrefix;
	}
	public void setNewPrefix(String newPrefix) {
		this.newPrefix = newPrefix;
	}
	public void setLanguage(Language language) {
		this.language = language;
	}
	@Override
	public Language getLanguage() {
		return language;
	}

}

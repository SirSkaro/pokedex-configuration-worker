package skaro.pokedex.worker.configuration.commands.prefix;

import skaro.pokedex.sdk.client.Language;
import skaro.pokedex.sdk.discord.MessageContent;

public class PrefixCharacterLimitMessageContent implements MessageContent {

	private int maxLength;
	private Language language;
	
	public int getMaxLength() {
		return maxLength;
	}
	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}
	@Override
	public Language getLanguage() {
		return language;
	}
	public void setLanguage(Language language) {
		this.language = language;
	}
	

}

package skaro.pokedex.worker.configuration.commands.language;

import static skaro.pokedex.sdk.worker.command.specification.CommonLocaleSpecConfiguration.BASE_WARNING_LOCALE_SPEC_BEAN;
import static skaro.pokedex.sdk.worker.command.specification.CommonLocaleSpecConfiguration.EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_BEAN;

import java.util.HashMap;
import java.util.stream.Stream;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import skaro.pokedex.sdk.client.Language;
import skaro.pokedex.sdk.discord.DiscordMessageDirector;
import skaro.pokedex.sdk.discord.DiscordRouterFacade;
import skaro.pokedex.sdk.discord.MessageCreateRequestDirector;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedLocaleSpec;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedSpec;
import skaro.pokedex.sdk.worker.command.validation.common.ExpectedArgumentsFilter;
import skaro.pokedex.sdk.worker.command.validation.common.ExpectedArgumentsMessageBuilder;
import skaro.pokedex.sdk.worker.command.validation.common.ExpectedArgumentsMessageContent;

@Configuration
@PropertySource("classpath:language-command.properties")
public class LanguageCommandConfiguration {
	private static final String LANGUAGE_CHANGE_LOCALE_SPEC_PROPERTIES_PREFIX = "skaro.pokedex.worker.discord.embed-locale.language";
	private static final String LANGUAGE_CHANGE_LOCALE_SPEC_BEAN = "languageChangeLocaleSpec";
	
	public static final String LANGUAGE_EXPECTED_ARGUMENT_FILTER_BEAN = "languageExpectedArgumentFilter";
	private static final String LANGUAGE_EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_PROPERTIES_PREFIX = "skaro.pokedex.worker.discord.embed-locale.filter.language.expected-arguments";
	private static final String BASE_LANGUAGE_EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_BEAN = "baseLanguageExpectedArgumentsFilterLocaleSpec";
	private static final String LANGUAGE_EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_BEAN = "languageExpectedArgumentsFilterLocaleSpec";
	
	private static final String SUPPORTED_LANGUAGE_FILTER_LOCALE_SPEC_PROPERTIES_PREFIX = "skaro.pokedex.worker.discord.embed-locale.filter.language.supported-language";
	private static final String BASE_SUPPORTED_LANGUAGE_FILTER_LOCALE_SPEC_BEAN = "baseSupportedLanguageFilterLocaleSpec";
	private static final String SUPPORTED_LANGUAGE_FILTER_LOCALE_SPEC_BEAN = "supportedLanguageFilterLocaleSpec";

	
	@Bean(LANGUAGE_CHANGE_LOCALE_SPEC_BEAN)
	@Valid
	@ConfigurationProperties(LANGUAGE_CHANGE_LOCALE_SPEC_PROPERTIES_PREFIX)
	public DiscordEmbedLocaleSpec languageChangeLocaleSpec() {
		return new DiscordEmbedLocaleSpec();
	}
	
	@Bean
	public DiscordMessageDirector<LanguageChangeMessageContent> languageChangeMessageDirector(
			DiscordRouterFacade router, 
			@Qualifier(LANGUAGE_CHANGE_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec localeSpec) {
		LanguageChangeMessageBuilder messageBuilder = new LanguageChangeMessageBuilder(localeSpec);
		return new MessageCreateRequestDirector<LanguageChangeMessageContent>(router, messageBuilder);
	}
	
	@Bean(BASE_LANGUAGE_EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_BEAN)
	@Valid
	@ConfigurationProperties(LANGUAGE_EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_PROPERTIES_PREFIX)
	public DiscordEmbedLocaleSpec expectedArgumentsLocaleSpec() {
		return new DiscordEmbedLocaleSpec();
	}
	
	@Bean(LANGUAGE_EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_BEAN)
	@Valid
	public DiscordEmbedLocaleSpec expectedArgumentsLocaleSpec(
			@Qualifier(EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec expectedArgumentLocaleSpec,
			@Qualifier(BASE_LANGUAGE_EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec languageArgumentLocaleSpec) {
		DiscordEmbedLocaleSpec result = new DiscordEmbedLocaleSpec();
		result.setColor(expectedArgumentLocaleSpec.getColor());
		result.setThumbnail(expectedArgumentLocaleSpec.getThumbnail());
		result.setEmbedSpecs(new HashMap<>());
		Stream.of(Language.values()).forEach(language -> {
			DiscordEmbedSpec spec = new DiscordEmbedSpec();
			spec.setTitle(expectedArgumentLocaleSpec.getEmbedSpecs().get(language).getTitle());
			spec.setDescription(expectedArgumentLocaleSpec.getEmbedSpecs().get(language).getDescription());
			spec.setFields(languageArgumentLocaleSpec.getEmbedSpecs().get(language).getFields());
			result.getEmbedSpecs().put(language, spec);
		});
		
		return result;
	}
	
	@Bean(LANGUAGE_EXPECTED_ARGUMENT_FILTER_BEAN)
	public ExpectedArgumentsFilter exactArgumentCountFilter(
			DiscordRouterFacade router, 
			@Qualifier(LANGUAGE_EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec localeSpec) {
		ExpectedArgumentsMessageBuilder messageBuilder = new ExpectedArgumentsMessageBuilder(localeSpec);
		MessageCreateRequestDirector<ExpectedArgumentsMessageContent> director = new MessageCreateRequestDirector<ExpectedArgumentsMessageContent>(router, messageBuilder);
		return new ExpectedArgumentsFilter(1, director);
	}
	
	@Bean(BASE_SUPPORTED_LANGUAGE_FILTER_LOCALE_SPEC_BEAN)
	@Valid
	@ConfigurationProperties(SUPPORTED_LANGUAGE_FILTER_LOCALE_SPEC_PROPERTIES_PREFIX)
	public DiscordEmbedLocaleSpec baseSupportedLanguageLocaleSpec() {
		return new DiscordEmbedLocaleSpec();
	}
	
	@Bean(SUPPORTED_LANGUAGE_FILTER_LOCALE_SPEC_BEAN)
	@Valid
	public DiscordEmbedLocaleSpec supportedLanguageLocaleSpec(
			@Qualifier(BASE_WARNING_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec baseWarningLocaleSpec,
			@Qualifier(BASE_SUPPORTED_LANGUAGE_FILTER_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec supportedLanguageLocaleSpec) {
		DiscordEmbedLocaleSpec result = new DiscordEmbedLocaleSpec();
		result.setColor(baseWarningLocaleSpec.getColor());
		result.setThumbnail(baseWarningLocaleSpec.getThumbnail());
		result.setEmbedSpecs(new HashMap<>());
		Stream.of(Language.values()).forEach(language -> {
			DiscordEmbedSpec spec = new DiscordEmbedSpec();
			spec.setTitle(baseWarningLocaleSpec.getEmbedSpecs().get(language).getTitle());
			spec.setDescription(supportedLanguageLocaleSpec.getEmbedSpecs().get(language).getDescription());
			result.getEmbedSpecs().put(language, spec);
		});
		
		return result;
	}
	
	@Bean
	public DiscordMessageDirector<SupportedLanguageMessageContent> supportedLanguageMessageDirector(
			DiscordRouterFacade router, 
			@Qualifier(SUPPORTED_LANGUAGE_FILTER_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec localeSpec) {
		SupportedLanguageMessageBuilder messageBuilder = new SupportedLanguageMessageBuilder(localeSpec);
		return new MessageCreateRequestDirector<SupportedLanguageMessageContent>(router, messageBuilder);
	}
	
}

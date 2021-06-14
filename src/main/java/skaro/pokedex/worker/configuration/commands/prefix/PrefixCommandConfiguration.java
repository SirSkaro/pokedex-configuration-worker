package skaro.pokedex.worker.configuration.commands.prefix;


import static skaro.pokedex.sdk.worker.command.specification.CommonLocaleSpecConfiguration.BASE_WARNING_LOCALE_SPEC_BEAN;
import static skaro.pokedex.sdk.worker.command.specification.CommonLocaleSpecConfiguration.DISOCRD_PERMISSION_LOCALE_SPEC_BEAN;
import static skaro.pokedex.sdk.worker.command.specification.CommonLocaleSpecConfiguration.EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_BEAN;

import java.util.HashMap;
import java.util.stream.Stream;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import skaro.pokedex.sdk.client.Language;
import skaro.pokedex.sdk.discord.DiscordMessageDirector;
import skaro.pokedex.sdk.discord.DiscordRouterFacade;
import skaro.pokedex.sdk.discord.MessageCreateRequestDirector;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedLocaleSpec;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedSpec;
import skaro.pokedex.sdk.worker.command.validation.common.DiscordPermissionsFilter;
import skaro.pokedex.sdk.worker.command.validation.common.DiscordPermissionsMessageBuilder;
import skaro.pokedex.sdk.worker.command.validation.common.DiscordPermissionsMessageContent;
import skaro.pokedex.sdk.worker.command.validation.common.ExpectedArgumentsFilter;
import skaro.pokedex.sdk.worker.command.validation.common.ExpectedArgumentsMessageBuilder;
import skaro.pokedex.sdk.worker.command.validation.common.ExpectedArgumentsMessageContent;

@Configuration
@PropertySource("classpath:prefix-command.properties")
public class PrefixCommandConfiguration {
	public static final String PREFIX_EXPECTED_ARGUMENT_FILTER_BEAN = "prefixArgumentValidator";
	public static final String ADMIN_ROLE_FILTER = "adminRoleValidator";
	public static final String COMMAND_LOCALE_SPEC_BEAN = "prefixLocaleSpec";
	private static final String COMMAND_LOCALE_SPEC_PROPERTIES_PREFIX = "skaro.pokedex.worker.discord.embed-locale.prefix";
	private static final String PREFIX_EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_PROPERTIES_PREFIX = "skaro.pokedex.worker.discord.embed-locale.filter.prefix.expected-arguments";
	private static final String PREFIX_EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_BEAN = "prefixExpectedArgumentsFilterLocaleSpec";
	private static final String BASE_PREFIX_EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_BEAN = "basePrefixExpectedArgumentsFilterLocaleSpec";
	private static final String EXPECTED_ARGUMENTS_MESSAGE_DIRECTOR_BEAN = "prefixExpectedArgumentsMessageDirector";
	private static final String CHARACTER_LIMIT_FILTER_LOCALE_SPEC_BEAN = "characterLimitFilterLocaleSpec";
	private static final String BASE_CHARACTER_LIMIT_FILTER_LOCALE_SPEC_BEAN = "baseCharacterLimitFilterLocaleSpec";
	private static final String CHARACTER_LIMIT_FILTER_PROPERTIES_PREFIX = "skaro.pokedex.sdk.discord.embed-locale.filter.prefix.character-limit";
	
	@Bean(BASE_PREFIX_EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_BEAN)
	@Valid
	@ConfigurationProperties(PREFIX_EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_PROPERTIES_PREFIX)
	public DiscordEmbedLocaleSpec expectedArgumentsLocaleSpec() {
		return new DiscordEmbedLocaleSpec();
	}
	
	@Bean(PREFIX_EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_BEAN)
	@Valid
	public DiscordEmbedLocaleSpec expectedArgumentsLocaleSpec(
			@Qualifier(EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec expectedArgumentLocaleSpec,
			@Qualifier(BASE_PREFIX_EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec prefixArgumentLocaleSpec) {
		DiscordEmbedLocaleSpec result = new DiscordEmbedLocaleSpec();
		result.setColor(expectedArgumentLocaleSpec.getColor());
		result.setThumbnail(expectedArgumentLocaleSpec.getThumbnail());
		result.setEmbedSpecs(new HashMap<>());
		Stream.of(Language.values()).forEach(language -> {
			DiscordEmbedSpec spec = new DiscordEmbedSpec();
			spec.setTitle(expectedArgumentLocaleSpec.getEmbedSpecs().get(language).getTitle());
			spec.setDescription(expectedArgumentLocaleSpec.getEmbedSpecs().get(language).getDescription());
			spec.setFields(prefixArgumentLocaleSpec.getEmbedSpecs().get(language).getFields());
			result.getEmbedSpecs().put(language, spec);
		});
		
		return result;
	}
	
	@Bean(EXPECTED_ARGUMENTS_MESSAGE_DIRECTOR_BEAN)
	public DiscordMessageDirector<ExpectedArgumentsMessageContent> expectedArgumentsDirector(DiscordRouterFacade router, @Qualifier(PREFIX_EXPECTED_ARGUMENTS_FILTER_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec localeSpec) {
		ExpectedArgumentsMessageBuilder messageBuilder = new ExpectedArgumentsMessageBuilder(localeSpec);
		return new MessageCreateRequestDirector<ExpectedArgumentsMessageContent>(router, messageBuilder);
	}
	
	@Bean(PREFIX_EXPECTED_ARGUMENT_FILTER_BEAN)
	public ExpectedArgumentsFilter exactArgumentCountFilter(@Qualifier(EXPECTED_ARGUMENTS_MESSAGE_DIRECTOR_BEAN) DiscordMessageDirector<ExpectedArgumentsMessageContent> director) {
		return new ExpectedArgumentsFilter(1, director);
	}
	
	@Bean(ADMIN_ROLE_FILTER)
	public DiscordPermissionsFilter discordPermissionFilter(DiscordRouterFacade router, @Qualifier(DISOCRD_PERMISSION_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec localeSpec) {
		PermissionSet requiredPermissions = PermissionSet.of(Permission.MANAGE_ROLES);
		DiscordPermissionsMessageBuilder messageBuilder = new DiscordPermissionsMessageBuilder(localeSpec);
		MessageCreateRequestDirector<DiscordPermissionsMessageContent> director = new MessageCreateRequestDirector<DiscordPermissionsMessageContent>(router, messageBuilder);
		return new DiscordPermissionsFilter(requiredPermissions, router, director);
	}
	
	@Bean(BASE_CHARACTER_LIMIT_FILTER_LOCALE_SPEC_BEAN)
	@Valid
	@ConfigurationProperties(CHARACTER_LIMIT_FILTER_PROPERTIES_PREFIX)
	public DiscordEmbedLocaleSpec characterLimitFilterLocaleSpec() {
		return new DiscordEmbedLocaleSpec();
	}
	
	@Bean(CHARACTER_LIMIT_FILTER_LOCALE_SPEC_BEAN)
	@Valid
	public DiscordEmbedLocaleSpec characterLimitFilterLocaleSpec(
			@Qualifier(BASE_WARNING_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec baseWarningLocaleSpec,
			@Qualifier(BASE_CHARACTER_LIMIT_FILTER_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec baseCharacterLimitFilterLocaleSpec) {
		DiscordEmbedLocaleSpec result = new DiscordEmbedLocaleSpec();
		result.setColor(baseWarningLocaleSpec.getColor());
		result.setThumbnail(baseWarningLocaleSpec.getThumbnail());
		result.setEmbedSpecs(new HashMap<>());
		Stream.of(Language.values()).forEach(language -> {
			DiscordEmbedSpec spec = new DiscordEmbedSpec();
			spec.setTitle(baseWarningLocaleSpec.getEmbedSpecs().get(language).getTitle());
			spec.setDescription(baseCharacterLimitFilterLocaleSpec.getEmbedSpecs().get(language).getDescription());
			result.getEmbedSpecs().put(language, spec);
		});
		return result;
	}
	
	@Bean
	public DiscordMessageDirector<PrefixCharacterLimitMessageContent> characterLimitMessageDirector(DiscordRouterFacade router, @Qualifier(CHARACTER_LIMIT_FILTER_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec localeSpec) {
		PrefixCharacterLimitMessageBuilder messageBuilder = new PrefixCharacterLimitMessageBuilder(localeSpec);
		return new MessageCreateRequestDirector<PrefixCharacterLimitMessageContent>(router, messageBuilder);
	}
	
	@Bean(COMMAND_LOCALE_SPEC_BEAN)
	@Valid
	@ConfigurationProperties(COMMAND_LOCALE_SPEC_PROPERTIES_PREFIX)
	public DiscordEmbedLocaleSpec prefixLocaleSpec() {
		return new DiscordEmbedLocaleSpec();
	}
	
	@Bean
	public DiscordMessageDirector<PrefixChangeMessageContent> prefixMessageDirector(DiscordRouterFacade router, @Qualifier(COMMAND_LOCALE_SPEC_BEAN) DiscordEmbedLocaleSpec localeSpec) {
		PrefixChangeMessageBuilder messageBuilder = new PrefixChangeMessageBuilder(localeSpec);
		return new MessageCreateRequestDirector<PrefixChangeMessageContent>(router, messageBuilder);
	}
	
}

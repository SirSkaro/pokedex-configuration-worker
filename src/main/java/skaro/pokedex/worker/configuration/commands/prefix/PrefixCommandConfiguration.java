package skaro.pokedex.worker.configuration.commands.prefix;

import javax.validation.Valid;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import discord4j.rest.request.Router;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import skaro.pokedex.sdk.worker.command.specification.DiscordEmbedLocaleSpec;
import skaro.pokedex.sdk.worker.command.validation.common.DiscordPermissionFilter;
import skaro.pokedex.sdk.worker.command.validation.common.SingleArgumentFilter;

@Configuration
public class PrefixCommandConfiguration {
	public static final String SINGLE_ARGUMENT_VALIDATOR = "singleArgumentValidator";
	public static final String ADMIN_ROLE_VALIDATOR = "adminRoleValidator";
	public static final String PREFIX_LOCALE_SPEC = "prefixLocaleSpec";
	private static final String LOCALE_SPEC_PROPERTIES_PREFIX = "skaro.pokedex.worker.discord.embed-locale.prefix";
	
	@Bean(SINGLE_ARGUMENT_VALIDATOR)
	public SingleArgumentFilter exactArgumentCountFilter() {
		return new SingleArgumentFilter("The prefix command requires exactly 1 argument.");
	}
	
	@Bean(ADMIN_ROLE_VALIDATOR)
	public DiscordPermissionFilter discordPermissionFilter(Router router) {
		PermissionSet requiredPermissions = PermissionSet.of(Permission.MANAGE_ROLES);
		return new DiscordPermissionFilter(requiredPermissions, router);
	}
	
	@Bean(PREFIX_LOCALE_SPEC)
	@Valid
	@ConfigurationProperties(LOCALE_SPEC_PROPERTIES_PREFIX)
	public DiscordEmbedLocaleSpec prefixLocaleSpec() {
		return new DiscordEmbedLocaleSpec();
	}
	
}

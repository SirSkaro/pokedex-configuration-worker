package skaro.pokedex.worker.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import skaro.pokedex.sdk.client.guild.GuildServiceClientConfiguration;
import skaro.pokedex.sdk.worker.WorkerDiscordConfiguration;
import skaro.pokedex.sdk.worker.WorkerMessageListenConfiguration;
import skaro.pokedex.sdk.worker.WorkerResourceConfiguration;
import skaro.pokedex.sdk.worker.command.DefaultWorkerCommandConfiguration;
import skaro.pokedex.sdk.worker.command.ratelimit.local.LocalRateLimitConfiguration;

@Configuration
@Import({
	WorkerResourceConfiguration.class,
	WorkerDiscordConfiguration.class, 
	WorkerMessageListenConfiguration.class,
	DefaultWorkerCommandConfiguration.class,
	GuildServiceClientConfiguration.class,
	LocalRateLimitConfiguration.class
})
public class ConfigurationWorkerConfiguration {

}

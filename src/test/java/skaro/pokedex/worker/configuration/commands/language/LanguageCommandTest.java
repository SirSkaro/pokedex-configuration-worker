package skaro.pokedex.worker.configuration.commands.language;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import discord4j.rest.http.client.ClientResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import skaro.pokedex.sdk.client.Language;
import skaro.pokedex.sdk.client.guild.GuildServiceClient;
import skaro.pokedex.sdk.client.guild.GuildSettings;
import skaro.pokedex.sdk.discord.DiscordMessageDirector;
import skaro.pokedex.sdk.messaging.dispatch.AnsweredWorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;

@ExtendWith(SpringExtension.class)
public class LanguageCommandTest {

	@Mock
	private DiscordMessageDirector<LanguageChangeMessageContent> director;
	@Mock
	private GuildServiceClient client;
	
	private LanguageCommand command;
	
	@BeforeEach
	public void setup() {
		command = new LanguageCommand(director, client);
	}
	
	@Test
	public void testExecute_guildHasExistingSettings() {
		Language newLanguage = Language.FRENCH;
		String guildId = UUID.randomUUID().toString();
		String channelId = UUID.randomUUID().toString();
		WorkRequest request = new WorkRequest();
		request.setArguments(List.of(newLanguage.getAbbreviation()));
		request.setGuildId(guildId);
		request.setChannelId(channelId);
		GuildSettings existingSettings = new GuildSettings();
		existingSettings.setLanguage(Language.JAPANESE);
		existingSettings.setPrefix("$$");
		
		Mockito.when(client.getSettings(guildId))
			.thenReturn(Mono.just(existingSettings));
		Mockito.when(client.saveSettings(eq(guildId), any()))
			.thenReturn(Mono.just(existingSettings));
		Mockito.when(director.createDiscordMessage(any(), eq(channelId)))
			.thenReturn(Mono.just(Mockito.mock(ClientResponse.class)));
		
		Consumer<AnsweredWorkRequest> assertAnswerCorrect = answer -> {
			assertEquals(WorkStatus.SUCCESS, answer.getStatus());
			assertEquals(request, answer.getWorkRequest());
		};
		
		StepVerifier.create(command.execute(request))
			.assertNext(assertAnswerCorrect)
			.expectComplete()
			.verify();
		
		ArgumentCaptor<GuildSettings> savedSettingsCaptor = ArgumentCaptor.forClass(GuildSettings.class);
		Mockito.verify(client, times(1)).saveSettings(eq(guildId), savedSettingsCaptor.capture());
		GuildSettings savedSettings = savedSettingsCaptor.getValue();
		assertEquals(newLanguage, savedSettings.getLanguage());
		assertEquals(existingSettings.getPrefix(), savedSettings.getPrefix());
	}
	
	@Test
	public void testExecute_guildDoesNotHaveExistingSettings() {
		Language newLanguage = Language.SPANISH;
		String guildId = UUID.randomUUID().toString();
		String channelId = UUID.randomUUID().toString();
		WorkRequest request = new WorkRequest();
		request.setArguments(List.of(newLanguage.getName()));
		request.setGuildId(guildId);
		request.setChannelId(channelId);
		request.setLanguage(Language.KOREAN);
		
		Mockito.when(client.getSettings(guildId))
			.thenReturn(Mono.empty());
		Mockito.when(client.saveSettings(eq(guildId), any()))
			.thenAnswer(invocation -> Mono.just(invocation.getArgument(1)));
		Mockito.when(director.createDiscordMessage(any(), eq(channelId)))
			.thenReturn(Mono.just(Mockito.mock(ClientResponse.class)));
		
		Consumer<AnsweredWorkRequest> assertAnswerCorrect = answer -> {
			assertEquals(WorkStatus.SUCCESS, answer.getStatus());
			assertEquals(request, answer.getWorkRequest());
		};
		
		StepVerifier.create(command.execute(request))
			.assertNext(assertAnswerCorrect)
			.expectComplete()
			.verify();
		
		ArgumentCaptor<GuildSettings> savedSettingsCaptor = ArgumentCaptor.forClass(GuildSettings.class);
		Mockito.verify(client, times(1)).saveSettings(eq(guildId), savedSettingsCaptor.capture());
		GuildSettings savedSettings = savedSettingsCaptor.getValue();
		assertEquals(newLanguage, savedSettings.getLanguage());
		assertNull(savedSettings.getPrefix());
	}
	
	@Test
	public void testExecute_errorShouldPropogate() {
		Language newLanguage = Language.ITALIAN;
		String guildId = UUID.randomUUID().toString();
		WorkRequest request = new WorkRequest();
		request.setArguments(List.of(newLanguage.getName()));
		request.setGuildId(guildId);
		IOException guildServiceException = new IOException();
		
		Mockito.when(client.getSettings(eq(guildId)))
			.thenReturn(Mono.error(guildServiceException));
		
		StepVerifier.create(command.execute(request))
			.expectError(guildServiceException.getClass())
			.verify();
	}
	
	
}

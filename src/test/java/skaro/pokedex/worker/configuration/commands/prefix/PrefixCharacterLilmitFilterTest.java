package skaro.pokedex.worker.configuration.commands.prefix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import discord4j.rest.http.client.ClientResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import skaro.pokedex.sdk.discord.DiscordMessageDirector;
import skaro.pokedex.sdk.messaging.dispatch.WorkRequest;
import skaro.pokedex.sdk.messaging.dispatch.WorkStatus;

@ExtendWith(SpringExtension.class)
public class PrefixCharacterLilmitFilterTest {

	@Mock
	private DiscordMessageDirector<PrefixCharacterLimitMessageContent> messageDirector;
	private PrefixCharacterLimitFilter filter;
	
	@BeforeEach
	public void setup() {
		filter = new PrefixCharacterLimitFilter(messageDirector);
	}
	
	@Test
	public void testFitler_FilterPass() {
		String newPrefix = "#";
		WorkRequest prefixChangeRequest = new WorkRequest();
		prefixChangeRequest.setArguments(List.of(newPrefix));
		
		StepVerifier.create(filter.filter(prefixChangeRequest))
			.expectComplete()
			.verify();
	}
	
	@Test
	public void testFitler_FilterFails() {
		String newPrefix = "!@#$^*&";
		String channelId = UUID.randomUUID().toString();
		WorkRequest prefixChangeRequest = new WorkRequest();
		prefixChangeRequest.setChannelId(channelId);
		prefixChangeRequest.setArguments(List.of(newPrefix));
		
		Mockito.when(messageDirector.createDiscordMessage(any(), ArgumentMatchers.eq(channelId)))
			.thenReturn(Mono.just(Mockito.mock(ClientResponse.class)));
		
		StepVerifier.create(filter.filter(prefixChangeRequest))
			.assertNext(answer -> assertEquals(WorkStatus.BAD_REQUEST, answer.getStatus()))
			.expectComplete()
			.verify();
	}
	
	
}

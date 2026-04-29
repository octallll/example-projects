package backend.academy.linktracker.scrapper.config;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.client.GithubClient;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.properties.GithubProperties;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ClientConfiguration {

    @Bean
    public BotClient botClient(@Value("${app.bot-url}") String botUrl) {
        RestClient restClient = RestClient.builder().baseUrl(botUrl).build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(BotClient.class);
    }

    @Bean
    public GithubClient githubClient(GithubProperties githubProperties) {
        return client(githubProperties.getUrl(), githubProperties.getToken(), GithubClient.class);
    }

    @Bean
    public StackOverflowClient stackOverflowClient(StackoverflowProperties stackoverflowProperties) {
        return client(stackoverflowProperties.getUrl(), null, StackOverflowClient.class);
    }

    private <T> T client(String baseUrl, String token, Class<T> clientClass) {
        RestClient.Builder builder =
                RestClient.builder().baseUrl(baseUrl).defaultHeader("User-Agent", "LinkTracker-App");

        if (token != null && !token.isBlank() && !token.startsWith("${")) {
            builder.defaultHeader("Authorization", "Bearer " + token);
        }

        RestClient restClient = builder.build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(clientClass);
    }
}

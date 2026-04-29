package backend.academy.linktracker.scrapper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import backend.academy.linktracker.scrapper.client.GithubClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@ExtendWith(MockitoExtension.class)
class GithubClientTest {

    private MockRestServiceServer server;
    private GithubClient githubClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.github.com");

        this.server = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder.build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build();
        this.githubClient = factory.createClient(GithubClient.class);
    }

    @Test
    void testApiErrorHandling() {
        server.expect(requestTo("https://api.github.com/repos/owner/repo"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(RuntimeException.class, () -> {
            githubClient.fetchRepository("owner", "repo");
        });
    }
}

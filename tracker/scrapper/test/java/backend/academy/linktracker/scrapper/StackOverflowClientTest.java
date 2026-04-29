package backend.academy.linktracker.scrapper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

class StackOverflowClientTest {

    private MockRestServiceServer server;
    private StackOverflowClient stackOverflowClient;

    @BeforeEach
    void setUp() {
        // Базовый URL API StackOverflow
        String baseUrl = "https://api.stackexchange.com/2.3";
        RestClient.Builder builder = RestClient.builder().baseUrl(baseUrl);

        this.server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build();
        this.stackOverflowClient = factory.createClient(StackOverflowClient.class);
    }

    @Test
    void testApiErrorHandling() {
        server.expect(requestTo("https://api.stackexchange.com/2.3/questions/12345?site=stackoverflow"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(AssertionError.class, () -> {
            stackOverflowClient.fetchQuestion(12345L);
        });
    }
}

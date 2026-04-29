package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.model.StackOverflowAnswersResponse;
import backend.academy.linktracker.scrapper.model.StackOverflowResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/questions/{id}")
public interface StackOverflowClient {

    @GetExchange("?site=stackoverflow")
    StackOverflowResponse fetchQuestion(@PathVariable("id") Long id);

    @GetExchange("/questions/{id}/answers?site=stackoverflow&sort=activity&order=desc")
    StackOverflowAnswersResponse fetchAnswers(@PathVariable Long id);
}

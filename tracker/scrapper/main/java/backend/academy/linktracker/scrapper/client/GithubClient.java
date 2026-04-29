package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.model.GithubIssueResponse;
import backend.academy.linktracker.scrapper.model.GithubResponse;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface GithubClient {
    @GetExchange("/repos/{owner}/{repo}")
    GithubResponse fetchRepository(@PathVariable("owner") String owner, @PathVariable("repo") String repo);

    @GetExchange("/repos/{owner}/{repo}/issues?state=all&sort=created&per_page=1")
    List<GithubIssueResponse> fetchLastIssues(@PathVariable String owner, @PathVariable String repo);
}

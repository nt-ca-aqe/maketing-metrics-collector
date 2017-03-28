package info.novatec.metricscollector.github;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@ConfigurationProperties(prefix = "github.project")
public class GithubServiceScheduler {

    @Autowired
    private GithubCollector githubCollector;

    @Autowired
    private GithubRepository githubRepository;

    private List<String> urls = new ArrayList<>();

    /**
     * Accesses the urls from application.yml
     *
     * @return List of Strings with the respective urls
     */
    public List<String> getUrls() {
        return this.urls;
    }

    //TODO replace with cron expression, cron = "0 0 0 * * *" - Every day once
    @Scheduled(fixedRate = 15000)
    private void updateAllGithubProjectsMetrics() {
        if (!getUrls().isEmpty()) {
            getUrls().forEach(githubProjectUrl -> {
                githubRepository.saveMetrics(collectMetrics(githubProjectUrl));
                log.info("SCHEDULER " + githubProjectUrl + " completed.");
            });
        }
    }

    private GithubMetrics collectMetrics(String githubProjectUrl) {
        return githubCollector.collect(githubProjectUrl);
    }
}

package info.novatec.metricscollector.github;

import java.util.ArrayList;
import java.util.List;

import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import info.novatec.metricscollector.commons.InfluxService;


@Slf4j
@Component
public class GithubRepository {

    private InfluxService influx;

    @Autowired
    GithubRepository(InfluxService influx) {
        this.influx = influx;
    }

    void setRetention(String retention) {
        if(retention == null || retention == ""){
            return;
        }
        influx.setRetention(retention);
    }

    void saveMetrics(GithubMetricsResult metrics) {
        influx.connect();
        influx.savePoint(createPoints(metrics));
        influx.close();
        log.info("Added point  for '" + metrics.getRepositoryName() + "' to InfluxDb Measurement");
    }

    List<Point> createPoints(GithubMetricsResult metrics) {
        log.info("Adding measurement point for '" + metrics.getRepositoryName() + "'.");
        List<Point> points = new ArrayList<>();

        if(metrics.hasNullValues()){
            return points;
        }

        Point.Builder point = Point.measurement(metrics.getRepositoryName())
                .addField("contributors", metrics.getContributors())
                .addField("stars", metrics.getStars())
                .addField("forks", metrics.getForks())
                .addField("watchers", metrics.getWatchers())
                .addField("openIssues", metrics.getOpenIssues())
                .addField("closedIssues", metrics.getClosedIssues())
                .addField("commits", metrics.getCommits())
                .addField("yesterdaysTotalVisits", metrics.getDailyVisits().getTotalVisits())
                .addField("yesterdaysUniqueVisits", metrics.getDailyVisits().getUniqueVisits());
        metrics.getReleaseDownloads().entrySet().forEach(
            download -> point.addField(download.getKey(), download.getValue())
        );


        metrics.getReferringSitesLast14Days().entrySet().forEach( referrer -> {
            Point pointReferrer = Point
                .measurement(metrics.getRepositoryName()+"_Referrer")
                .tag("referringSite", referrer.getKey())
                .addField("clicksLast14Days", referrer.getValue().getTotalVisits())
                .addField("uniqueClicksLast14Days", referrer.getValue().getUniqueVisits())
                .build();
            points.add(pointReferrer);
        });

        points.add(point.build());

        return points;
    }

}

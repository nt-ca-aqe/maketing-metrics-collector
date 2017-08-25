package info.novatec.metricscollector.google.collector;

import static info.novatec.metricscollector.google.DimensionFilterOperators.*;
import static info.novatec.metricscollector.google.GoogleAnalyticsProperties.GA_PAGEPATH;

import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;

import info.novatec.metricscollector.commons.MetricCollector;
import info.novatec.metricscollector.google.GoogleAnalyticsProperties;
import info.novatec.metricscollector.google.Metrics;
import info.novatec.metricscollector.google.RequestBuilder;
import info.novatec.metricscollector.google.ResponseParser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;


@Component
@RequiredArgsConstructor
public class AqeHomePage implements MetricCollector {

    private static final String AQE_HOME_PAGE_PATH = "/dienstleistungen/agile-quality-engineering/";

    private final GoogleAnalyticsProperties properties;

    private final RequestBuilder requestBuilder;

    private final ResponseParser responseParser;

    @Getter
    private final List<Metrics> metrics;

    @Override
    public void collect() {
        GetReportsResponse getReportsResponse = requestMetrics();
        responseParser.parse(getReportsResponse, metrics);
    }

    private GetReportsResponse requestMetrics() {
        return requestBuilder.prepareRequest()
            .addDimensions(properties.getSharedDimensions())
            .addDimensions(properties.getAqeHomepage().getSpecificDimensions())
            .addMetrics(properties.getSharedMetrics())
            .addMetrics(properties.getAqeHomepage().getSpecificMetrics())
            .addDimensionFilters(GA_PAGEPATH, EXACT, Collections.singletonList(AQE_HOME_PAGE_PATH))
            .buildRequest(properties.getAqeHomepage().getViewId())
            .sendRequest();
    }
}

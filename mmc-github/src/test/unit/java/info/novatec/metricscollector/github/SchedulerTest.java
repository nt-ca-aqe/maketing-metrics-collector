package info.novatec.metricscollector.github;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import info.novatec.metricscollector.commons.exception.UserDeniedException;
import info.novatec.metricscollector.github.collector.GithubMetricCollector;
import info.novatec.metricscollector.github.collector.GithubMetricCollectorWithImplementations;
import info.novatec.metricscollector.github.collector.GithubMetricCollectorWithoutImplementations;
import info.novatec.metricscollector.github.collector.GithubMetricFakeCollector;
import info.novatec.metricscollector.github.util.DataProvider;


@RunWith(SpringRunner.class)
public class SchedulerTest {

    @MockBean
    private RestService restService;

    @MockBean
    private MetricsRepository repository;

    private Scheduler scheduler;

    @Before
    public void init(){
        List<String> urls = new ArrayList<>();
        urls.add(DataProvider.VALID_GITHUB_URL+1);
        urls.add(DataProvider.VALID_GITHUB_URL+2);
        scheduler = spy(new Scheduler(restService, repository));
        scheduler.setUrls(urls);
    }

    @Test
    public void checkIfCollectorIsInvokedWithUrlsTest() {
        mockApplicationContext(scheduler);
        scheduler.updateAllGithubProjectsMetrics(GithubMetricCollectorWithImplementations.class);

        //one invocation for every url
        verify(scheduler, times(2)).executeCollection(anyObject(), anyObject());
    }

    @Test
    public void tryFindingCollectorsWithInterfaceThatHasNoImplementations(){
        mockApplicationContext(scheduler);
        scheduler.updateAllGithubProjectsMetrics(GithubMetricCollectorWithoutImplementations.class);
        //since there is no collector implemented, execution is not invoked
        verify(scheduler, times(0)).executeCollection(anyObject(), anyObject());
    }

    @Test
    public void checkThatNextCollectionIsInvokedWithNonAuthorizedUserTest(){
        mockApplicationContext(scheduler);
        doThrow(UserDeniedException.class).when(scheduler).executeCollection(anyObject(), anyObject());
        scheduler.updateAllGithubProjectsMetrics(GithubMetricCollectorWithImplementations.class);
        verify(repository, times(0)).saveMetrics(anyObject());
        verify(scheduler, times(2)).executeCollection(anyObject(), anyObject());
    }

    @Test
    public void checkIfCollectionIsInvokedBySchedulerMethodTest(){
        doNothing().when(scheduler).updateAllGithubProjectsMetrics(GithubMetricCollector.class);
        scheduler.scheduleUpdateAllGithubProjectsMetrics();
        verify(scheduler).updateAllGithubProjectsMetrics(GithubMetricCollector.class);
    }

    @SuppressWarnings("unchecked")
    private void mockApplicationContext(Scheduler scheduler){
        ApplicationContext mockedContext = mock(ApplicationContext.class);
        scheduler.setApplicationContext(mockedContext);
        Map<String, GithubMetricFakeCollector> mockedBeanMap = mock(Map.class);
        GithubMetricFakeCollector mockedBean = mock(GithubMetricFakeCollector.class);
        List<GithubMetricFakeCollector> beanList = new ArrayList<>();
        beanList.add(mockedBean);
        doReturn(mockedBeanMap).when(mockedContext).getBeansOfType(GithubMetricCollectorWithImplementations.class);
        doReturn(beanList).when(mockedBeanMap).values();
    }

}
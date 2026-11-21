package org.jeasy.batch.core.listener;

import org.jeasy.batch.core.job.JobParameters;
import org.jeasy.batch.core.job.JobReport;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.inOrder;

public class CompositeJobListener_ArrowTest {

    @Test
    public void beforeJobDelegatesInListOrder() {
        JobListener first = mock(JobListener.class);
        JobListener second = mock(JobListener.class);
        JobParameters parameters = mock(JobParameters.class);
        CompositeJobListener composite =
                new CompositeJobListener(Arrays.asList(first, second));

        composite.beforeJob(parameters);

        inOrder(first, second).verify(first).beforeJob(parameters);
        inOrder(first, second).verify(second).beforeJob(parameters);
    }

    @Test
    public void afterJobDelegatesInReverseListOrder() {
        JobListener first = mock(JobListener.class);
        JobListener second = mock(JobListener.class);
        JobReport report = mock(JobReport.class);
        CompositeJobListener composite =
                new CompositeJobListener(Arrays.asList(first, second));

        composite.afterJob(report);

        inOrder(second, first).verify(second).afterJob(report);
        inOrder(second, first).verify(first).afterJob(report);
    }

    @Test
    public void addJobListenerAddsDelegateForBeforeJob() {
        JobListener listener = mock(JobListener.class);
        JobParameters parameters = mock(JobParameters.class);
        CompositeJobListener composite = new CompositeJobListener();

        composite.addJobListener(listener);
        composite.beforeJob(parameters);

        verify(listener).beforeJob(parameters);
    }

    @Test
    public void emptyListenerListDoesNotDelegate() {
        CompositeJobListener composite = new CompositeJobListener();

        composite.beforeJob(mock(JobParameters.class));
        composite.afterJob(mock(JobReport.class));
    }

    @Test
    public void afterJobWithEmptyProvidedListDoesNotDelegate() {
        CompositeJobListener composite =
                new CompositeJobListener(new ArrayList<JobListener>());

        composite.afterJob(mock(JobReport.class));
    }
}

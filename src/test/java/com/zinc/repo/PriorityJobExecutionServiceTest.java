package com.zinc.repo;

import com.zinc.classes.PriorityJobExecutionService;
import com.zinc.exceptions.ZincRuntimeException;
import com.zinc.utils.MockFactory;
import com.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * User: NachoSoto
 * Date: 9/25/13
 */
public class PriorityJobExecutionServiceTest extends ZincBaseTest {

    public static final int CONCURRENCY = 1;

    private static class Data {
        private final int mPriority;
        private final String mResult;

        public Data(final int priority, final String result) {
            mPriority = priority;
            mResult = result;
        }

        private int getPriority() {
            return mPriority;
        }

        private String getResult() {
            return mResult;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Data data = (Data) o;

            return (mPriority == data.mPriority) && !(mResult != null ? !mResult.equals(data.mResult) : data.mResult != null);

        }

        @Override
        public int hashCode() {
            return 31 * mPriority + (mResult != null ? mResult.hashCode() : 0);
        }
    }

    private static class DataComparator implements Comparator<Data> {

        @Override
        public int compare(final Data o1, final Data o2) {
            return (o1.getPriority() < o2.getPriority()) ? 1 : -1;
        }
    }

    private PriorityJobExecutionService<Data, String> service;

    @Mock PriorityJobExecutionService.DataProcessor<Data, String> mDataProcessor;

    @Before
    public void setUp() throws Exception {
        service = new PriorityJobExecutionService<Data, String>(
                CONCURRENCY,
                new MockFactory.DaemonThreadFactory(),
                new DataComparator(),
                mDataProcessor);
    }

    @Test
    public void canBeStarted() throws Exception {
        service.start();
    }

    @Test(expected = ZincRuntimeException.class)
    public void cannotBeStartedTwice() throws Exception {
        service.start();
        service.start();
    }

    @Test
    public void canBeStopped() throws Exception {
        service.start();
        assertTrue(service.stop());
    }

    @Test(expected = ZincRuntimeException.class)
    public void cannotBeStoppedIfNotStarted() throws Exception {
        service.stop();
    }

    @Test
    public void dataCanBeAdded() throws Exception {
        service.add(new Data(1, "result"));
    }

    @Test
    public void dataResultCanBeRetrieved() throws Exception {
        final Data data = processAndAddRandomData();

        // run
        service.start();
        final Future<String> result = service.get(data);

        verify(mDataProcessor).process(data);
        assertNotNull(result);
        assertEquals(data.getResult(), result.get());
    }

    @Test
    public void dataResultCanBeRetrievedIfOtherObjectsWereAddedBefore() throws Exception {
        final Data data = processAndAddRandomData();

        processAndAddRandomData();
        processAndAddRandomData();
        processAndAddRandomData();

        // run
        service.start();
        final Future<String> result = service.get(data);

        verify(mDataProcessor).process(data);
        assertNotNull(result);
        assertEquals(data.getResult(), result.get());
    }

    @Test(expected = PriorityJobExecutionService.JobNotFoundException.class)
    public void dataCannotBeRetrievedIfItWasNeverAdded() throws Exception {
        final Data data = randomData();

        service.start();
        service.get(data);
    }

    private Data randomData() {
        return new Data(MockFactory.randomInt(1, 10), MockFactory.randomString());
    }

    private Data processAndAddRandomData() {
        final Data data = randomData();

        processData(data);
        service.add(data);

        return data;
    }

    private void processData(final Data data) {
        final Callable<String> processor = MockFactory.callableWithResult(data.getResult());
        when(mDataProcessor.process(data)).thenReturn(processor);
    }
}

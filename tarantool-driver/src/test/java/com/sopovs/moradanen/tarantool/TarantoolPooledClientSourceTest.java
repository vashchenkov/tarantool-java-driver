package com.sopovs.moradanen.tarantool;

import com.sopovs.moradanen.tarantool.core.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

import static com.sopovs.moradanen.tarantool.TarantoolPooledClientSource.CONNECTION_CLOSED;
import static com.sopovs.moradanen.tarantool.TarantoolPooledClientSource.POOL_CLOSED;
import static org.junit.jupiter.api.Assertions.*;


class TarantoolPooledClientSourceTest {
    private static final TarantoolConfig DUMMY_CONFIG = new TarantoolConfig(null, 0, null, null);
    private static final int POOL_SIZE = 5;

    @Test
    void testSimple() {
        //noinspection EmptyTryBlock
        try (TarantoolClientSource ignored = createPool()) {
        }
    }

    private TarantoolPooledClientSource createPool() {
        return createPool(POOL_SIZE);
    }

    private TarantoolPooledClientSource createPool(int size) {
        return new TarantoolPooledClientSource(DUMMY_CONFIG, DummyTarantoolClient::new, size);
    }

    @Test
    void releaseWaiterOnPoolClose() throws Exception {
        ExecutorService threadPool = Executors.newFixedThreadPool(POOL_SIZE);
        List<Future<?>> futures = new ArrayList<>();
        try (TarantoolClientSource pool = createPool(0)) {
            for (int i = 0; i < POOL_SIZE; i++) {
                futures.add(threadPool.submit(() -> {
                    //noinspection EmptyTryBlock
                    try (TarantoolClient ignored = pool.getClient()) {
                    }
                }));
            }
        }
        assertEquals(POOL_SIZE, futures.size());
        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.SECONDS);
        for (Future<?> future : futures) {
            assertTrue(future.isDone());
            try {
                future.get();
                fail("Exception expected");
            } catch (ExecutionException e) {
                assertTrue(e.getCause() instanceof TarantoolException);
                assertEquals("Pool is closed", e.getCause().getMessage());
            }
        }
    }

    @Test
    void testGet1Connection() {
        //noinspection EmptyTryBlock
        try (TarantoolClientSource pool = createPool(); TarantoolClient ignored = pool.getClient()) {
        }
    }

    @Test
    void testGetAllConnections() throws Exception {
        try (TarantoolClientSource pool = createPool()) {
            testGetAllConnectionsInternal(pool);
        }
    }

    @Test
    void testGetAllConnectionsAfterException() throws Exception {
        try (TarantoolClientSource pool = createPool()) {
            try (TarantoolClient client = pool.getClient()) {
                client.ping();
                fail("Exception expected");
            } catch (Exception e) {
                // no code
            }
            testGetAllConnectionsInternal(pool);
        }
    }

    private void testGetAllConnectionsInternal(TarantoolClientSource pool) throws InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(POOL_SIZE);
        CountDownLatch latch = new CountDownLatch(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++) {
            threadPool.execute(() -> {
                try (TarantoolClient ignored = pool.getClient()) {
                    latch.countDown();
                    try {
                        latch.await();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.SECONDS);
        assertEquals(0, latch.getCount());
    }

    @Test
    void testPoolSize() {
        testReuseOneConnection(1);
    }

    @Test
    void testReuseConnectionWhenPossible() {
        testReuseOneConnection(2);
    }

    private void testReuseOneConnection(int poolSize) {
        Function<TarantoolConfig, TarantoolClient> clientFactory = new Function<TarantoolConfig, TarantoolClient>() {
            boolean used = false;

            @Override
            public TarantoolClient apply(TarantoolConfig t) {
                assertFalse(used);
                used = true;
                return new DummyTarantoolClient(t);
            }
        };

        try (TarantoolClientSource pool = new TarantoolPooledClientSource(DUMMY_CONFIG, clientFactory, poolSize)) {
            pool.getClient().close();
            pool.getClient().close();
        }
    }

    @Test
    void testSimpleConnectionReuse() {
        try (TarantoolClientSource pool = createPool(1)) {
            for (int i = 0; i < 10; i++) {
                pool.getClient().close();
            }
        }
    }

    @Test
    void testUseConnectionAfterClose() {
        try (TarantoolClientSource pool = createPool(1)) {
            TarantoolClient client = pool.getClient();
            client.close();

            TarantoolException exception = assertThrows(TarantoolException.class, client::ping);
            assertEquals(CONNECTION_CLOSED, exception.getMessage());
        }
    }

    @Test
    void testCloseWithWaiting() throws Exception {
        TarantoolClientSource pool = createPool(0);
        pool.close();
        ExecutorService threadPool = Executors.newFixedThreadPool(1);
        Future<TarantoolException> future = threadPool.submit(() -> {
            try {
                pool.getClient();
                return null;
            } catch (TarantoolException e) {
                return e;
            }
        });
        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.SECONDS);

        TarantoolException exception = assertThrows(TarantoolException.class,
                () -> {
                    throw future.get();
                }
        );
        assertEquals(POOL_CLOSED, exception.getMessage());
    }

    @Test
    void testThrowingClientCreation() {
        try (TarantoolClientSource pool = new TarantoolPooledClientSource(DUMMY_CONFIG, DummyCreationThrowingClient::new, 1)) {
            assertThrows(TarantoolException.class, pool::getClient);
            //should not hang on failing to get two connections from pool of size 1
            assertThrows(TarantoolException.class, pool::getClient);
        }
    }

    static class DummyTarantoolClient implements TarantoolClient {

        DummyTarantoolClient(@SuppressWarnings("unused") TarantoolConfig config) {
            // dummy
        }

        @Override
        public boolean isClosed() {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public void close() {
            // dummy
        }

        @Override
        public Result execute() {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public void consumeLastResult() {

        }

        @Override
        public void addBatch() {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public int[] executeBatchUpdate() {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public void executeBatch() {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public void select(int space, int index, int limit, int offset, Iter iterator) {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public void selectAll(int space, int limit, int offset) {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public void eval(String expression) {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public void insert(int space) {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public void replace(int space) {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public void delete(int space, int index) {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public void update(int space, int index) {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public void upsert(int space) {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public void change(IntOp op, int field, int arg) {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public void change(IntOp op, int field, long arg) {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public void change(Op op, int field, @Nullable String arg) {
            throw new TarantoolException("Not implemented!");

        }

        @Override
        public void change(Op op, int field, byte[] arg) {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public void sql(String sqlQuery) {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public int executeUpdate() {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public void ping() {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public void setNull() {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public void setBoolean(boolean val) {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public void setDouble(double val) {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public void setFloat(float val) {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public void setInt(int val) {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public void setLong(long val) {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public void setString(@Nullable String val) {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public void setBytes(byte[] bytes) {
            throw new TarantoolException("Not implemented!");
        }

        @Override
        public String getVersion() {
            throw new TarantoolException("Not implemented!");
        }
    }

    static class DummyCreationThrowingClient extends DummyTarantoolClient {

        DummyCreationThrowingClient(TarantoolConfig config) {
            super(config);
            throw new TarantoolException("Cannot create!");
        }
    }
}

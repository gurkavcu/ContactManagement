package com.vngrs.parser;

import com.vngrs.model.Contact;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * This class manages producer and consumer threads
 */
public class ContactParser {

    private static final Logger LOG = LogManager.getLogger(ContactParser.class.getName());

    public static class ParseResult{
        public long time;
        public long count;
        public ParseResult(long time, long count) {
            this.time = time;
            this.count = count;
        }
    }


    /**
     * Start a producer - consumer cycle.
     *
     * It create a producer and four consumer threads.
     * Once all threads started, it waits until consumer
     * thread finishes its work.
     *
     * @param Import file
     * @return ParseResult which contains elapsed time and processed item count
     * @throws FileNotFoundException
     */
    public static ParseResult parseContact(File xml) throws FileNotFoundException {

        if(!xml.exists()) {
            throw new FileNotFoundException();
        }

        BlockingQueue<Contact> q = new ArrayBlockingQueue<Contact>(1024);
        ExecutorService service = Executors.newFixedThreadPool(5);
        service.execute(new ContactProducer(q, xml));

        long totalConsumed = 0;
        long start = 0,end = 0;

        try {
            CountDownLatch latch = new CountDownLatch(1);

            start = System.currentTimeMillis();

            Set<Future<Long>> set = new HashSet<Future<Long>>();
            for (Integer consumerId : Arrays.asList(1,2,3,4)) {
                Callable<Long> callable = new ContactConsumer(q, latch);
                Future<Long> future = service.submit(callable);
                set.add(future);
            }

            for (Future<Long> future : set) {
                totalConsumed += future.get();
            }
            latch.await();

            end = System.currentTimeMillis();
            service.shutdown();
        } catch (Exception e) {
            LOG.error(e.toString());
        }
        return new ParseResult((end - start),totalConsumed);
    }
}

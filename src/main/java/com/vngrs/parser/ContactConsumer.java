package com.vngrs.parser;

import com.vngrs.model.Contact;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Consume contacts
 */
public class ContactConsumer implements Callable<Long> {

    private long totalConsumed ;

    private final BlockingQueue<Contact> queue;

    private final CountDownLatch countDownLatch;

    private static final Logger LOG =  LogManager.getLogger(ContactConsumer.class.getName());

    public ContactConsumer(BlockingQueue<Contact> queue, CountDownLatch countDownLatch) {
        this.queue = queue;
        this.countDownLatch = countDownLatch;
    }

    /**
     * <p>Take a contact from queue and import to mongodb.
     * This process will continue until we get a terminator object.</p>
     *
     * @return Imported contact count
     * @throws Exception
     */
    @Override
    public Long call() throws Exception {
        Contact contact;
        totalConsumed = 0l;
        try {
            while(countDownLatch.getCount()>0){
                // Try to get a contact from queue
                contact = queue.poll(100, TimeUnit.MILLISECONDS);
                if(contact == null)
                   continue;
                if(contact.isEof()) {
                    // This breaks all consumer thread's blocking cycle
                    countDownLatch.countDown();
                    break;
                }
                if(contact.upsert()) totalConsumed ++;
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return  totalConsumed;
    }
}

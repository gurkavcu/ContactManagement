package com.vngrs.parser;

import com.vngrs.model.Contact;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

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
                // Blocks the thread until we get a contact
                contact = queue.take();
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

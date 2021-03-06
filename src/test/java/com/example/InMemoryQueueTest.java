package com.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.example.common.Constants;
import com.example.common.Message;
import com.example.common.RetrievalRequest;
import com.example.common.exceptions.InvalidRetrievalCountException;
import com.example.common.exceptions.InvalidVisibilityTimeout;
import com.example.common.exceptions.MessageNotInvisibleException;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class InMemoryQueueTest {

	@Rule
	public Timeout globalTimeout = Timeout.millis(999); // < 1 second per test

	private static InMemoryQueueService queue;

	@BeforeClass
	public static void setupQueue() {
		queue = new InMemoryQueueService();
	}

	@AfterClass
	public static void stopTimer() {
		queue.stopTimeoutTask();
	}

	@After
	public void resetQueue() {
		queue.purge();
	}

	@Test
	public void putMessage() {
		assertEquals("Queue should be empty", 0, queue.size());
		queue.put("HelloWorld");
		assertEquals("Queue should have 1 element", 1, queue.size());
	}

	@Test
	public void testNoMessagesRetrieval() {
		List<Message> messages = queue.pull(new RetrievalRequest(1));
		assertEquals(0, messages.size());
	}

	@Test
	public void deleteMessage() {
		queue.put("HelloWorld");
		List<Message> messages = queue.pull(new RetrievalRequest(1));
		String receiptHandle = messages.get(0).getReceiptHandle();

		assertEquals(1, queue.getInvisibleMessages().size());
		queue.delete(receiptHandle);
		assertEquals(0, queue.getInvisibleMessages().size());
		assertEquals(0, queue.size());
	}

	@Test(expected = MessageNotInvisibleException.class)
	public void testInvalidReceipt() {
		queue.put("HelloWorld");
		queue.delete("invalidreceipt");
	}

	@Test
	public void canGetMultipleMessages() {
		queue.put("HelloWorld1");
		queue.put("HelloWorld2");

		List<Message> messages = queue.pull(new RetrievalRequest(2));
		assertEquals(messages.size(), 2);
	}

	@Test
	public void pullMessage() {
		queue.put("HelloWorld");
		List<Message> messages = queue.pull(new RetrievalRequest(1));
		assertEquals("Should retrieve message correctly", "HelloWorld", messages.get(0).getBody());
	}

	@Test
	public void doesNotServeInvisibleMessages1() {
		queue.put("HelloWorld1");
		Message sent2 = queue.put("HelloWorld2");
		queue.pull(new RetrievalRequest(1)).get(0);
		List<Message> messages = queue.getMessages();
		assertEquals(messages.get(0).getBody(), sent2.getBody());
	}
	
	@Test
	public void doesNotServeInvisibleMessages2() {
		int testSize = 5;
		for (int i = 0; i < testSize; i++) {
			queue.put("HelloWorld" + i);
		}
		List<String> firstAttempt  = Lists.newArrayList();
		Set<String> secondAttempt = Sets.newHashSet();
		boolean keepGoing = true;
		for (int i = 0; i < testSize; i++) {
			Message msg = queue.pull(new RetrievalRequest(1, 150)).get(0);
			firstAttempt.add(msg.getBody());
		}
		while(keepGoing) {
			List<Message> messages = queue.pull(new RetrievalRequest(1));
			if (!messages.isEmpty()) {
				Message msg = messages.get(0);
				secondAttempt.add(msg.getBody());
				if (queue.size() == 0 && queue.getInvisibleMessages().size() == testSize) {
					keepGoing = false;
				}
			}
		}
		assertEquals(firstAttempt.size(), secondAttempt.size());
		for (int i = 0; i < testSize; i++) {
			assertTrue(secondAttempt.contains(firstAttempt.get(i)));
		}
	}

	@Test(expected = InvalidRetrievalCountException.class)
	public void checkMaxRetrievalCount() {
		queue.pull(new RetrievalRequest(Constants.maxRetrievalCount + 1));
	}

	@Test(expected = InvalidVisibilityTimeout.class)
	public void checkMaxVisibilityTimeout() {
		queue.pull(new RetrievalRequest(1, Constants.maxVisibilityTimeout + 1));
	}

	@Test
	public void tracksInvisibleMessages() {
		queue.put("HelloWorld");
		assertEquals(queue.size(), 1);
		assertEquals(queue.numberOfInvisibleMessages(), 0);

		queue.pull(new RetrievalRequest(1));
		assertEquals(queue.size(), 0);
		assertEquals(queue.numberOfInvisibleMessages(), 1);
	}
	
	@Test
	public void multiThreadPutPull() {
		int numThreads = 9;
		int numIterations = 10;
		ExecutorService service = Executors.newFixedThreadPool(numThreads);
		List<Callable<Object>> threads = new ArrayList<Callable<Object>>(numThreads);
		for (int i = 1; i <= numThreads; i++) {
			threads.add(Executors.callable(new PutTask(i, numIterations)));
		}
		
		try {
			service.invokeAll(threads);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		
		List<String> result = Lists.newArrayList();
		while(queue.size() > 0) {
			List<Message> messages = queue.pull(new RetrievalRequest(10, 100000));
			for (Message msg : messages) {
				result.add(msg.getBody());
			}
		}
		
		assertEquals(numThreads * numIterations, result.size());
		for (int i = 10; i <= 99; i++) {
			assertTrue(result.contains(String.valueOf(i)));
		}
	}
	
	private static class PutTask implements Runnable {

		int threadNumber;
		int numIterations;
		
		public PutTask(int threadNumber, int numIterations) {
			this.threadNumber = threadNumber;
			this.numIterations = numIterations;
		}

		@Override
		public void run() {
			for (int i = 0; i < numIterations; i++) {
				queue.put(String.valueOf(threadNumber) + i);
			}
		}

	}
}

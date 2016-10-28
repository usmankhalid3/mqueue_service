package com.example;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.example.common.Constants;
import com.example.common.Message;
import com.example.common.QueueConfig;
import com.example.common.RetrievalRequest;
import com.example.common.Util;
import com.example.common.exceptions.InvalidRetrievalCountException;
import com.example.common.exceptions.InvalidVisibilityTimeout;
import com.google.common.collect.Lists;

public class InMemoryQueueService implements QueueService { 
	
	private QueueConfig config;
	private Object lock = new Object();
	private Deque<Message> queue = new ArrayDeque<Message>();
	private Map<String, Message> invisibleMessages = new HashMap<String, Message>();
	private ScheduledExecutorService taskService = null;
	
	public InMemoryQueueService(QueueConfig config) {
		this.config = config;
		this.taskService = Executors.newScheduledThreadPool(1);
		startTimeoutTask();
	}
	
	public void startTimeoutTask() {
		Runnable task = new Runnable(){
			@Override
			public void run() {
				timeoutInvisibleMessages();
			}
		};
		// invoke timeout checking task every 10ms
		taskService.scheduleWithFixedDelay(task, 0, 10, TimeUnit.MILLISECONDS);
	}
	
	public void stopTimeoutTask() {
		taskService.shutdown();
	}
	
	@Override
	public void purge() {
		synchronized (lock) {
			queue.clear();
			invisibleMessages.clear();
		}
	}
	
	@Override
	public Message put(String data) {
		synchronized (lock) {
			Message msg = new Message(data);
			queue.offerLast(msg);
			return msg;
		}
	}

	@Override
	public List<Message> pull(RetrievalRequest request) {
		int numMsgs = request.getNumberOfMessages();
		if (!Util.existsInRange(numMsgs, 0, Constants.maxRetrievalCount)) {
			throw new InvalidRetrievalCountException();
		}
		if (!Util.existsInRange(request.getVisibilityTimeout(), 0, Constants.maxVisibilityTimeout)) {
			throw new InvalidVisibilityTimeout();
		}
		if (queue.isEmpty()) {
			return Lists.newArrayList();
		}
		synchronized (lock) {
			int numExistingMessages = queue.size();
			int resultSize = numMsgs > numExistingMessages ? numMsgs : numMsgs;
			List<Message> result = new ArrayList<Message>(resultSize);
			for (int i = 0; i < resultSize; i++) {
				Message msg = queue.pollFirst();
				if (msg != null) {
					msg.setExpiryTime(request.getVisibilityTimeout());
					String receiptHandle = Util.getRandomId();
					invisibleMessages.put(receiptHandle, msg);
					msg.setReceiptHandle(receiptHandle);
					result.add(msg);
				}
			}
			return result;
		}
	}

	@Override
	public void delete(String receiptHandle) {
		synchronized (lock) {
			invisibleMessages.remove(receiptHandle);
		}
	}

	@Override
	public int size() {
		return queue.size(); 
	}
	
	public String getName() {
		return config.getQueueName();
	}
	
	public List<Message> getMessages() {
		return Lists.newArrayList(queue);
	}
	
	public List<Message> getInvisibleMessages() {
		return Lists.newArrayList(invisibleMessages.values());
	}
	
	private void timeoutInvisibleMessages() {
		synchronized (lock) {
			Map<String, Message> messagesToBeDeleted = new HashMap<String, Message>(); 
			for (String key : invisibleMessages.keySet()) {
				Message msg = invisibleMessages.get(key);
				if (msg.hasExpired(Util.now())) {
					messagesToBeDeleted.put(key, msg);
				}
			}
			
			for (String key : messagesToBeDeleted.keySet()) {
				Message msg = messagesToBeDeleted.get(key);
				msg.expire();
				queue.offerFirst(msg);
				delete(key);
			}
		}
	}

	@Override
	public int numberOfInvisibleMessages() {
		return invisibleMessages.size();
	}
}

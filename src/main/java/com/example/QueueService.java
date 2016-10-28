package com.example;

import java.util.List;

import com.example.common.Message;
import com.example.common.RetrievalRequest;

public interface QueueService {
	
	public Message put(String data);
	public List<Message> pull(RetrievalRequest request);
	public void delete(String receiptHandle);
	public int size();
	public void purge();
	public int numberOfInvisibleMessages();
}

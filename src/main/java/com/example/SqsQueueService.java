package com.example;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.example.common.Message;
import com.example.common.RetrievalRequest;
import com.google.common.collect.Lists;

public class SqsQueueService implements QueueService {
	
	private String queueUrl;
	private AmazonSQSClient sqs;

	public SqsQueueService(AmazonSQSClient sqs, String queueUrl) {
		if (sqs == null) {
			throw new RuntimeException("sqsClient found null");
		}
		this.sqs = sqs;
		this.queueUrl = queueUrl;
	}

	@Override
	public Message put(String data) {
		SendMessageResult result = sqs.sendMessage(new SendMessageRequest(queueUrl, data));
		return new Message(data);
		//TODO use the SendMessageResult to verify md5 signature of the sent message
		// and incorporate this logic into the overall design
	}

	@Override
	public List<Message> pull(RetrievalRequest request) {
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
		receiveMessageRequest.setMaxNumberOfMessages(request.getNumberOfMessages());
		receiveMessageRequest.setVisibilityTimeout((int)TimeUnit.MILLISECONDS.toSeconds(request.getVisibilityTimeout()));
		List<com.amazonaws.services.sqs.model.Message> sqsMessages = sqs.receiveMessage(receiveMessageRequest).getMessages();
		List<Message> result = Lists.newArrayList();
		for (com.amazonaws.services.sqs.model.Message sqsMessage : sqsMessages) {
			Message message = new com.example.common.Message(sqsMessage.getBody());
			String handle = sqsMessage.getReceiptHandle();
			message.setReceiptHandle(handle);
			result.add(message);
		}
		return result;
	}

	@Override
	public void delete(String receiptHandle) {
		sqs.deleteMessage(new DeleteMessageRequest(queueUrl, receiptHandle));
	}

	@Override
	public int size() {
		return getSizeAttribute("ApproximateNumberOfMessages");
	}

	@Override
	public void purge() {
		sqs.purgeQueue(new PurgeQueueRequest(queueUrl));
	}

	@Override
	public int numberOfInvisibleMessages() {
		return getSizeAttribute("ApproximateNumberOfMessagesNotVisible");
	}
	
	private int getSizeAttribute(String attributeName) {
		List<String> attributes = Lists.newArrayList(attributeName);
		GetQueueAttributesResult result = sqs.getQueueAttributes(new GetQueueAttributesRequest(queueUrl, attributes));
		return Integer.parseInt(result.getAttributes().get(attributeName));	
	}
}

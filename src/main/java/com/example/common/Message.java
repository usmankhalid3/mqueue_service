package com.example.common;

public class Message {

	private String body;
	private String id;
	private long visibilityTimeout;
	private String receiptHandle;
	
	public Message(String body) {
		this.body = body;
		this.id = Util.getRandomId();		
	}
	
	public void expire() {
		visibilityTimeout = 0;
	}
	
	public boolean hasExpired(long timestamp) {
		return visibilityTimeout == 0 || visibilityTimeout < timestamp;
	}
	
	public void setExpiryTime(long millis) {
		visibilityTimeout = Util.now() + millis;
	}
	
	public String getReceiptHandle() {
		return receiptHandle;
	}

	public void setReceiptHandle(String receiptHandle) {
		this.receiptHandle = receiptHandle;
	}

	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public long getVisibilityTimeout() {
		return visibilityTimeout;
	}
	public void setVisibilityTimeout(long visibilityTimeout) {
		this.visibilityTimeout = visibilityTimeout;
	}
}

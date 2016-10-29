package com.example.common;

public class RetrievalRequest {

	private int numberOfMessages;
	private int visibilityTimeout; // in millis
	
	public RetrievalRequest() {
		
	}
	public RetrievalRequest(int numberOfMessages) {
		super();
		this.numberOfMessages = numberOfMessages;
		this.visibilityTimeout = Constants.defaultVisibilityTimeout;
	}
	public RetrievalRequest(int numberOfMessages, int visibilityTimeout) {
		super();
		this.numberOfMessages = numberOfMessages;
		this.visibilityTimeout = visibilityTimeout;
	}
	public int getNumberOfMessages() {
		return numberOfMessages;
	}
	public void setNumberOfMessages(int numberOfMessages) {
		this.numberOfMessages = numberOfMessages;
	}
	public int getVisibilityTimeout() {
		return visibilityTimeout;
	}
	public void setVisibilityTimeout(int visibilityTimeout) {
		this.visibilityTimeout = visibilityTimeout;
	}
}

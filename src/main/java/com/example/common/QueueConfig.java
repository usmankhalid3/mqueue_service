package com.example.common;

public class QueueConfig {

	private String queueName;
	
	public QueueConfig() {
		
	}
	
	public QueueConfig(String queueName) {
		this.queueName = queueName;
	}
	public String getQueueName() {
		return queueName;
	}
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
}

package com.example;

import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.example.common.Message;
import com.example.common.RetrievalRequest;

public class MainFile {

	public static void main(String[] args) {
		testInMemory();
//		testSQS();
	}
	
	private static void testInMemory() {
		QueueService service = new InMemoryQueueService("TestQueue1");
		performTest(service);
	}

	private static void testSQS() {
		AWSCredentials creds = null;
		creds = new ProfileCredentialsProvider().getCredentials();
		String queueName = "CanvaTest1";
		AmazonSQSClient sqs = new AmazonSQSClient(creds);
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		sqs.setRegion(usWest2);
		CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
		String queueUrl =  sqs.createQueue(createQueueRequest).getQueueUrl();
		System.out.println("Created queue: " + queueUrl);
		QueueService service = new SqsQueueService(sqs, queueUrl);
//		System.out.println(service.size());
		performTest(service);
	}
	
	private static void performTest(QueueService service) {
		new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < 20; i++) {
					String msg = "hello" + i;
					service.put(msg);
					System.out.println("Pushed " + msg);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
		new Thread() {
			@Override
			public void run() {
				while (true) {
					List<Message> result = service.pull(new RetrievalRequest(10, 1000));
					for (Message msg : result) {
						System.out.println(msg.getBody());
					}
				}
			}
		}.start();
	}
}

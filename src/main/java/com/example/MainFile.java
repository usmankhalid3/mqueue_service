package com.example;

import java.util.List;

import com.example.common.Message;
import com.example.common.QueueConfig;
import com.example.common.RetrievalRequest;

public class MainFile {

	public static void main(String[] args) {
		testInMemory();
	}

	private static void testInMemory() {
		QueueService service = new InMemoryQueueService(new QueueConfig("TestQueue1"));
		new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < 5; i++) {
					String msg = "hello" + i;
					service.put(msg);
					System.out.println("Pushed " + msg);
					try {
						Thread.sleep(3000);
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
					List<Message> result = service.pull(new RetrievalRequest(1));
					for (Message msg : result) {
						System.out.println(msg.getBody());
					}
				}
			}
		}.start();
	}
}

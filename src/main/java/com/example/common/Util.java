package com.example.common;

import java.util.UUID;

public class Util {

	public static String getRandomId() {
		return UUID.randomUUID().toString();
	}
	
	public static long now() {
		return System.currentTimeMillis();
	}
	
	public static boolean existsInRange(int num, int min, int max) {
		return num >= min && num <= max;
	}
}

package com.example.common.exceptions;

import com.example.common.Constants;

public class InvalidVisibilityTimeout extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public InvalidVisibilityTimeout() {
		super("Invalid Visibility Timeout. Please specify a value between 0 & " + Constants.maxVisibilityTimeout);
	}
	
}

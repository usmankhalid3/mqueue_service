package com.example.common.exceptions;

import com.example.common.Constants;

public class InvalidRetrievalCountException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public InvalidRetrievalCountException() {
		super("Invalid Retrieval Count. Please specify a value between 1 & " + Constants.maxRetrievalCount);
	}
	
}

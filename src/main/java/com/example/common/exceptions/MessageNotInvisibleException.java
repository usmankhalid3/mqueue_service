package com.example.common.exceptions;

public class MessageNotInvisibleException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public MessageNotInvisibleException() {
		super("Invalid message handle. Either you did not pull this message before attempting to delete or provided an incorrect handle");
	}

}

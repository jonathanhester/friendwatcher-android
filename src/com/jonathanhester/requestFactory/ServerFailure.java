package com.jonathanhester.requestFactory;

public class ServerFailure {
	
	String message;
	
	public ServerFailure(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}

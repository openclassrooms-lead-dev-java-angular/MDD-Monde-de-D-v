package com.openclassrooms.mddapi.storage.exception;

public class StorageException extends RuntimeException {

	public StorageException() {
	    super("Error accessing storage");
    }
	
	public StorageException(String message) {
        super(message);
    }
}

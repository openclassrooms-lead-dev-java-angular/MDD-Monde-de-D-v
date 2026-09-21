package com.openclassrooms.mddapi.storage.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
	
	String upload(
			final MultipartFile file,
			final String resourceType,
			final String resourceIdentifier
	);
	
	boolean exists(
			final String resourceType,
			final String resourceIdentifier,
			final String originalFileName
	);
	
	void delete(
			final String filename
	);
}

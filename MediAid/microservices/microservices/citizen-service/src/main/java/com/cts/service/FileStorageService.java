package com.cts.service;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cts.exception.FileStorageException;

@Service
public class FileStorageService {
	
	@Value("${file.upload-dir}")
	private String uploadDir;
	
	public String storeFile(MultipartFile file)
	{
		try {
			Path uploadPath=Paths.get(uploadDir);
			
			if(!Files.exists(uploadPath))
			{
				Files.createDirectories(uploadPath);
			}
			
			//generate unique filename to avoid conflicts
			
			String fileName=UUID.randomUUID()+"_"+file.getOriginalFilename();
			Path filePath=uploadPath.resolve(fileName);
			
			Files.copy(file.getInputStream(), filePath,StandardCopyOption.REPLACE_EXISTING);
			
			//return the path as string 
			return fileName;
		}
		catch(IOException e)
		{
			throw new FileStorageException("Failed to store file: "+e.getMessage());
			
		}
	}
	
}

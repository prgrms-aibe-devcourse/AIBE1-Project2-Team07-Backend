package org.lucky0111.pettalk.service.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileUploaderService {
    String uploadFile(MultipartFile file) throws IOException;
}

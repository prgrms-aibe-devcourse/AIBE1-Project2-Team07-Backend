package org.lucky0111.pettalk.service.file;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileUploaderService {
    /**
     * 파일을 업로드합니다.
     * @param file 업로드할 파일
     * @param folderName 스토리지 내의 저장될 폴더 경로
     * @return 업로드된 파일의 접근 가능한 URL
     * @throws IOException 파일 입출력 오류 시
     * @throws FileUploadException 파일 업로드 비즈니스 로직 오류 또는 스토리지 오류 시 (필요하다면 Custom 예외 정의)
     */
    String uploadFile(MultipartFile file, String folderName) throws IOException; // IOException은 MultipartFile에서 발생 가능

    /**
     * 지정된 URL의 파일을 삭제합니다.
     * @param fileUrl 삭제할 파일의 URL
     * @throws FileDeletionException 파일 삭제 비즈니스 로직 오류 또는 스토리지 오류 시 (필요하다면 Custom 예외 정의)
     */
    void deleteFile(String fileUrl); // delete는 예외를 던지거나 void 반환

    // 필요에 따라 다른 파일 관련 메소드 추가 (예: downloadFile 등)
}

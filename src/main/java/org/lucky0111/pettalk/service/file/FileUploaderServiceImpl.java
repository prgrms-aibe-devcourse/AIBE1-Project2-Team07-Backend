package org.lucky0111.pettalk.service.file;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploaderServiceImpl implements FileUploaderService {

    private final S3Client s3Client;

    @Value("${CLOUD.AWS.S3.BUCKET}")
    private String bucketName;

    // **** FileUploaderService 인터페이스의 메소드 구현 ****

    @Override // 인터페이스 메소드 구현 명시
    public String uploadFile(MultipartFile file, String folderName) {
        try {
            // 파일명을 UUID로 생성
            String fileName = folderName + UUID.randomUUID() + "_" + file.getOriginalFilename();

            // S3 업로드 요청 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            // 파일을 S3에 업로드 (InputStream을 사용하여 파일 데이터 전송)
            PutObjectResponse response = s3Client.putObject(putObjectRequest,
                    software.amazon.awssdk.core.sync.RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // 업로드된 파일의 URL 반환
            return "https://" + bucketName + ".s3.amazonaws.com/" + fileName;

        } catch (S3Exception | IOException e) {
            throw new RuntimeException("파일 업로드 오류 발생", e);
        }
    }


    @Override
    public void deleteFile(String fileUrl) {
        String s3ObjectKey = extractS3ObjectKeyFromUrl(fileUrl);

        try {
            // S3 삭제 요청 객체 (DeleteObjectRequest) 빌더 생성
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3ObjectKey)
                    .build();
            // S3 클라이언트의 deleteObject 메소드를 사용하여 파일 삭제 실행
            s3Client.deleteObject(deleteObjectRequest);

        } catch (S3Exception e) {
            throw new RuntimeException("S3 파일 삭제 실패: " + fileUrl + " - " + e.getMessage(), e);
        }
    }

    /**
     * 파일 URL에서 S3 객체 키(버킷 이름 제외한 경로/파일 이름)를 추출하는 헬퍼 메소드
     * S3 퍼블릭 URL 형식 (https://[bucket-name].s3.[region].amazonaws.com/[key]) 가정
     * 실제 사용되는 URL 형식에 맞춰 파싱 로직을 구현해야 합니다. (CloudFront 등 사용 시)
     */
    private String extractS3ObjectKeyFromUrl(String fileUrl) {
        try {
            //URL 파싱
            URL url = new URL(fileUrl);
            String path = url.getPath(); // Object Key에 해당, 앞에 '/' 포함

            // Path에서 선행 '/' 제거하여 순수한 Object Key 반환
            // 예: "/trainer-photos/abc.jpg" -> "trainer-photos/abc.jpg"
            return path.startsWith("/") ? path.substring(1) : path;

        } catch (MalformedURLException e) {
            throw new RuntimeException("유효하지 않은 파일 URL 형식으로 Object Key 추출 실패: " + fileUrl, e);
        }
    }
}

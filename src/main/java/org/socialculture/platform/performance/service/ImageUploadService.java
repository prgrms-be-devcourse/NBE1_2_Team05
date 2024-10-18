package org.socialculture.platform.performance.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.socialculture.platform.global.apiResponse.exception.ErrorStatus;
import org.socialculture.platform.global.apiResponse.exception.GeneralException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

/**
 * FTP 서버에 공연 이미지 등록 서비스
 *
 * @author ycjung
 */
@Service
@Slf4j
public class ImageUploadService {

    @Value("${ftp.host}")
    private String ftpHost;

    @Value("${ftp.username}")
    private String ftpUsername;

    @Value("${ftp.password}")
    private String ftpPassword;

    @Value("${ftp.poster-path}") // 설정 파일에서 포스터 저장 경로를 가져옴
    private String ftpPosterPath;

    @Value("${nginx.server.base.url}") // Nginx 서버의 기본 URL 정보 가져오기
    private String nginxBaseUrl;

    public String uploadFileToFTP(MultipartFile imageFile) {
        if(Objects.isNull(imageFile)) return null;

        FTPClient ftpClient = null;
        try {
            ftpClient = connectToFtp();

            // UUID 기반 고유 파일명 생성 및 경로 결정 (경로는 /upload/pfmPoster/UUID.확장자)
            String uuidFileName = generateUuidFileName(imageFile.getOriginalFilename());
            String imageUrl = buildImageUrl(uuidFileName); // /upload/pfmPoster/UUID.확장자

            // 로그 추가: 파일 업로드 시작
            log.debug("Starting FTP file upload. File: {}, UUID File Name: {}", imageFile.getOriginalFilename(), uuidFileName);

            // FTP 서버에 파일 업로드
            uploadToFtp(ftpClient, imageFile, uuidFileName);

            // 로그 추가: 파일 업로드 성공
            log.info("File upload to FTP server completed successfully. Image URL: {}", imageUrl);
            return nginxBaseUrl + imageUrl;
        } catch (IOException e) {
            log.error("Error during FTP file upload. File: {}, Error: {}", imageFile.getOriginalFilename(), e.getMessage());
            throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
        } finally {
            closeFtpClient(ftpClient);
        }


    }

    // FTP 클라이언트 연결 설정
    private FTPClient connectToFtp() throws IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(ftpHost);
        ftpClient.login(ftpUsername, ftpPassword);
        ftpClient.enterLocalPassiveMode(); // 수동 모드 설정
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE); // 바이너리 전송 모드 설정
        return ftpClient;
    }

    // FTP 클라이언트 종료
    private void closeFtpClient(FTPClient ftpClient) {
        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException ex) {
                log.error("Error while disconnecting FTP client: {}", ex.getMessage(), ex);
                throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
            }
        }
    }

    // FTP 업로드
    private void uploadToFtp(FTPClient ftpClient, MultipartFile file, String fileName) throws IOException {
        String remotePath = ftpPosterPath + fileName; // 포스터 경로 설정 (/upload/pfmPoster/UUID.확장자)
        try (InputStream inputStream = file.getInputStream()) {
            if (!ftpClient.storeFile(remotePath, inputStream)) {
                String reply = ftpClient.getReplyString(); // FTP 서버 응답 로그
                throw new RuntimeException("FTP 서버에 파일 업로드 실패: " + fileName + ". 응답: " + reply);
            }
        }
    }

    // UUID 기반 파일명 생성
    private String generateUuidFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName); // 파일 확장자 추출
        String uuid = UUID.randomUUID().toString(); // UUID 생성
        return uuid + extension; // UUID + 확장자로 고유 파일명 생성
    }

    // 이미지 URL 생성 (IP나 포트 정보 없이 저장)
    private String buildImageUrl(String fileName) {
        return String.format("%s%s", ftpPosterPath, fileName); // /upload/pfmPoster/UUID.확장자 형식으로 경로 반환
    }

    // 파일 확장자 추출
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }
}

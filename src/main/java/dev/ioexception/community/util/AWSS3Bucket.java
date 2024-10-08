package dev.ioexception.community.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class AWSS3Bucket {

    private final AmazonS3 amazonS3Client;
    @Value("${cloud.aws.s3.bucketName}")
    private String BUCKET_NAME;

    public String uploadS3(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType());
        objectMetadata.setContentLength(file.getSize());

        amazonS3Client.putObject(
                new PutObjectRequest(BUCKET_NAME, fileName, file.getInputStream(), objectMetadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead));

        return amazonS3Client.getUrl(BUCKET_NAME, fileName).toString();
    }

    public void deleteImage(String url) {
        String key = extractKeyNameFromUrl(url);

        amazonS3Client.deleteObject(new DeleteObjectRequest(BUCKET_NAME, key));
    }

    private String extractKeyNameFromUrl(String url) {

        return url.substring(url.lastIndexOf("/") + 1);
    }
}

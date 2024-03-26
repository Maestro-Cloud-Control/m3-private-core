package io.maestro3.agent.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.DirectoryDownload;
import software.amazon.awssdk.transfer.s3.model.DirectoryUpload;
import software.amazon.awssdk.transfer.s3.model.DownloadDirectoryRequest;
import software.amazon.awssdk.transfer.s3.model.UploadDirectoryRequest;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3FileManager implements IFileManager {

    private final S3Client s3Client;
    private final S3TransferManager transferManager;

    @Autowired
    public S3FileManager(S3Client s3Client,
                         S3TransferManager transferManager) {
        this.s3Client = s3Client;
        this.transferManager = transferManager;
    }

    @Override
    public void uploadFile(final String bucketName, final String key, final byte[] fileContent) {
        final PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileContent));
    }

    @Override
    public void uploadFile(final String bucketName, final String key, final String fileContent) {
        this.uploadFile(bucketName, key, fileContent.getBytes());
    }

    @Override
    public void uploadDirectory(final String bucketName, final String key, final File directory) {
        UploadDirectoryRequest request = UploadDirectoryRequest.builder()
                .source(Path.of(directory.getAbsolutePath()))
                .bucket(bucketName)
                .s3Prefix(key)
                .build();
        DirectoryUpload directoryUpload = transferManager.uploadDirectory(request);
        directoryUpload.completionFuture().join();
    }

    @Override
    public void downloadDirectory(final String bucketName, final String key, final File targetDirectory) {
        DownloadDirectoryRequest request = DownloadDirectoryRequest.builder()
                .bucket(bucketName)
                .destination(targetDirectory.toPath())
                .listObjectsV2RequestTransformer(transformer -> transformer.prefix(key))
                .build();
        DirectoryDownload directoryDownload = transferManager.downloadDirectory(request);
        directoryDownload.completionFuture().join();
    }

    @Override
    public byte[] getFileContent(final String bucketName, final String key) {
        final GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        final ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
        return objectBytes.asByteArray();
    }

    @Override
    public void copyFile(final String sourceBucket, final String sourceKey,
                         final String destinationBucket, final String destinationKey) {
        final CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                .sourceBucket(sourceBucket)
                .sourceKey(sourceKey)
                .destinationBucket(destinationBucket)
                .destinationKey(destinationKey)
                .build();
        s3Client.copyObject(copyRequest);
    }

    @Override
    public void deleteDirectory(final String bucketName, final String key) {
        final ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder()
                .bucket(bucketName)
                .prefix(key)
                .build();
        final ListObjectsResponse listObjectsResponse = s3Client.listObjects(listObjectsRequest);
        final List<S3Object> contents = listObjectsResponse.contents();
        if (contents.isEmpty()) {
            return;
        }

        final Collection<ObjectIdentifier> objectsToDelete = contents.stream()
                .map(S3Object::key)
                .map(keyToDelete -> ObjectIdentifier.builder().key(keyToDelete).build())
                .collect(Collectors.toList());
        final DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                .bucket(bucketName)
                .delete(builder -> builder.objects(objectsToDelete).build())
                .build();
        s3Client.deleteObjects(deleteObjectsRequest);
    }
}

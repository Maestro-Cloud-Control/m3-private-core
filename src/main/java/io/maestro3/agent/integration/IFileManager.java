package io.maestro3.agent.integration;

import java.io.File;

public interface IFileManager {
    void uploadFile(String bucketName, String key, byte[] fileContent);

    void uploadFile(String bucketName, String key, String fileContent);

    void uploadDirectory(String bucketName, String key, File directory);

    void downloadDirectory(String bucketName, String key, File targetDirectory);

    byte[] getFileContent(String bucketName, String key);

    void copyFile(String sourceBucket, String sourceKey, String destinationBucket, String destinationKey);

    void deleteDirectory(String bucketName, String key);
}

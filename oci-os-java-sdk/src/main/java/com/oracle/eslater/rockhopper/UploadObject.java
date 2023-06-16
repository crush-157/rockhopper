package com.oracle.eslater.rockhopper;

import java.io.File;
import java.util.Map;

import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.transfer.UploadConfiguration;
import com.oracle.bmc.objectstorage.transfer.UploadManager;
import com.oracle.bmc.objectstorage.transfer.UploadManager.UploadRequest;
import com.oracle.bmc.objectstorage.transfer.UploadManager.UploadResponse;

public class UploadObject {

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            throw new IllegalArgumentException(
                "Usage: java "
                + UploadObject.class.getName()
                + " <namespaceName> <bucketName> <objectName> <inputFilePath>");
        }
        
        String namespaceName = args[0];
        String bucketName = args[1];
        String objectName = args[2];
        File body = new File(args[3]);
        Map<String, String> metadata = null;
        String contentType = null;
        String contentEncoding = null;
        String contentLanguage = null;

        UploadConfiguration uploadConfiguration =
            UploadConfiguration.builder()
                .allowMultipartUploads(true)
                .allowParallelUploads(true)
                .build();

        UploadManager uploadManager = new UploadManager(ClientFactory.getOsClient(), uploadConfiguration);

        PutObjectRequest request =
            PutObjectRequest.builder()
                .bucketName(bucketName)
                .namespaceName(namespaceName)
                .objectName(objectName)
                .contentType(contentType)
                .contentLanguage(contentLanguage)
                .contentEncoding(contentEncoding)
                .opcMeta(metadata)
                .build();

        UploadRequest uploadDetails =
            UploadRequest.builder(body).allowOverwrite(true).build(request);

        UploadResponse response = uploadManager.upload(uploadDetails);
        System.out.println(response);
    }
}

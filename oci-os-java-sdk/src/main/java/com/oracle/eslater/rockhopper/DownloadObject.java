package com.oracle.eslater.rockhopper;

import java.io.File;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import com.oracle.bmc.objectstorage.transfer.DownloadConfiguration;
import com.oracle.bmc.objectstorage.transfer.DownloadManager;

public class DownloadObject {

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            throw new IllegalArgumentException(
                "Usage: java "
                + DownloadObject.class.getName()
                + " <namespaceName> <bucketName> <objectName> <outputFileName>");
        }

        String namespaceName = args[0];
        String bucketName = args[1];
        String objectName = args[2];
        String outputFileName = args[3];

        DownloadConfiguration downloadConfiguration =
            DownloadConfiguration.builder()
                .parallelDownloads(3)
                .maxRetries(3)
                .multipartDownloadThresholdInBytes(6 * 1024 * 1024)
                .partSizeInBytes(4 * 1024 * 1024)
                .build();

        DownloadManager downloadManager = new DownloadManager(ClientFactory.getOsClient(), downloadConfiguration);

        GetObjectRequest request =
            GetObjectRequest.builder()
                .namespaceName(namespaceName)
                .bucketName(bucketName)
                .objectName(objectName)
                .build();

        GetObjectResponse response = downloadManager.downloadObjectToFile(request, new File(outputFileName));
        System.out.println("Downloaded " + response.getContentLength() + " bytes.");
    }
}

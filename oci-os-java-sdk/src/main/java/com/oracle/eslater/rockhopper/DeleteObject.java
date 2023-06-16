package com.oracle.eslater.rockhopper;

import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.responses.DeleteObjectResponse;

public class DeleteObject {

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            throw new IllegalArgumentException(
                "Usage: java "
                + DeleteObject.class.getName()
                + " <namespaceName> <bucketName> <objectName>");
        }
        
        String namespaceName = args[0];
        String bucketName = args[1];
        String objectName = args[2];

        DeleteObjectRequest deleteRequest =
            DeleteObjectRequest.builder()
                .bucketName(bucketName)
                .namespaceName(namespaceName)
                .objectName(objectName)
                .build();

        DeleteObjectResponse response = ClientFactory.getOsClient().deleteObject(deleteRequest);
        System.out.println(response);
    }
}

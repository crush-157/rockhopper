package com.oracle.eslater.rockhopper;

import java.io.IOException;
import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;

public class ClientFactory {
    public static ObjectStorageClient getOsClient() throws IOException {
        return ObjectStorageClient.builder()
            .build(new ConfigFileAuthenticationDetailsProvider(ConfigFileReader.parseDefault()));
    }
}

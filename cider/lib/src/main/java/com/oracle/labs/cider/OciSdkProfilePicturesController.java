// Copyright (c) 2023, Oracle.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.oracle.labs.cider;
import static io.micronaut.http.MediaType.IMAGE_JPEG_TYPE;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import com.oracle.bmc.objectstorage.transfer.DownloadConfiguration;
import com.oracle.bmc.objectstorage.transfer.DownloadManager;
import com.oracle.bmc.objectstorage.transfer.UploadConfiguration;
import com.oracle.bmc.objectstorage.transfer.UploadManager;
import com.oracle.bmc.objectstorage.transfer.UploadManager.UploadRequest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.http.server.util.HttpHostResolver;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

@Requires(property="cloud.sdk", value="OCI")
@Controller(OciSdkProfilePicturesController.PREFIX)
@ExecuteOn(TaskExecutors.IO)
class OciSdkProfilePicturesController implements ProfilePicturesApi {

    {
        System.out.println("OciSdkProfilePicturesController");
    }

    static final String PREFIX = "/pictures";
    private final ObjectStorageClient ociOsClient;
    private final HttpHostResolver httpHostResolver;
    private final UploadManager uploadManager;
    private final DownloadManager downloadManager;
    private final String namespace;
    private final String bucketName;

    public OciSdkProfilePicturesController(
        HttpHostResolver httpHostResolver,
        @Value("${micronaut.object-storage.oracle-cloud.default.bucket}") String bucketName,
        @Value("${micronaut.object-storage.oracle-cloud.default.namespace}")  String namespace
    ) throws IOException {
       this.ociOsClient = ObjectStorageClient.builder()
            .build(new ConfigFileAuthenticationDetailsProvider(ConfigFileReader.parseDefault()));
        UploadConfiguration uploadConfiguration = UploadConfiguration.builder()
            .allowMultipartUploads(true)
            .allowParallelUploads(true)
            .build();
        this.uploadManager = new UploadManager(this.ociOsClient, uploadConfiguration);
        DownloadConfiguration downloadConfiguration =
            DownloadConfiguration.builder()
                .parallelDownloads(3)
                .maxRetries(3)
                .multipartDownloadThresholdInBytes(6 * 1024 * 1024)
                .partSizeInBytes(4 * 1024 * 1024)
                .build();
        this.downloadManager = new DownloadManager(ociOsClient, downloadConfiguration);
        this.httpHostResolver = httpHostResolver;
        this.bucketName = bucketName;
        this.namespace = namespace;
    }

    @Override
    public HttpResponse<?> upload(CompletedFileUpload fileUpload,
                                  String userId,
                                  HttpRequest<?> request) {
        String key = buildKey(userId);
        PutObjectRequest putRequest =
            PutObjectRequest.builder()
                .bucketName(this.bucketName)
                .namespaceName(this.namespace)
                .objectName(key)
                .contentType(null)
                .contentLanguage(null)
                .contentEncoding(null)
                .opcMeta(null)
                .build();
        UploadRequest uploadDetails;
        try {
            uploadDetails = UploadRequest.builder(fileUpload.getInputStream(),fileUpload.getSize())
                .allowOverwrite(true).build(putRequest);
            this.uploadManager.upload(uploadDetails);
        } catch (IOException e) {
            return HttpResponse.serverError();
        }
        return HttpResponse.created(location(request, userId));
    }

    @Override
    public Optional<HttpResponse<StreamedFile>> download(String userId) {
        String key = buildKey(userId);
        GetObjectRequest request =
            GetObjectRequest.builder()
                .namespaceName(this.namespace)
                .bucketName(this.bucketName)
                .objectName(key)
                .build();
        GetObjectResponse response = this.downloadManager.getObject(request);
        return  Optional.of(buildStreamedFile(response, key));
    }

    @Override
    public void delete(String userId) {
        String key = buildKey(userId);
        DeleteObjectRequest deleteRequest =
            DeleteObjectRequest.builder()
                .bucketName(this.bucketName)
                .namespaceName(this.namespace)
                .objectName(key)
                .build();
        this.ociOsClient.deleteObject(deleteRequest);
    }

    private String buildKey(String userId) {
        return userId + ".jpg";
    }

    private URI location(HttpRequest<?> request, String userId) {
        return UriBuilder.of(httpHostResolver.resolve(request))
                .path(PREFIX)
                .path(userId)
                .build();
    }

    private static HttpResponse<StreamedFile> buildStreamedFile(GetObjectResponse response, String key) {
        StreamedFile file = new StreamedFile(response.getInputStream(), IMAGE_JPEG_TYPE).attach(key);
        MutableHttpResponse<Object> httpResponse = HttpResponse.ok();
        file.process(httpResponse);
        return httpResponse.body(file);
    }

}

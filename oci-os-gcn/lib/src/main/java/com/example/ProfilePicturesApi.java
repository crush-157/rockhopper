// Copyright (c) 2023, Oracle.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
package com.example;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Status;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.server.types.files.StreamedFile;

import java.util.Optional;

import static io.micronaut.http.HttpStatus.NO_CONTENT;
import static io.micronaut.http.MediaType.MULTIPART_FORM_DATA;

public interface ProfilePicturesApi {

    @Post(uri = "/{userId}", consumes = MULTIPART_FORM_DATA)
    HttpResponse<?> upload(CompletedFileUpload fileUpload, String userId, HttpRequest<?> request);

    @Get("/{userId}")
    Optional<HttpResponse<StreamedFile>> download(String userId);

    @Status(NO_CONTENT)
    @Delete("/{userId}")
    void delete(String userId);
}

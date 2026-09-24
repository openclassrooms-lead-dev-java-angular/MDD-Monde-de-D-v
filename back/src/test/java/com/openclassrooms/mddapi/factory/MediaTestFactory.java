package com.openclassrooms.mddapi.factory;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

public class MediaTestFactory {

    public static MockMultipartFile createMedia() {
        return new MockMultipartFile(
                "media",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image-content".getBytes());
    }
}

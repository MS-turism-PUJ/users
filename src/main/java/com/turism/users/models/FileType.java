package com.turism.users.models;

import java.util.Arrays;

import org.springframework.http.MediaType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@AllArgsConstructor
public enum FileType {
    JPG("jpg", MediaType.IMAGE_JPEG),
    JPEG("jpeg", MediaType.IMAGE_JPEG),
    PNG("png", MediaType.IMAGE_PNG),
    SVG("svg", MediaType.valueOf("image/svg+xml")),
    WEBP("webp", MediaType.valueOf("image/webp"));
    

    private final String extension;

    // Media type associated with the file extension
    private final MediaType mediaType;

    // Method to get MediaType based on the filename's extension
    public static MediaType fromFilename(String extension) {
        log.info("Getting media type from extension: {}", extension);
        return Arrays.stream(values())
                .filter(e -> e.getExtension().equals(extension))
                .findFirst()
                .map(FileType::getMediaType)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
    }
}

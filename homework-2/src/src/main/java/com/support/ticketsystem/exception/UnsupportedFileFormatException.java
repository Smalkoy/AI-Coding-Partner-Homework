package com.support.ticketsystem.exception;

/**
 * Exception thrown when an unsupported file format is uploaded.
 */
public class UnsupportedFileFormatException extends RuntimeException {

    private final String contentType;

    public UnsupportedFileFormatException(String contentType) {
        super("Unsupported file format: " + contentType);
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }
}

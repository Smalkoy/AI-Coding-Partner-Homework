package com.support.ticketsystem.exception;

/**
 * Exception thrown when file import parsing fails.
 */
public class ImportParseException extends RuntimeException {

    private final String fileType;
    private final String detail;

    public ImportParseException(String fileType, String detail) {
        super("Failed to parse " + fileType + " file: " + detail);
        this.fileType = fileType;
        this.detail = detail;
    }

    public ImportParseException(String fileType, String detail, Throwable cause) {
        super("Failed to parse " + fileType + " file: " + detail, cause);
        this.fileType = fileType;
        this.detail = detail;
    }

    public String getFileType() {
        return fileType;
    }

    public String getDetail() {
        return detail;
    }
}

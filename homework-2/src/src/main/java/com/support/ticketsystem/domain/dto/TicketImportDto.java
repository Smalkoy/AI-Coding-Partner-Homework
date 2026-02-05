package com.support.ticketsystem.domain.dto;

import java.util.List;

/**
 * DTO for ticket import from CSV/JSON/XML files.
 * All fields are strings initially for flexible parsing, then validated and converted.
 */
public class TicketImportDto {

    private String customerId;
    private String customerEmail;
    private String customerName;
    private String subject;
    private String description;
    private String category;
    private String priority;
    private String status;
    private String assignedTo;
    private List<String> tags;
    private String metadataSource;
    private String metadataBrowser;
    private String metadataDeviceType;

    public TicketImportDto() {
    }

    // Getters and Setters
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getMetadataSource() {
        return metadataSource;
    }

    public void setMetadataSource(String metadataSource) {
        this.metadataSource = metadataSource;
    }

    public String getMetadataBrowser() {
        return metadataBrowser;
    }

    public void setMetadataBrowser(String metadataBrowser) {
        this.metadataBrowser = metadataBrowser;
    }

    public String getMetadataDeviceType() {
        return metadataDeviceType;
    }

    public void setMetadataDeviceType(String metadataDeviceType) {
        this.metadataDeviceType = metadataDeviceType;
    }
}

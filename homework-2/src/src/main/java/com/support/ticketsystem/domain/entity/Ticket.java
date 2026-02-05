package com.support.ticketsystem.domain.entity;

import com.support.ticketsystem.config.StringListConverter;
import com.support.ticketsystem.domain.enums.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a customer support ticket.
 */
@Entity
@Table(name = "tickets", indexes = {
    @Index(name = "idx_tickets_status", columnList = "status"),
    @Index(name = "idx_tickets_category", columnList = "category"),
    @Index(name = "idx_tickets_priority", columnList = "priority"),
    @Index(name = "idx_tickets_customer_id", columnList = "customer_id"),
    @Index(name = "idx_tickets_created_at", columnList = "created_at")
})
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    @Column(name = "customer_email", nullable = false, length = 255)
    private String customerEmail;

    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;

    @Column(name = "subject", nullable = false, length = 200)
    private String subject;

    @Column(name = "description", nullable = false, length = 5000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50)
    private Category category = Category.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private Status status = Status.NEW;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Convert(converter = StringListConverter.class)
    @Column(name = "tags", length = 2000)
    private List<String> tags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "metadata_source", length = 20)
    private Source metadataSource;

    @Column(name = "metadata_browser", length = 100)
    private String metadataBrowser;

    @Enumerated(EnumType.STRING)
    @Column(name = "metadata_device_type", length = 20)
    private DeviceType metadataDeviceType;

    /**
     * Default constructor for JPA.
     */
    public Ticket() {
    }

    /**
     * Private constructor for builder pattern.
     */
    private Ticket(Builder builder) {
        this.customerId = builder.customerId;
        this.customerEmail = builder.customerEmail;
        this.customerName = builder.customerName;
        this.subject = builder.subject;
        this.description = builder.description;
        this.category = builder.category;
        this.priority = builder.priority;
        this.status = builder.status;
        this.assignedTo = builder.assignedTo;
        this.tags = builder.tags != null ? builder.tags : new ArrayList<>();
        this.metadataSource = builder.metadataSource;
        this.metadataBrowser = builder.metadataBrowser;
        this.metadataDeviceType = builder.metadataDeviceType;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getSubject() {
        return subject;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public Priority getPriority() {
        return priority;
    }

    public Status getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getResolvedAt() {
        return resolvedAt;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public List<String> getTags() {
        return tags;
    }

    public Source getMetadataSource() {
        return metadataSource;
    }

    public String getMetadataBrowser() {
        return metadataBrowser;
    }

    public DeviceType getMetadataDeviceType() {
        return metadataDeviceType;
    }

    // Setters
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setResolvedAt(OffsetDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    public void setMetadataSource(Source metadataSource) {
        this.metadataSource = metadataSource;
    }

    public void setMetadataBrowser(String metadataBrowser) {
        this.metadataBrowser = metadataBrowser;
    }

    public void setMetadataDeviceType(DeviceType metadataDeviceType) {
        this.metadataDeviceType = metadataDeviceType;
    }

    /**
     * Creates a new builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for Ticket entity.
     */
    public static class Builder {
        private String customerId;
        private String customerEmail;
        private String customerName;
        private String subject;
        private String description;
        private Category category = Category.OTHER;
        private Priority priority = Priority.MEDIUM;
        private Status status = Status.NEW;
        private String assignedTo;
        private List<String> tags = new ArrayList<>();
        private Source metadataSource;
        private String metadataBrowser;
        private DeviceType metadataDeviceType;

        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder customerEmail(String customerEmail) {
            this.customerEmail = customerEmail;
            return this;
        }

        public Builder customerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder category(Category category) {
            this.category = category != null ? category : Category.OTHER;
            return this;
        }

        public Builder priority(Priority priority) {
            this.priority = priority != null ? priority : Priority.MEDIUM;
            return this;
        }

        public Builder status(Status status) {
            this.status = status != null ? status : Status.NEW;
            return this;
        }

        public Builder assignedTo(String assignedTo) {
            this.assignedTo = assignedTo;
            return this;
        }

        public Builder tags(List<String> tags) {
            this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
            return this;
        }

        public Builder metadataSource(Source source) {
            this.metadataSource = source;
            return this;
        }

        public Builder metadataBrowser(String browser) {
            this.metadataBrowser = browser;
            return this;
        }

        public Builder metadataDeviceType(DeviceType deviceType) {
            this.metadataDeviceType = deviceType;
            return this;
        }

        public Ticket build() {
            return new Ticket(this);
        }
    }
}

package com.civictransparency.evidence.entity;

import com.civictransparency.complaint.entity.Complaint;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "evidence", schema = "evidence")
public class Evidence {

    @Id
    @Column(name = "evidence_id", updatable = false, nullable = false)
    private UUID evidenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false, updatable = false)
    private Complaint complaint;

    @Column(name = "storage_key", unique = true, nullable = false, length = 500)
    private String storageKey;

    @Column(name = "media_type", nullable = false, length = 100)
    private String mediaType;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "sha256", nullable = false, columnDefinition = "CHAR(64)")
    private String sha256;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "processing_status", nullable = false, length = 30)
    private String processingStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Evidence() {
    }

    public Evidence(UUID evidenceId, Complaint complaint, String storageKey, String mediaType, Long fileSizeBytes, String sha256, Integer durationSeconds, String processingStatus) {
        this.evidenceId = evidenceId;
        this.complaint = complaint;
        this.storageKey = storageKey;
        this.mediaType = mediaType;
        this.fileSizeBytes = fileSizeBytes;
        this.sha256 = sha256;
        this.durationSeconds = durationSeconds;
        this.processingStatus = processingStatus;
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getEvidenceId() { return evidenceId; }
    public Complaint getComplaint() { return complaint; }
    public String getStorageKey() { return storageKey; }
    public String getMediaType() { return mediaType; }
    public Long getFileSizeBytes() { return fileSizeBytes; }
    public String getSha256() { return sha256; }
    public Integer getDurationSeconds() { return durationSeconds; }
    public String getProcessingStatus() { return processingStatus; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}

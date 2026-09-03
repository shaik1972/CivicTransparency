package com.civictransparency.complaint.service;

import com.civictransparency.complaint.dto.ComplaintSubmissionRequest;
import java.util.UUID;

public interface ComplaintSubmissionService {
    UUID submitComplaint(ComplaintSubmissionRequest request);
}

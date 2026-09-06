package com.civictransparency.complaint.service;

import com.civictransparency.citizen.entity.CitizenProfile;
import com.civictransparency.citizen.repository.CitizenProfileRepository;
import com.civictransparency.complaint.dto.ComplaintSubmissionRequest;
import com.civictransparency.complaint.entity.Complaint;
import com.civictransparency.complaint.entity.ComplaintCategory;
import com.civictransparency.complaint.entity.ComplaintSubcategory;
import com.civictransparency.complaint.entity.Severity;
import com.civictransparency.complaint.repository.ComplaintCategoryRepository;
import com.civictransparency.complaint.repository.ComplaintRepository;
import com.civictransparency.complaint.repository.ComplaintSubcategoryRepository;
import com.civictransparency.evidence.entity.Evidence;
import com.civictransparency.evidence.repository.EvidenceRepository;
import com.civictransparency.geography.entity.City;
import com.civictransparency.geography.entity.Ward;
import com.civictransparency.geography.repository.CityRepository;
import com.civictransparency.geography.repository.WardRepository;
import com.civictransparency.location.entity.ComplaintLocation;
import com.civictransparency.location.entity.ExposurePolicy;
import com.civictransparency.location.repository.ComplaintLocationRepository;
import com.civictransparency.audit.service.AuditService;
import com.civictransparency.routing.service.RoutingEngineService;
import com.civictransparency.workflow.entity.ComplaintState;
import com.civictransparency.workflow.entity.Workflow;
import com.civictransparency.workflow.repository.WorkflowRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ComplaintSubmissionServiceImpl implements ComplaintSubmissionService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintLocationRepository locationRepository;
    private final WorkflowRepository workflowRepository;
    private final EvidenceRepository evidenceRepository;
    private final CitizenProfileRepository citizenRepository;
    private final ComplaintCategoryRepository categoryRepository;
    private final ComplaintSubcategoryRepository subcategoryRepository;
    private final CityRepository cityRepository;
    private final WardRepository wardRepository;
    private final RoutingEngineService routingEngineService;
    private final AuditService auditService;
    private final GeometryFactory geometryFactory;

    public ComplaintSubmissionServiceImpl(
            ComplaintRepository complaintRepository,
            ComplaintLocationRepository locationRepository,
            WorkflowRepository workflowRepository,
            EvidenceRepository evidenceRepository,
            CitizenProfileRepository citizenRepository,
            ComplaintCategoryRepository categoryRepository,
            ComplaintSubcategoryRepository subcategoryRepository,
            CityRepository cityRepository,
            WardRepository wardRepository,
            RoutingEngineService routingEngineService,
            AuditService auditService) {
        this.complaintRepository = complaintRepository;
        this.locationRepository = locationRepository;
        this.workflowRepository = workflowRepository;
        this.evidenceRepository = evidenceRepository;
        this.citizenRepository = citizenRepository;
        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
        this.cityRepository = cityRepository;
        this.wardRepository = wardRepository;
        this.routingEngineService = routingEngineService;
        this.auditService = auditService;
        this.geometryFactory = new GeometryFactory();
    }

    @Override
    public UUID submitComplaint(ComplaintSubmissionRequest request) {

        CitizenProfile citizen = citizenRepository.findById(request.citizenId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid citizen ID"));
        ComplaintCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));
        ComplaintSubcategory subcategory = request.subcategoryId() != null 
                ? subcategoryRepository.findById(request.subcategoryId()).orElse(null) 
                : null;
        City city = cityRepository.findById(request.cityId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid city ID"));
        Ward ward = request.wardId() != null 
                ? wardRepository.findById(request.wardId()).orElse(null) 
                : null;

        String reference = "CMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String title = request.title() != null && !request.title().isBlank() 
                ? request.title() 
                : (subcategory != null ? subcategory.getName() : category.getName());
        
        Severity severity = Severity.MEDIUM;
        if (subcategory != null) {
            severity = Severity.valueOf(subcategory.getDefaultPriority());
        }

        Complaint complaint = new Complaint(
                UUID.randomUUID(),
                reference,
                citizen,
                category,
                subcategory,
                title,
                request.description(),
                severity,
                city,
                ward
        );
        complaint = complaintRepository.save(complaint);

        if (request.latitude() != null && request.longitude() != null) {
            Point point = geometryFactory.createPoint(new Coordinate(
                    request.longitude().doubleValue(),
                    request.latitude().doubleValue()
            ));
            point.setSRID(4326);
            
            ComplaintLocation location = new ComplaintLocation(
                    UUID.randomUUID(),
                    complaint,
                    point,
                    request.accuracyMeters(),
                    request.locationDescription(),
                    ExposurePolicy.WARD,
                    null
            );
            locationRepository.save(location);
        }

        Workflow workflow = new Workflow(
                UUID.randomUUID(),
                complaint,
                ComplaintState.SUBMITTED
        );
        workflowRepository.save(workflow);

        if (request.evidence() != null && !request.evidence().isEmpty()) {
            for (ComplaintSubmissionRequest.EvidenceSubmission evReq : request.evidence()) {
                Evidence evidence = new Evidence(
                        UUID.randomUUID(),
                        complaint,
                        evReq.storageKey(),
                        evReq.mediaType(),
                        evReq.fileSizeBytes(),
                        evReq.sha256(),
                        null,
                        "PENDING"
                );
                evidenceRepository.save(evidence);
            }
        }

        routingEngineService.routeComplaint(complaint);

        auditService.log(
            "COMPLAINT_SUBMITTED",
            null,
            complaint.getComplaintId(),
            "Complaint submitted with reference " + complaint.getComplaintReference()
        );

        return complaint.getComplaintId();
    }
}

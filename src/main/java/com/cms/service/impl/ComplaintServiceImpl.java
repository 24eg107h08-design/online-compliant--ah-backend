package com.cms.service.impl;

import com.cms.dto.request.ComplaintCreateRequest;
import com.cms.dto.request.ComplaintUpdateRequest;
import com.cms.dto.request.StatusUpdateRequest;
import com.cms.dto.response.ComplaintHistoryResponse;
import com.cms.dto.response.ComplaintResponse;
import com.cms.dto.response.DashboardStatsResponse;
import com.cms.entity.*;
import com.cms.exception.BadRequestException;
import com.cms.exception.ResourceNotFoundException;
import com.cms.exception.UnauthorizedException;
import com.cms.repository.ComplaintHistoryRepository;
import com.cms.repository.ComplaintRepository;
import com.cms.repository.UserRepository;
import com.cms.service.AuthService;
import com.cms.service.ComplaintService;
import com.cms.service.FileStorageService;
import com.cms.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintHistoryRepository complaintHistoryRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;
    private final Random random = new Random();

    @Autowired
    public ComplaintServiceImpl(ComplaintRepository complaintRepository,
                                ComplaintHistoryRepository complaintHistoryRepository,
                                UserRepository userRepository,
                                AuthService authService,
                                FileStorageService fileStorageService,
                                NotificationService notificationService) {
        this.complaintRepository = complaintRepository;
        this.complaintHistoryRepository = complaintHistoryRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    private String generateUniqueComplaintCode() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        String code;
        do {
            StringBuilder sb = new StringBuilder("CMP-");
            for (int i = 0; i < 4; i++) {
                sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
            }
            sb.append("-");
            for (int i = 0; i < 4; i++) {
                sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
            }
            code = sb.toString();
        } while (complaintRepository.existsByComplaintCode(code));
        return code;
    }

    @Override
    @Transactional
    public ComplaintResponse createComplaint(ComplaintCreateRequest request, MultipartFile file) {
        User currentUser = authService.getCurrentAuthenticatedUser();

        Complaint complaint = new Complaint();
        complaint.setComplaintCode(generateUniqueComplaintCode());
        complaint.setTitle(request.getTitle().trim());
        complaint.setCategory(request.getCategory().trim());
        complaint.setDescription(request.getDescription().trim());
        complaint.setLocation(request.getLocation().trim());
        complaint.setStatus(Status.SUBMITTED);
        complaint.setUser(currentUser);

        // Handle attachment upload if present
        if (file != null && !file.isEmpty()) {
            String storedFileName = fileStorageService.storeFile(file);
            complaint.setAttachmentPath(storedFileName);
            complaint.setAttachmentName(file.getOriginalFilename());
        }

        Complaint savedComplaint = complaintRepository.save(complaint);

        // Record initial history entry
        ComplaintHistory history = new ComplaintHistory(
                savedComplaint,
                null,
                Status.SUBMITTED,
                "Complaint lodged successfully and pending review.",
                currentUser.getName()
        );
        complaintHistoryRepository.save(history);

        // Trigger notification
        notificationService.createNotification(
                currentUser,
                savedComplaint,
                "Your complaint '" + savedComplaint.getTitle() + "' (" + savedComplaint.getComplaintCode() + ") has been submitted successfully."
        );

        return mapToComplaintResponse(savedComplaint);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComplaintResponse> getUserComplaints(String statusStr, String category, String search, int page, int size, String sortBy, String sortDir) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Status status = parseStatus(statusStr);
        String cleanCategory = StringUtils.hasText(category) ? category.trim() : null;
        String cleanSearch = StringUtils.hasText(search) ? search.trim() : null;

        Page<Complaint> complaints = complaintRepository.findUserComplaintsWithFilters(
                currentUser.getId(), status, cleanCategory, cleanSearch, pageable
        );

        return complaints.map(this::mapToComplaintResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", id));

        User currentUser = authService.getCurrentAuthenticatedUser();
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;

        if (!isAdmin && !complaint.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You do not have permission to view this complaint.");
        }

        return mapToComplaintResponse(complaint);
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintByCode(String complaintCode) {
        Complaint complaint = complaintRepository.findByComplaintCode(complaintCode.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "complaintCode", complaintCode));

        return mapToComplaintResponse(complaint);
    }

    @Override
    @Transactional
    public ComplaintResponse updateComplaint(Long id, ComplaintUpdateRequest request) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", id));

        User currentUser = authService.getCurrentAuthenticatedUser();
        if (!complaint.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You do not have permission to modify this complaint.");
        }

        if (complaint.getStatus() != Status.SUBMITTED) {
            throw new BadRequestException("Complaints can only be edited while in SUBMITTED status.");
        }

        complaint.setTitle(request.getTitle().trim());
        complaint.setCategory(request.getCategory().trim());
        complaint.setDescription(request.getDescription().trim());
        complaint.setLocation(request.getLocation().trim());

        Complaint updated = complaintRepository.save(complaint);
        return mapToComplaintResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComplaintResponse> getAllComplaints(String statusStr, String category, String search, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Status status = parseStatus(statusStr);
        String cleanCategory = StringUtils.hasText(category) ? category.trim() : null;
        String cleanSearch = StringUtils.hasText(search) ? search.trim() : null;

        Page<Complaint> complaints = complaintRepository.findWithFilters(status, cleanCategory, cleanSearch, pageable);
        return complaints.map(this::mapToComplaintResponse);
    }

    @Override
    @Transactional
    public ComplaintResponse updateStatus(Long id, StatusUpdateRequest request) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", id));

        User adminUser = authService.getCurrentAuthenticatedUser();
        Status previousStatus = complaint.getStatus();
        Status newStatus;

        try {
            newStatus = Status.valueOf(request.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid status value: " + request.getStatus() +
                    ". Must be SUBMITTED, IN_PROGRESS, RESOLVED, or REJECTED.");
        }

        complaint.setStatus(newStatus);

        if (StringUtils.hasText(request.getAssignedDepartment())) {
            complaint.setAssignedDepartment(request.getAssignedDepartment().trim());
        }
        if (StringUtils.hasText(request.getAssignedStaff())) {
            complaint.setAssignedStaff(request.getAssignedStaff().trim());
        }
        if (StringUtils.hasText(request.getRemarks())) {
            complaint.setResolutionRemarks(request.getRemarks().trim());
        }

        Complaint savedComplaint = complaintRepository.save(complaint);

        // Record history event
        String remarks = StringUtils.hasText(request.getRemarks()) ? request.getRemarks().trim() : "Status updated to " + newStatus;
        ComplaintHistory history = new ComplaintHistory(
                savedComplaint,
                previousStatus,
                newStatus,
                remarks,
                adminUser.getName() + " (Admin)"
        );
        complaintHistoryRepository.save(history);

        // Notify user about status change
        String notificationMsg = String.format("Status of complaint %s changed to %s. Remark: %s",
                savedComplaint.getComplaintCode(), newStatus, remarks);
        notificationService.createNotification(savedComplaint.getUser(), savedComplaint, notificationMsg);

        return mapToComplaintResponse(savedComplaint);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getUserStats() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        long userId = currentUser.getId();

        long total = complaintRepository.countByUserId(userId);
        long pending = complaintRepository.countByUserIdAndStatus(userId, Status.SUBMITTED);
        long inProgress = complaintRepository.countByUserIdAndStatus(userId, Status.IN_PROGRESS);
        long resolved = complaintRepository.countByUserIdAndStatus(userId, Status.RESOLVED);
        long rejected = complaintRepository.countByUserIdAndStatus(userId, Status.REJECTED);

        return new DashboardStatsResponse(total, pending, inProgress, resolved, rejected, 1);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getAdminStats() {
        long total = complaintRepository.count();
        long pending = complaintRepository.countByStatus(Status.SUBMITTED);
        long inProgress = complaintRepository.countByStatus(Status.IN_PROGRESS);
        long resolved = complaintRepository.countByStatus(Status.RESOLVED);
        long rejected = complaintRepository.countByStatus(Status.REJECTED);
        long totalUsers = userRepository.count();

        return new DashboardStatsResponse(total, pending, inProgress, resolved, rejected, totalUsers);
    }

    private Status parseStatus(String statusStr) {
        if (!StringUtils.hasText(statusStr) || statusStr.equalsIgnoreCase("ALL")) {
            return null;
        }
        try {
            return Status.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private ComplaintResponse mapToComplaintResponse(Complaint complaint) {
        ComplaintResponse response = new ComplaintResponse();
        response.setId(complaint.getId());
        response.setComplaintCode(complaint.getComplaintCode());
        response.setTitle(complaint.getTitle());
        response.setCategory(complaint.getCategory());
        response.setDescription(complaint.getDescription());
        response.setLocation(complaint.getLocation());
        response.setAttachmentPath(complaint.getAttachmentPath());
        response.setAttachmentName(complaint.getAttachmentName());
        response.setStatus(complaint.getStatus().name());
        response.setAssignedDepartment(complaint.getAssignedDepartment());
        response.setAssignedStaff(complaint.getAssignedStaff());
        response.setResolutionRemarks(complaint.getResolutionRemarks());
        response.setCreatedAt(complaint.getCreatedAt());
        response.setUpdatedAt(complaint.getUpdatedAt());

        if (complaint.getUser() != null) {
            response.setUserId(complaint.getUser().getId());
            response.setUserName(complaint.getUser().getName());
            response.setUserEmail(complaint.getUser().getEmail());
            response.setUserPhone(complaint.getUser().getPhone());
        }

        List<ComplaintHistory> historyList = complaintHistoryRepository.findByComplaintIdOrderByCreatedAtDesc(complaint.getId());
        if (historyList != null) {
            List<ComplaintHistoryResponse> historyResponses = historyList.stream()
                    .map(h -> new ComplaintHistoryResponse(
                            h.getId(),
                            h.getPreviousStatus() != null ? h.getPreviousStatus().name() : null,
                            h.getNewStatus() != null ? h.getNewStatus().name() : null,
                            h.getRemarks(),
                            h.getUpdatedBy(),
                            h.getCreatedAt()
                    ))
                    .collect(Collectors.toList());
            response.setHistory(historyResponses);
        } else {
            response.setHistory(Collections.emptyList());
        }

        return response;
    }

    @Override
    @Transactional
    public List<ComplaintResponse> generateRandomComplaints(int count) {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new BadRequestException("No users found to associate random complaints with.");
        }

        // Exclude pure admin if regular citizens exist
        List<User> citizenUsers = users.stream()
                .filter(u -> u.getRole() == Role.ROLE_USER)
                .toList();
        if (citizenUsers.isEmpty()) {
            citizenUsers = users;
        }

        String[][] electricityPool = {
                {"Frequent voltage fluctuation damaging domestic appliances", "Voltage spikes observed daily between 7 PM and 10 PM in residential buildings."},
                {"Defective street lights causing total blackout on main avenue", "Over 8 streetlight poles non-functional, causing acute pedestrian safety risks."},
                {"Exposed live electrical wire hanging from pole", "Hazardous dangling wire close to public sidewalk, urgent repair requested."},
                {"Distribution transformer oil leak and sparking", "Repeated sparks and loud hum observed from local pole-mounted transformer."}
        };

        String[][] waterPool = {
                {"Muddy and contaminated tap water supplied for 3 days", "Contaminated tap water unfit for cooking or consumption, over 50 homes affected."},
                {"Major drinking water pipeline burst flooding roadway", "Underground main water line fractured, wasting thousands of liters of clean water."},
                {"Zero municipal water supply for consecutive mornings", "Scheduled morning municipal water supply not received for past 4 days."},
                {"Sewage backflow mixing into underground water tank", "Foul drain water leaking into domestic water sump due to main sewer line blockage."}
        };

        String[][] roadsPool = {
                {"Severe crater-like potholes causing two-wheeler skids", "Deep potholes spanning entire road width near bus stop, highly dangerous after rain."},
                {"Non-functional traffic signals causing severe junction gridlock", "Traffic signals blinking red at busy 4-way crossroad since yesterday."},
                {"Road cave-in following underground utility work", "Asphalt surface depressed by 2 feet, posing major vehicular hazard."},
                {"Missing cast-iron manhole cover on walking sidewalk", "Open manhole on pedestrian path without warning signage."}
        };

        String[][] sanitationPool = {
                {"Garbage collection truck absent for over a week", "Overflowing municipal waste dumpsters emitting foul odors into nearby market."},
                {"Clogged stormwater drain overflowing onto pedestrian pavement", "Black stagnant wastewater pooling on street due to silt and plastic blockage."},
                {"Illegal open dumping of commercial construction debris", "Large heaps of concrete waste and debris dumped along vacant public plot."},
                {"Stagnant water cesspool promoting intense mosquito breeding", "Waterlogged vacant land causing acute dengue and malaria risk for residents."}
        };

        String[][] educationPool = {
                {"Broken window panes and damaged benches in primary school", "Municipal primary school classroom lacks adequate desks and safety glass."},
                {"Non-functional drinking water filtration unit in school", "Students unable to access clean potable water on campus during school hours."},
                {"Cracked boundary wall posing safety risk to school students", "School compound wall tilted outward, risk of collapse during rains."}
        };

        String[][] otherPool = {
                {"Stray cattle herd obstructing vehicular traffic on main bypass", "Over a dozen stray animals causing recurrent traffic jams and near-misses."},
                {"Unauthorized commercial shed encroaching on public footpath", "Footpath completely blocked by illegal tin shed extension."},
                {"Dangerous tree branches touching overhead high-tension line", "Heavy tree branches swaying dangerously close to power cables during wind."}
        };

        String[] locations = {
                "Ward 4, 2nd Main Road, Near Metro Pillar 124",
                "Sector 14, Opposite Government Civil Hospital",
                "Block D, Green Park Housing Society, 5th Cross",
                "Market Road, Adjacent to Central Bus Terminal",
                "Ward 22, Ring Road Junction, Near Flyover",
                "Industrial Area Phase 2, Near Gate 3",
                "MG Road, Opposite Municipal Public Library",
                "Civil Lines, Near Government Girls High School",
                "Lake View Colony, 7th Cross Street",
                "Railway Colony, Near Overbridge Junction",
                "Sector 9, Outer Ring Road Service Lane",
                "Subhash Nagar, Main Commercial Boulevard"
        };

        String[] staffMembers = {
                "Engineer Ramesh Kumar",
                "Officer Suresh Patel",
                "Inspector Vikram Singh",
                "Officer Meena Rao",
                "Assistant Engineer Deepa Nair",
                "Superintendent Rajesh Verma",
                "Supervisor Anil Deshmukh"
        };

        List<ComplaintResponse> createdList = new ArrayList<>();
        Status[] statuses = {Status.SUBMITTED, Status.IN_PROGRESS, Status.RESOLVED, Status.REJECTED};

        for (int i = 0; i < count; i++) {
            int catIdx = random.nextInt(6);
            String category;
            String[][] pool;
            String department;

            switch (catIdx) {
                case 0 -> { category = "Electricity"; pool = electricityPool; department = "Electricity Board"; }
                case 1 -> { category = "Water"; pool = waterPool; department = "Water Supply & Sewerage Board"; }
                case 2 -> { category = "Roads"; pool = roadsPool; department = "Public Works & Roads Department"; }
                case 3 -> { category = "Sanitation"; pool = sanitationPool; department = "Sanitation & Waste Management"; }
                case 4 -> { category = "Education"; pool = educationPool; department = "Municipal Education Department"; }
                default -> { category = "Other"; pool = otherPool; department = "Town Planning & Civic Administration"; }
            }

            String[] issue = pool[random.nextInt(pool.length)];
            String title = issue[0];
            String description = issue[1];
            String location = locations[random.nextInt(locations.length)];
            User citizen = citizenUsers.get(random.nextInt(citizenUsers.size()));

            // Pick status with weighted probability
            int randStatus = random.nextInt(10);
            Status status;
            if (randStatus < 3) status = Status.SUBMITTED;
            else if (randStatus < 6) status = Status.IN_PROGRESS;
            else if (randStatus < 9) status = Status.RESOLVED;
            else status = Status.REJECTED;

            int daysAgo = 1 + random.nextInt(25);
            LocalDateTime createdAt = LocalDateTime.now().minusDays(daysAgo).minusHours(random.nextInt(24));
            LocalDateTime updatedAt = createdAt.plusHours(2 + random.nextInt(48));

            Complaint complaint = new Complaint();
            complaint.setComplaintCode(generateUniqueComplaintCode());
            complaint.setTitle(title);
            complaint.setCategory(category);
            complaint.setDescription(description);
            complaint.setLocation(location);
            complaint.setUser(citizen);
            complaint.setStatus(status);
            complaint.setCreatedAt(createdAt);
            complaint.setUpdatedAt(updatedAt);

            if (status != Status.SUBMITTED) {
                complaint.setAssignedDepartment(department);
                complaint.setAssignedStaff(staffMembers[random.nextInt(staffMembers.length)]);
            }

            if (status == Status.RESOLVED) {
                complaint.setResolutionRemarks("Issue inspected and completely rectified on-site. Verified by department supervisor.");
            } else if (status == Status.REJECTED) {
                complaint.setResolutionRemarks("Complaint closed: Verification confirmed this location has active approvals or falls outside municipal jurisdiction.");
            } else if (status == Status.IN_PROGRESS) {
                complaint.setResolutionRemarks("Field inspection in progress. Work order dispatched to response team.");
            }

            Complaint saved = complaintRepository.save(complaint);

            // Create initial history
            ComplaintHistory h1 = new ComplaintHistory(saved, null, Status.SUBMITTED, "Complaint registered by citizen", citizen.getName());
            h1.setCreatedAt(createdAt);
            complaintHistoryRepository.save(h1);

            // If progressed, add second history
            if (status != Status.SUBMITTED) {
                ComplaintHistory h2 = new ComplaintHistory(
                        saved,
                        Status.SUBMITTED,
                        status,
                        saved.getResolutionRemarks(),
                        "System Administrator (Admin)"
                );
                h2.setCreatedAt(updatedAt);
                complaintHistoryRepository.save(h2);

                notificationService.createNotification(
                        citizen,
                        saved,
                        "Your complaint " + saved.getComplaintCode() + " status is now " + status + "."
                );
            }

            createdList.add(mapToComplaintResponse(saved));
        }

        return createdList;
    }
}

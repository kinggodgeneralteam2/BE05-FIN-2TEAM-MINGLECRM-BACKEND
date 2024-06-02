package com.team2final.minglecrm.service.inquiry;

import com.team2final.minglecrm.controller.inquiry.request.InquiryActionRequest;
import com.team2final.minglecrm.controller.inquiry.request.InquiryReplyRequest;
import com.team2final.minglecrm.controller.inquiry.response.InquiryActionResponse;
import com.team2final.minglecrm.controller.inquiry.response.InquiryDetailResponse;
import com.team2final.minglecrm.controller.inquiry.response.InquiryReplyResponse;
import com.team2final.minglecrm.controller.inquiry.response.InquiryResponse;
import com.team2final.minglecrm.entity.employee.Employee;
import com.team2final.minglecrm.entity.inquiry.ActionStatus;
import com.team2final.minglecrm.entity.inquiry.Inquiry;
import com.team2final.minglecrm.entity.inquiry.InquiryAction;
import com.team2final.minglecrm.entity.inquiry.InquiryReply;
import com.team2final.minglecrm.persistence.repository.employee.EmployeeRepository;
import com.team2final.minglecrm.persistence.repository.inquiry.InquiryActionRepository;
import com.team2final.minglecrm.persistence.repository.inquiry.InquiryReplyRepository;
import com.team2final.minglecrm.persistence.repository.inquiry.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryReplyRepository inquiryReplyRepository;
    private final InquiryActionRepository inquiryActionRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public Page<InquiryResponse> getAllInquiries(Pageable pageable) {
        Page<Inquiry> inquiries = inquiryRepository.findAll(pageable);
        return inquiries.map(inquiry -> {

            Optional<InquiryReply> inquiryReplyOptional = inquiryReplyRepository.findByInquiryId(inquiry.getId());
            InquiryReply inquiryReply = inquiryReplyOptional.orElse(null); // 답변 없으면 null

            Optional<InquiryAction> inquiryActionOptional = inquiryActionRepository.findByInquiryId(inquiry.getId());
            InquiryAction inquiryAction = inquiryActionOptional.orElse(null);

            return convertToDTO(inquiry, inquiryReply, inquiryAction);
        });
    }

    @Transactional
    public Page<InquiryResponse> getUnansweredInquiries(Pageable pageable) {
        Page<Inquiry> unansweredInquiries = inquiryRepository.findUnansweredInquiries(pageable);
        return unansweredInquiries.map(inquiry -> {
            Optional<InquiryAction> inquiryActionOptional = inquiryActionRepository.findByInquiryId(inquiry.getId());
            InquiryAction inquiryAction = inquiryActionOptional.orElse(null);
            return convertToDTO(inquiry, null, inquiryAction); // 답변이 없는 문의만 조회 - inquiryReply는 항상 null
        });
    }

    @Transactional
    public Page<InquiryResponse> getAnsweredInquiries(Pageable pageable) {
        Page<Inquiry> answeredInquiries = inquiryRepository.findInquiriesWithReply(pageable);

        Map<Long, InquiryReply> inquiryReplyMap = inquiryReplyRepository.findAll().stream()
                .collect(Collectors.toMap(ir -> ir.getInquiry().getId(), ir -> ir));

        return answeredInquiries.map(inquiry -> {
            InquiryReply inquiryReply = inquiryReplyMap.get(inquiry.getId());

            Optional<InquiryAction> inquiryActionOptional = inquiryActionRepository.findByInquiryId(inquiry.getId());
            InquiryAction inquiryAction = inquiryActionOptional.orElse(null);

            return convertToDTO(inquiry, inquiryReply, inquiryAction);
        });
    }

    @Transactional
    public Page<InquiryResponse> getInquiriesWithAction(Pageable pageable) {
        Page<Inquiry> inquiriesWithAction = inquiryRepository.findInquiriesWithAction(pageable);
        return inquiriesWithAction.map(inquiry -> {
            Optional<InquiryReply> inquiryReplyOptional = inquiryReplyRepository.findByInquiryId(inquiry.getId());
            InquiryReply inquiryReply = inquiryReplyOptional.orElse(null);

            Optional<InquiryAction> inquiryActionOptional = inquiryActionRepository.findByInquiryId(inquiry.getId());
            InquiryAction inquiryAction = inquiryActionOptional.orElse(null);

            return convertToDTO(inquiry, inquiryReply, inquiryAction);
        });
    }

    @Transactional
    public Page<InquiryResponse> getInquiriesWithoutAction(Pageable pageable) {
        Page<Inquiry> inquiriesWithoutAction = inquiryRepository.findInquiriesWithoutAction(pageable);
        return inquiriesWithoutAction.map(inquiry -> {
            Optional<InquiryReply> inquiryReplyOptional = inquiryReplyRepository.findByInquiryId(inquiry.getId());
            InquiryReply inquiryReply = inquiryReplyOptional.orElse(null);
            return convertToDTO(inquiry, inquiryReply, null);
        });
    }

    @Transactional
    public InquiryDetailResponse getInquiryById(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));

        InquiryReply reply = inquiryReplyRepository.findByInquiryId(inquiryId).orElse(null);
        InquiryAction action = inquiryActionRepository.findByInquiryId(inquiryId).orElse(null);

        InquiryResponse inquiryResponse = convertToDTO(inquiry, reply, action);
        InquiryReplyResponse inquiryReplyResponse = (reply != null) ? convertToDTO(reply) : null;
        InquiryActionResponse inquiryActionResponse = (action != null) ? convertToActionDTO(action) : null;
        // 문의에 답변 존재하는지 확인 -> 없으면 null

        return InquiryDetailResponse.builder()
                .inquiryResponse(inquiryResponse)
                .inquiryReplyResponse(inquiryReplyResponse)
                .inquiryActionResponse(inquiryActionResponse)
                .build();
    }

    @Transactional
    public InquiryReplyResponse replyToInquiry(InquiryReplyRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("로그인한 사용자를 찾을 수 없습니다."));

        Inquiry inquiry = inquiryRepository.findById(request.getInquiryId())
                .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));

        InquiryReply inquiryReply = InquiryReply.builder()
                .inquiry(inquiry)
                .employee(employee)
                .reply(request.getReply())
                .date(LocalDateTime.now())
                .build();

        InquiryReply saveReply = inquiryReplyRepository.save(inquiryReply);

        return convertToDTO(saveReply);
    }

    @Transactional
    public InquiryReplyResponse updateInquiryReply(Long inquiryReplyId, String updatedReply) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("로그인한 사용자를 찾을 수 없습니다."));

        InquiryReply inquiryReply = inquiryReplyRepository.findById(inquiryReplyId)
                .orElseThrow(() -> new RuntimeException("답변을 찾을 수 없습니다."));

        // 엔티티 메서드 호출
        inquiryReply.updateReply(updatedReply, LocalDateTime.now(), employee);

        return convertToDTO(inquiryReply);
    }

    @Transactional
    public InquiryActionResponse actionToInquiry(InquiryActionRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("로그인한 사용자를 찾을 수 없습니다."));

        Inquiry inquiry = inquiryRepository.findById(request.getInquiryId())
                .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));

        InquiryAction inquiryAction = InquiryAction.builder()
                .inquiry(inquiry)
                .employee(employee)
                .actionStatus(request.getActionStatus())
                .actionContent(request.getActionContent())
                .date(LocalDateTime.now())
                .build();

        InquiryAction saveAction = inquiryActionRepository.save(inquiryAction);

        return convertToActionDTO(saveAction);
    }

    @Transactional
    public InquiryActionResponse updateInquiryAction(Long inquiryActionId, String updateAction, ActionStatus actionStatus) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("로그인한 사용자를 찾을 수 없습니다."));

        InquiryAction inquiryAction = inquiryActionRepository.findById(inquiryActionId)
                .orElseThrow(() -> new RuntimeException("조치 내용을 찾을 수 없습니다."));

        inquiryAction.updateAction(updateAction, LocalDateTime.now(), employee, actionStatus);

        return convertToActionDTO(inquiryAction);
    }

    @Transactional
    public Page<InquiryResponse> getInquiriesByCustomerId(Long customerId, Pageable pageable) {
        Page<Inquiry> inquiries = inquiryRepository.findByCustomerId(customerId, pageable);

        return inquiries.map(inquiry -> {
            InquiryReply reply = inquiryReplyRepository.findByInquiryId(inquiry.getId()).orElse(null);
            InquiryAction action = inquiryActionRepository.findByInquiryId(inquiry.getId()).orElse(null);

            return convertToDTO(inquiry, reply, action);
        });
    }

    @Transactional
    public InquiryDetailResponse getInquiryDetailByCustomerId(Long customerId, Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findByIdAndCustomerId(inquiryId, customerId)
                .orElseThrow(() -> new RuntimeException("해당 고객의 문의를 찾을 수 없습니다."));

        InquiryReply reply = inquiryReplyRepository.findByInquiryId(inquiryId).orElse(null);
        InquiryAction action = inquiryActionRepository.findByInquiryId(inquiryId).orElse(null);

        InquiryResponse inquiryResponse = convertToDTO(inquiry, reply, action);
        InquiryReplyResponse inquiryReplyResponse = (reply != null) ? convertToDTO(reply) : null;
        InquiryActionResponse inquiryActionResponse = (action != null) ? convertToActionDTO(action) : null;

        return InquiryDetailResponse.builder()
                .inquiryResponse(inquiryResponse)
                .inquiryReplyResponse(inquiryReplyResponse)
                .inquiryActionResponse(inquiryActionResponse)
                .build();
    }

    @Transactional
    public Page<InquiryResponse> searchInquiries(String keyword, LocalDate startDate, LocalDate endDate, Pageable pageable) {

        LocalDateTime startDateTime = startDate.atStartOfDay(); // LocalDate 객체를 LocalDateTime 객체로 변환
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        Page<Inquiry> inquiries = inquiryRepository.searchByKeyword(keyword, startDateTime, endDateTime, pageable);
        return inquiries.map(inquiry -> {

            InquiryReply reply = inquiryReplyRepository.findByInquiryId(inquiry.getId()).orElse(null);
            InquiryAction action = inquiryActionRepository.findByInquiryId(inquiry.getId()).orElse(null);

            return convertToDTO(inquiry, reply, action);
        });
    }

    private InquiryResponse convertToDTO(Inquiry inquiry, InquiryReply inquiryReply, InquiryAction inquiryAction) {
        String employName = (inquiryReply != null) ? inquiryReply.getEmployee().getName() : null;
        boolean isReply = (inquiryReply != null); // 답변이 있으면 true

        ActionStatus actionStatus = (inquiryAction != null) ? inquiryAction.getActionStatus() : null;


        return InquiryResponse.builder()
                .id(inquiry.getId())
                .customerName(inquiry.getCustomer().getName())
                .customerPhone(inquiry.getCustomer().getPhone())
                .date(inquiry.getDate())
                .type(inquiry.getType())
                .employName(employName)
                .inquiryTitle(inquiry.getInquiryTitle())
                .inquiryContent(inquiry.getInquiryContent())
                .isReply(isReply)
                .actionStatus(actionStatus)
                .build();
    }

    private InquiryReplyResponse convertToDTO(InquiryReply inquiryReply) {
        return InquiryReplyResponse.builder()
                .id(inquiryReply.getId())
                .inquiryId(inquiryReply.getInquiry().getId())
                .email(inquiryReply.getEmployee().getEmail())
                .reply(inquiryReply.getReply())
                .date(inquiryReply.getDate())
                .build();
    }

    private InquiryActionResponse convertToActionDTO(InquiryAction inquiryAction) {
        return InquiryActionResponse.builder()
                .id(inquiryAction.getId())
                .inquiryId(inquiryAction.getInquiry().getId())
                .actionContent(inquiryAction.getActionContent())
                .actionStatus(inquiryAction.getActionStatus())
                .email(inquiryAction.getEmployee().getEmail())
                .date(inquiryAction.getDate())
                .build();
    }
}

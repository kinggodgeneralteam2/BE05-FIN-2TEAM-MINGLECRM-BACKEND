package com.team2final.minglecrm.service.event;

import com.team2final.minglecrm.controller.event.request.CreateEventRequest;
import com.team2final.minglecrm.controller.event.request.EventEmailSendRequest;
import com.team2final.minglecrm.controller.event.request.PersonalEmailSendRequest;
import com.team2final.minglecrm.controller.event.request.ToEmailRequest;
import com.team2final.minglecrm.controller.event.response.EmailLogResponse;
import com.team2final.minglecrm.controller.event.response.EventLogResponse;
import com.team2final.minglecrm.controller.hotel.review.response.HotelReviewConditionSearchResponse;
import com.team2final.minglecrm.entity.customer.Customer;
import com.team2final.minglecrm.entity.employee.Employee;
import com.team2final.minglecrm.entity.event.Event;
import com.team2final.minglecrm.entity.log.EmailLog;
import com.team2final.minglecrm.persistence.repository.customer.CustomerRepository;
import com.team2final.minglecrm.persistence.repository.employee.EmployeeRepository;
import com.team2final.minglecrm.persistence.repository.event.EventRepository;
import com.team2final.minglecrm.persistence.repository.event.queryDsl.EventRespositoryCustom;
import com.team2final.minglecrm.persistence.repository.log.EmailLogRepository;
import com.team2final.minglecrm.persistence.repository.log.queryDsl.EmailLogRepositoryCustom;
import com.team2final.minglecrm.service.email.EmailSendService;
import com.team2final.minglecrm.service.log.LogService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EmailSendService emailSendService;
    private final EventRepository eventRepository;
    private final EmployeeRepository employeeRepository;
    private final EmailLogRepository emailLogRepository;
    private final CustomerRepository customerRepository;
    private final LogService logService;
    private final EventRespositoryCustom eventRespositoryCustom;
    private final EmailLogRepositoryCustom emailLogRepositoryCustom;

    public void sendEventEmail(EventEmailSendRequest request) throws Exception {

        // 요청한 이메일 주소를 통해 직원 정보를 조회합니다.
        Employee employee = employeeRepository.findByEmail(request.getFromEmail())
                .orElseThrow(() -> new Exception("Employee not found"));

        // 이벤트 객체를 생성합니다.
        Event event = Event.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .employee(employee)
                .sentDate(LocalDateTime.now())
                .build();

        // 이벤트 객체를 데이터베이스에 저장합니다.
        Event savedEvent = eventRepository.save(event);

        // 저장된 이벤트 객체의 ID가 null이 아닌지 확인합니다.
        if (savedEvent.getId() == null) {
            throw new Exception("Event ID is null after saving");
        }

        // 이벤트가 성공적으로 저장된 후, 각 수신자에게 이메일을 보냅니다.
        for(ToEmailRequest toEmailRequest : request.getToEmail()) {
            String toEmail = toEmailRequest.getEmail();
            emailSendService.sendMail(toEmail, request.getTitle(), request.getContent());

            // 이메일 로그를 생성합니다.
            logService.createEmailLog(savedEvent.getId(), toEmail);
        }
    }

    @Transactional
    public Long createEvent(CreateEventRequest request) {

        Employee employee = employeeRepository.findByEmail(request.getEmployeeEmail()).orElseThrow( () -> new IllegalArgumentException("없는 직원 입니다."));

        Event event = Event.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .employee(employee)
                .sentDate(LocalDateTime.now())
                .sendCount(request.getSendCount())
                .build();

        Event savedEvent = eventRepository.save(event);
        return savedEvent.getId();
    }

    @Transactional
    public LocalDateTime emailOpenCheck(Long eventId, String customerEmail) throws Exception {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(Exception::new);

        Customer customer = customerRepository
                .findByEmail(customerEmail).orElseThrow(Exception::new);

        EmailLog emailLog = emailLogRepository.findByEventAndCustomer(event, customer);
        LocalDateTime clickedTime = emailLog.open();
        return clickedTime;
    }

    @Transactional
    public List<EventLogResponse> getAllEvents(int pageNo) {

        Page<EventLogResponse> page =  eventRespositoryCustom.findAll(PageRequest.of(pageNo, 50));
        List<EventLogResponse> response = new ArrayList<>();

        for(EventLogResponse eventLogResponse : page.getContent() ) {
            response.add(eventLogResponse);
        }

        return response;
    }

    @Transactional
    public List<EmailLogResponse> getEmailLogsByEventId(int pageNo, Long eventId) {
        Page<EmailLogResponse> page = emailLogRepositoryCustom.findByEventId(PageRequest.of(pageNo, 50), eventId);
        List<EmailLogResponse> response = new ArrayList<>();

        for (EmailLogResponse emailLogResponse : page.getContent()) {
            response.add(emailLogResponse);
        }

        return response;
    }

    @Transactional
    public Long getPagesNumber() {
        return eventRepository.count();
    }
}

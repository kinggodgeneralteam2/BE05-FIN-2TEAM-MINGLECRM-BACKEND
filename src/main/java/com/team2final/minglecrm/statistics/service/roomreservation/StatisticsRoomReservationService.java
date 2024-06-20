package com.team2final.minglecrm.statistics.service.roomreservation;

import com.team2final.minglecrm.statistics.controller.reservation.response.StatisticsRoomReservationResponse;
import com.team2final.minglecrm.entity.hotel.RoomReservation;
import com.team2final.minglecrm.statistics.persistance.StatisticsRoomReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsRoomReservationService {

    private final StatisticsRoomReservationRepository statisticsRoomReservationRepository;


}

package com.team2final.minglecrm.review.service.hotel;

import com.team2final.minglecrm.review.dto.hotel.request.HotelReviewConditionSearchRequest;
import com.team2final.minglecrm.review.dto.hotel.response.HotelReviewConditionSearchResponse;
import com.team2final.minglecrm.review.domain.hotel.HotelReview;
import com.team2final.minglecrm.review.domain.hotel.repository.HotelReviewRepository;
import com.team2final.minglecrm.review.domain.hotel.repository.queryDsl.HotelReviewRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelReviewService {

    private final HotelReviewRepository hotelReviewRepository;
    private final HotelReviewRepositoryCustom hotelReviewRepositoryCustom;

//    public List<HotelReviewResponse> findAllHotelReviewsWithPaging(int pageNo) {
//        Page<HotelReview> hotelReviewPage = hotelReviewRepository.findAll(PageRequest.of(pageNo, 9));
//
//
//        List<HotelReviewResponse> response = new ArrayList<>();
//
//        for(HotelReview hotelReview: hotelReviewPage.getContent()) {
//            response.add(HotelReviewResponse.of(hotelReview));
//        }
//        return response;
//    }

    public List<HotelReviewConditionSearchResponse> searchReviews(HotelReviewConditionSearchRequest condition, int pageNo) {

        Page<HotelReviewConditionSearchResponse> page =  hotelReviewRepositoryCustom.searchByExpression(condition, PageRequest.of(pageNo, 9));
        List<HotelReviewConditionSearchResponse> response = new ArrayList<>();

        for(HotelReviewConditionSearchResponse hotelReview : page.getContent() ) {
            response.add(hotelReview);
        }

        return response;
    }

//    public void createHotelReviewSummary(LocalDateTime startDate, SummaryType summaryType) {
//        List<HotelReview> hotelReviewList =
//    }

    public String getEmbeddedReviews() {
        List<HotelReview> hotelReviewList = hotelReviewRepository.findAll();
        String response = "";

        for (int i=0; i < hotelReviewList.size() ; i++) {
            response += i + " 번째 리뷰 : " + hotelReviewList.get(i).getComment() + "\n";
        }

        return response;
    }
}
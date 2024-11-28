package com.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dto.advertisement.request.AdvertisementDeleteRequest;
import com.dto.advertisement.response.AdvertisementResponse;
import com.entities.Advertisement;
import com.errors.ApiResponse;
import com.mapper.AdvertisementMapper;
import com.repositories.AdversitementJPA;

@Service
public class AdvertisementService {

    @Autowired
    AdvertisementMapper advertisementMapper;

    @Autowired
    AdversitementJPA adversitementJPA;

    public PageImpl<AdvertisementResponse> getAllAdvertisement(int page, int size) {
        page = (page > 0) ? page - 1 : 0;

        Pageable pageable = PageRequest.of(page, size);

        Page<Advertisement> advertisementPage = adversitementJPA.findByStatus((byte) 1, pageable);

        List<AdvertisementResponse> responseList = advertisementPage.getContent().stream()
                .map(advertisementMapper::toAdvertisementResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responseList, pageable, advertisementPage.getTotalElements());
    }

    public AdvertisementResponse getAdvertisementById(int id) {
        Advertisement advertisement = adversitementJPA.findById(id)
                .orElseThrow();
        return advertisementMapper.toAdvertisementResponse(advertisement);
    }


    @Transactional
    public void deleteAdvertisementByList(AdvertisementDeleteRequest request) {
        for (Integer id : request.getIds()) {
            Advertisement advertisement = adversitementJPA.findById(id)
                    .orElseThrow();
            advertisement.setStatus((byte) 0);
            adversitementJPA.save(advertisement);
        }
    }

    @Transactional
    public void deleteAdvertisementById(int id) {
        Advertisement advertisement = adversitementJPA.findById(id)
                .orElseThrow();
        advertisement.setStatus((byte) 0);
        adversitementJPA.save(advertisement);
    }

    @Transactional
    public List<Advertisement> getAdvertisementsForToday() {
        return adversitementJPA.findAdvertisementsForToday();
    }

}

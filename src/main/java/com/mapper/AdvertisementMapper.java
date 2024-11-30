package com.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.dto.advertisement.request.AdvertisementCreateRequest;
import com.dto.advertisement.request.AdvertisementUpdateRequest;
import com.dto.advertisement.response.AdvertisementResponse;
import com.entities.Advertisement;


@Mapper(componentModel = "spring")
public interface AdvertisementMapper {

    @Mapping(target = "advId", ignore = true)
    Advertisement toAdvertisement(AdvertisementCreateRequest request);

    AdvertisementResponse toAdvertisementResponse(Advertisement advertisement);

    void updateAdvertisement(@MappingTarget Advertisement advertisement,  AdvertisementUpdateRequest request);
}


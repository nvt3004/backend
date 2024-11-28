package com.dto.advertisement.request;

import java.time.LocalDateTime;
import java.util.List;

import com.entities.Image;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AdvertisementCreateRequest {
    private String advName;
    private String advDescription;
    private LocalDateTime endDate;
    private LocalDateTime startDate;
    private byte status;
    private List<Image> images;
}

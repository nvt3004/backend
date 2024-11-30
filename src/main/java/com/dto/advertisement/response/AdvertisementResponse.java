package com.dto.advertisement.response;

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
public class AdvertisementResponse {
    private int advId;
    private String advName;
    private String advDescription;
    private LocalDateTime endDate;
    private LocalDateTime startDate;
    private Integer thoiGianId;
    private byte status;
    private List<Image> images;
}

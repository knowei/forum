package com.forum.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PageQueryDTO {

    @Min(1)
    private Long page = 1L;

    @Min(1)
    @Max(50)
    private Long size = 10L;

    private Integer categoryId;
    private String keyword;
    private String orderBy;
}

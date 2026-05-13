package com.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResourceCreateDTO {

    @NotNull
    private Integer categoryId;

    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 500)
    private String description;

    @Size(max = 65535)
    private String content;

    @Size(max = 255)
    private String cover;

    @Size(max = 500)
    private String downloadLink;

    @Size(max = 50)
    private String extractCode;

    @Size(max = 50)
    private String unzipPassword;

    @Size(max = 200)
    private String tags;

    private Integer status;

    @NotNull
    private Integer type;
}

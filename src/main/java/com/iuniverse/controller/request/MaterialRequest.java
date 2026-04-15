package com.iuniverse.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaterialRequest {

    @NotBlank(message = "title cannot be blank")
    private String title;

    @NotBlank(message = "type cannot be blank (VIDEO, PDF, LINK)")
    private String type;

    @NotBlank(message = "url cannot be blank")
    private String contentUrl;
}
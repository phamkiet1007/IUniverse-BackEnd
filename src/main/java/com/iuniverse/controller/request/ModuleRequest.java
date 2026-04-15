package com.iuniverse.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModuleRequest {

    @NotBlank(message = "Title cannot be blank")
    private String title;

    private Integer orderIndex;
}
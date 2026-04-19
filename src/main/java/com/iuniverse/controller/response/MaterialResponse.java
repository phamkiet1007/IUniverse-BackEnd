package com.iuniverse.controller.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MaterialResponse {
    private Long id;
    private String title;
    private String description;
    private String fileUrl;
    private String type;
}
package com.iuniverse.controller.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ModuleResponse {
    private Long id;
    private String title;
    private String description;
    private Integer orderIndex;

    private List<MaterialResponse> materials;
}
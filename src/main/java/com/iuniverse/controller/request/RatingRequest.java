package com.iuniverse.controller.request;
import lombok.Data;
@Data
public class RatingRequest {
    private Long courseId;
    private Integer starCount;
    private String comment;
}

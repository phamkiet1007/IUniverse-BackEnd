package com.iuniverse.controller.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public abstract class PageResponseAbstract implements Serializable {
    public int pageNumber;
    public int pageSize;
    public int totalPages;
    public long totalElements;
}

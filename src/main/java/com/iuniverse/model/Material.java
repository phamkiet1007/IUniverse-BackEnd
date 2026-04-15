package com.iuniverse.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "tbl_material")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "type", nullable = false, length = 50)
    private String type; //"VIDEO", "PDF", "LINK"

    @Column(name = "content_url", nullable = false, columnDefinition = "TEXT")
    private String contentUrl;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Date createdAt;
}
package com.iuniverse.repository;

import com.iuniverse.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    @Query(value = """
SELECT m.*
FROM tbl_material m
JOIN tbl_module_material mm ON mm.material_id = m.id
WHERE mm.module_id = :moduleId
""", nativeQuery = true)
List<Material> findByModuleId(@Param("moduleId") Long moduleId);
}
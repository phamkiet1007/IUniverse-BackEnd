package com.iuniverse.repository;

import com.iuniverse.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {

    //Lấy orderIndex lớn nhất của một Khóa học. Nếu chưa có chương nào thì trả về 0.
    @Query("SELECT COALESCE(MAX(m.orderIndex), 0) FROM Module m WHERE m.course.id = :courseId")
    Integer findMaxOrderIndexByCourseId(@Param("courseId") Long courseId);
}
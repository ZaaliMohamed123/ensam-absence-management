package com.ensam.academicservice.repositories;

import com.ensam.academicservice.entities.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByAcademicClassId(Long classId);
    List<Schedule> findByModuleId(Long moduleId);
    List<Schedule> findByTeacherId(String teacherId);
    List<Schedule> findByDayOfWeek(DayOfWeek dayOfWeek);

    @Query("SELECT s FROM Schedule s WHERE s.academicClass.id = :classId AND s.dayOfWeek = :dayOfWeek")
    List<Schedule> findByClassIdAndDayOfWeek(@Param("classId") Long classId, @Param("dayOfWeek") DayOfWeek dayOfWeek);

    @Query("SELECT s FROM Schedule s WHERE s.teacherId = :teacherId AND s.dayOfWeek = :dayOfWeek")
    List<Schedule> findByTeacherIdAndDayOfWeek(@Param("teacherId") String teacherId, @Param("dayOfWeek") DayOfWeek dayOfWeek);
}

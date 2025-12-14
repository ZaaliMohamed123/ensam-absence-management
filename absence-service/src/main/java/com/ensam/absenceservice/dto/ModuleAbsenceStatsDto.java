package com.ensam.absenceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleAbsenceStatsDto {
    private Long moduleId;
    private String moduleName;
    private Long totalAbsences;
    private Long unjustifiedAbsences;
    private Boolean isAtRisk; // Si > seuil d'absences (ex: 3 absences)
}

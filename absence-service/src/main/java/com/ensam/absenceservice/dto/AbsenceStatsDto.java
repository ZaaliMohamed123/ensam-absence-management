package com.ensam.absenceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceStatsDto {
    private Long totalAbsences;
    private Long justifiedAbsences;
    private Long unjustifiedAbsences;
    private Long pendingAbsences;
    private Double absenceRate; // Pourcentage d'absences
}

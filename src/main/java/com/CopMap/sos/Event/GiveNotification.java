package com.CopMap.sos.Event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GiveNotification {
    private String assignedOfficerId;
    private String emergencyType;
    private double distanceKm;
}
package com.CopMap.sos.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Entity
@Data
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String triggeringOfficerId;
    private double latitude;
    private double longitude;
    private String emergencyType;
    private String status;

    private Instant timestamp = Instant.now();
}

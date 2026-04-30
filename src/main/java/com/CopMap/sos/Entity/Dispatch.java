package com.CopMap.sos.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Entity
@Data
public class Dispatch {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID alertId;
    private String assignedOfficerId;
    private double distanceKm;
    private String status;

    private Instant assignedAt = Instant.now();
}

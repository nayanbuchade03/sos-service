package com.CopMap.sos.Dtos;

import lombok.Data;

@Data
public class LocationPayload {
    private String officerId;
    private double lat;
    private double lng;
}

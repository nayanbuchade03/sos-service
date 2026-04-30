package com.CopMap.sos.Dtos;

import lombok.Data;

@Data
public class SosPayload {
    private String officerId;
    private double lat;
    private double lng;
    private String type;
}

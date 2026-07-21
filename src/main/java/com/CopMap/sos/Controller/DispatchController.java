package com.CopMap.sos.Controller;


import com.CopMap.sos.Dtos.LocationPayload;
import com.CopMap.sos.Dtos.SosPayload;
import com.CopMap.sos.Entity.Dispatch;
import com.CopMap.sos.Services.DispatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DispatchController {

    private final DispatchService dispatchService;

    @PostMapping("/alert/place-location")
    public ResponseEntity<String> placeLocation(@RequestBody LocationPayload payload) {
        dispatchService.placeOfficerLocation(payload.getOfficerId(), payload.getLat(), payload.getLng());
        return ResponseEntity.ok("Location placed in Redis");
    }

    @PostMapping("/alerts/sos")
    public ResponseEntity<List<Dispatch>> triggerSos(@RequestBody SosPayload payload) {
        List<Dispatch> dispatches = dispatchService.handleSosAlert(
                payload.getOfficerId(), payload.getLat(), payload.getLng(), payload.getType()
        );
        return ResponseEntity.ok(dispatches);
    }

    @GetMapping("/alert/locations")
    public ResponseEntity<Map<String, Point>> getAllLocations() {
        return ResponseEntity.ok(dispatchService.getAllActiveLocations());
    }
}


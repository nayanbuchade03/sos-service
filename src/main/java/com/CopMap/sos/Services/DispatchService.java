package com.CopMap.sos.Services;

import com.CopMap.sos.Entity.Alert;
import com.CopMap.sos.Entity.Dispatch;
import com.CopMap.sos.Event.GiveNotification;
import com.CopMap.sos.Repository.AlertRepo;
import com.CopMap.sos.Repository.DispatchRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchService {

    private final AlertRepo alertRepository;
    private final DispatchRepo dispatchRepository;
    private final StringRedisTemplate redisTemplate;
    private final ApplicationEventPublisher eventPublisher;

    private static final String REDIS_GEO_KEY = "officers_live";
    private static final double DISPATCH_RADIUS_KM = 5.0;

    public void placeOfficerLocation(String officerId, double lat, double lng) {
        redisTemplate.opsForGeo().add(REDIS_GEO_KEY, new Point(lng, lat), officerId);
        log.info("Updated location for officer: {}", officerId);
    }

    @Transactional
    public List<Dispatch> handleSosAlert(String officerId, double lat, double lng, String type) {
        Alert alert = new Alert();
        alert.setTriggeringOfficerId(officerId);
        alert.setLatitude(lat);
        alert.setLongitude(lng);
        alert.setEmergencyType(type);
        alert.setStatus("DISPATCHING");
        alert = alertRepository.save(alert);

        Circle radius = new Circle(new Point(lng, lat), new Distance(DISPATCH_RADIUS_KM, Metrics.KILOMETERS));
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance().sortAscending().limit(5); // Get closest 5 officers

        GeoResults<RedisGeoCommands.GeoLocation<String>> nearbyOfficers =
                redisTemplate.opsForGeo().radius(REDIS_GEO_KEY, radius, args);

        List<Dispatch> assignments = new ArrayList<>();

        if (nearbyOfficers != null) {
            for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : nearbyOfficers) {
                String nearbyOfficerId = result.getContent().getName();

                if (nearbyOfficerId.equals(officerId)) continue;

                Dispatch dispatch = new Dispatch();
                dispatch.setAlertId(alert.getId());
                dispatch.setAssignedOfficerId(nearbyOfficerId);
                dispatch.setDistanceKm(result.getDistance().getValue());
                dispatch.setStatus("PENDING");

                assignments.add(dispatch);
            }
        }

        dispatchRepository.saveAll(assignments);
        log.info("Dispatched {} officers for SOS {}", assignments.size(), alert.getId());

        for (Dispatch assignment : assignments) {
            eventPublisher.publishEvent(new GiveNotification(
                    assignment.getAssignedOfficerId(),
                    alert.getEmergencyType(),
                    assignment.getDistanceKm()
            ));
        }

        return assignments;


    }

    public Map<String, Point> getAllActiveLocations() {
        Set<String> officerIds = redisTemplate.opsForZSet().range(REDIS_GEO_KEY, 0, -1);

        Map<String, Point> locations = new HashMap<>();

        if (officerIds != null && !officerIds.isEmpty()) {
            List<Point> points = redisTemplate.opsForGeo()
                    .position(REDIS_GEO_KEY, officerIds.toArray(new String[0]));

            int index = 0;
            for (String id : officerIds) {
                locations.put(id, points.get(index));
                index++;
            }
        }
        return locations;
    }
}
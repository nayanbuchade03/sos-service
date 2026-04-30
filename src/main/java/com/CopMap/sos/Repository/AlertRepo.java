package com.CopMap.sos.Repository;

import com.CopMap.sos.Entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AlertRepo extends JpaRepository<Alert, UUID> {

}

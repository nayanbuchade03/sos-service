package com.CopMap.sos.Repository;

import com.CopMap.sos.Entity.Dispatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DispatchRepo extends JpaRepository<Dispatch, UUID> {}

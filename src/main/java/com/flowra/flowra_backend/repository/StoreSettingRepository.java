package com.flowra.flowra_backend.repository;

import com.flowra.flowra_backend.entity.StoreSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface StoreSettingRepository extends JpaRepository<StoreSetting, Long> {
    Optional<StoreSetting> findBySettingKey(String settingKey);
    List<StoreSetting> findBySettingGroup(String settingGroup);
}

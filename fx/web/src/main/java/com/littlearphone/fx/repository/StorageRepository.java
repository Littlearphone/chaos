package com.littlearphone.fx.repository;

import com.littlearphone.fx.entity.BaseStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageRepository extends JpaRepository<BaseStorage, Long> {
}

package com.praveen.expensetracker.repository;

import com.praveen.expensetracker.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    
    // Fetch all categories owned by a specific user
    List<CategoryEntity> findByProfileId(Long profileId);
    Optional<CategoryEntity> findByIdAndProfileId(Long id,Long profileId);

    List<CategoryEntity> findByTypeAndProfileId(String type,Long profileId);

    Boolean  existsByNameAndProfileId(String name,long profileId);

}

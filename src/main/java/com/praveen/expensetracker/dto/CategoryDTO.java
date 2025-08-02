package com.praveen.expensetracker.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {

    private Long id;
      private Long profileId; // To associate category with user (by ID only)
    private String name;
    private String icon;
    private String type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
  
}

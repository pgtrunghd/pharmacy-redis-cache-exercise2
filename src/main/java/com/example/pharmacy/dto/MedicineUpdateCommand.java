package com.example.pharmacy.dto;

import java.math.BigDecimal;

public record MedicineUpdateCommand(Long id, String name, BigDecimal price) {}

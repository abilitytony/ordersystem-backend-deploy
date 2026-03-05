package com.example.ordersystem.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RabbitMqStockDto {
    private Long productId;
    private Integer productCount;

}

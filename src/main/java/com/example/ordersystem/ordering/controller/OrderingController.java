package com.example.ordersystem.ordering.controller;

import com.example.ordersystem.ordering.dtos.OrderCreateDto;
import com.example.ordersystem.ordering.dtos.OrderDetailDto;
import com.example.ordersystem.ordering.dtos.OrderListDto;
import com.example.ordersystem.ordering.service.OrderingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordering")
public class OrderingController {
    private final OrderingService orderingService;

    public OrderingController(OrderingService orderingService) {
        this.orderingService = orderingService;
    }
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody List<OrderCreateDto> orderCreateDtoList) {
        Long id = orderingService.create(orderCreateDtoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> findAll(){
        List<OrderListDto> orderListDtoList = orderingService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(orderListDtoList);
    }

    @GetMapping("/myorders")
    public ResponseEntity<?> myOrders(){
        List<OrderListDto> orderListDtoList  = orderingService.myorders();
        return ResponseEntity.status(HttpStatus.OK).body(orderListDtoList);
    }

}
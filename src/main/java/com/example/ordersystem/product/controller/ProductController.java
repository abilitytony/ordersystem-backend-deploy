package com.example.ordersystem.product.controller;

import com.example.ordersystem.product.dtos.ProductCreateDto;
import com.example.ordersystem.product.dtos.ProductResDto;
import com.example.ordersystem.product.dtos.ProductSearchDto;
import com.example.ordersystem.product.dtos.ProductUpdateDto;
import com.example.ordersystem.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@ModelAttribute ProductCreateDto productCreateDto){
        Long id = productService.save(productCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping("/list")
    public ResponseEntity<?> findAll(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)Pageable pageable, ProductSearchDto searchDto){
        Page<ProductResDto> productResDtoList = productService.findAll(pageable, searchDto);
        return ResponseEntity.status(HttpStatus.OK).body(productResDtoList);

    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        ProductResDto productResDto = productService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(productResDto);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @ModelAttribute ProductUpdateDto dto){
        productService.update(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
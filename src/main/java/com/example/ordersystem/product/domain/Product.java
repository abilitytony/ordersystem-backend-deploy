package com.example.ordersystem.product.domain;


import com.example.ordersystem.common.domain.BaseTimeEntity;
import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.product.dtos.ProductUpdateDto;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
@Entity
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private Integer price;
    private Integer stockQuantity;
    private String imagePath;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private Member member;

    public void updateProfileImageUrl(String url){
        this.imagePath = url;
    }

    public void updateStockQuantity(int orderQuantity) {
        this.stockQuantity = this.stockQuantity-orderQuantity;;
    }

    public void updateProduct(ProductUpdateDto dto) {
        this.name = dto.getName();
        this.category = dto.getCategory();
        this.stockQuantity = dto.getStockQuantity();
        this.price = dto.getPrice();
    }
}
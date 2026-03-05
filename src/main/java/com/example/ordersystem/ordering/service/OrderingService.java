package com.example.ordersystem.ordering.service;

import com.example.ordersystem.common.service.RabbitMqStockService;
import com.example.ordersystem.common.service.SseAlarmService;
import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.repository.MemberRepository;
import com.example.ordersystem.ordering.domain.OrderDetail;
import com.example.ordersystem.ordering.domain.Ordering;
import com.example.ordersystem.ordering.dtos.OrderCreateDto;
import com.example.ordersystem.ordering.dtos.OrderListDto;
import com.example.ordersystem.ordering.repository.OrderDetailRepository;
import com.example.ordersystem.ordering.repository.OrderingRepository;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final SseAlarmService sseAlarmService;
    private final RedisTemplate<String, String> redisTemplate;
    private final RabbitMqStockService rabbitMqStockService;

    public OrderingService(OrderingRepository orderingRepository, OrderDetailRepository orderDetailRepository, ProductRepository productRepository, MemberRepository memberRepository, SseAlarmService sseAlarmService, @Qualifier("stockInventory") RedisTemplate<String, String> redisTemplate, RabbitMqStockService rabbitMqStockService) {
        this.orderingRepository = orderingRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
        this.sseAlarmService = sseAlarmService;
        this.redisTemplate = redisTemplate;
        this.rabbitMqStockService = rabbitMqStockService;
    }

//    동시성제어방법1. 특정 메서드에 한해 격리성 올리기
//    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Long create( List<OrderCreateDto> orderCreateDtoList){
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("member is not found"));
        Ordering ordering = Ordering.builder()
                .member(member)
                .build();
        orderingRepository.save(ordering);
        for (OrderCreateDto dto : orderCreateDtoList){
//            동시성 제어 방법2. select for update를 통한 락 설정 이후 조회
//            Product product = productRepository.findByIdForUpdate(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("entity is not found"));
            Product product = productRepository.findById(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("entity is not found"));
//            동시성 제어 방법 3. redis에서 재고 수량 확인 및 재고 수량 감소처리
//            단점 : 조회와 감소 요청이 분리되다보니, 동시성 문제 발생. -> 해결책: 루아(lua)스크립트를 통해 여러 작업을 단일 요청으로 묶어 해결
            String remain = redisTemplate.opsForValue().get(String.valueOf(dto.getProductId()));
            int remaiQuantity = Integer.parseInt(remain);
            if (remaiQuantity < dto.getProductCount()) {
                throw new IllegalArgumentException("재고가 부족합니다.");
            } else {
                redisTemplate.opsForValue().decrement(String.valueOf(dto.getProductId()), dto.getProductCount());
            }
//            if (product.getStockQuantity() < dto.getProductCount()) {
//                throw new IllegalArgumentException("재고가 부족합니다.");
//            }
//            product.updateStockQuantity((dto.getProductCount()));
            OrderDetail orderDetail = OrderDetail.builder()
                    .ordering(ordering)
                    .product(product)
                    .quantity(dto.getProductCount())
                    .build();
            ordering.getOrderDetailList().add(orderDetail);

//            rdb 동기화를 위한 작업1 : 스케줄러 활용
//            rdb 동기화를 위한 작업2 : rabbitmq에 rdb 재고 감소 메시지 발행
            rabbitMqStockService.publish(dto.getProductId(),dto.getProductCount());
        }

//        주문 성공시 admin 유저에게 알림 메시지 전송.
        String message = ordering.getId() + "번 주문이 들어왔습니다.";
        sseAlarmService.sendMessage("admin@naver.com", email, message);

        return ordering.getId();
    }


    @Transactional(readOnly = true)
    public List<OrderListDto> findAll(){
        return orderingRepository.findAll().stream().map(o->OrderListDto.fromEntity(o)).collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<OrderListDto> myorders(){
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("member is not found"));
        return orderingRepository.findAllByMember(member).stream().map(o->OrderListDto.fromEntity(o)).collect(Collectors.toList());
    }

}
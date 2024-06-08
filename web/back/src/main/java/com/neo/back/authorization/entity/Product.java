package com.neo.back.authorization.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.JOINED) // 상속 전략 설정
@Getter
@Setter
public abstract class Product{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;
    private Integer price;
    private Integer tax;

}

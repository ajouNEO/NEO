package com.neo.back.authorization.repository;

import com.neo.back.authorization.entity.PointProduct;
import com.neo.back.authorization.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product,Integer> {

    PointProduct findByItemName(String itemName);


}

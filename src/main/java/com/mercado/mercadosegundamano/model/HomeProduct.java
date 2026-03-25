package com.mercado.mercadosegundamano.model;

import com.mercado.mercadosegundamano.enums.HomeCondition;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("HOME")
public class HomeProduct extends Product{

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HomeCondition condition ;

    @Column
    private String dimensions;

    public HomeProduct() {}

    public HomeProduct(String title, String description, BigDecimal price, User seller, HomeCondition condition, String dimensions) {
        super(title, description, price, seller);
        this.condition = condition;
        this.dimensions = dimensions;
    }

    public HomeCondition getCondition() {
        return condition;
    }

    public void setCondition(HomeCondition condition) {
        this.condition = condition;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }
}

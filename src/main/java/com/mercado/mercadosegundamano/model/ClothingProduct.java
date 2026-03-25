package com.mercado.mercadosegundamano.model;


import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("CLOTHING")
public class ClothingProduct extends Product{

    @Column(nullable = false)
    @NotBlank
    private String brand;

    @Column(nullable = false)
    @NotBlank
    private String size;

    @Column(nullable = false)
    @NotBlank
    private String color;

    @Column(nullable = false)
    @NotBlank
    private String gender;

    public ClothingProduct() {}

    public ClothingProduct(String title, String description, BigDecimal price, User seller,
                           String brand, String size, String color, String gender){
        super(title, description, price, seller);
        this.brand = brand;
        this.size = size;
        this.color = color;
        this.gender = gender;
    }

    public  String getBrand() {
        return brand;
    }

    public  String getSize() {
        return size;
    }

    public  String getColor() {
        return color;
    }

    public  String getGender() {
        return gender;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}

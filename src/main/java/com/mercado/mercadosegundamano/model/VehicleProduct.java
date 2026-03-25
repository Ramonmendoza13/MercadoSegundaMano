package com.mercado.mercadosegundamano.model;

import com.mercado.mercadosegundamano.enums.FuelType;
import com.mercado.mercadosegundamano.enums.GearboxType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("VEHICLE")
public class VehicleProduct extends Product {

    @Column(nullable = false)
    @NotBlank
    private String brand;

    @Column(nullable = false)
    @NotBlank
    private String model;

    @Column(name = "manufacture_year", nullable = false)
    @NotNull
    private Integer year;

    @Column(nullable = false)
    @NotNull
    private Integer mileage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GearboxType gearboxType;

    @Column(nullable = false)
    @NotNull
    private Integer doors;

    // Constructor vacio OBLIGATORIO para JPA
    public VehicleProduct() {}

    // Constructor completo
    public VehicleProduct(String title, String description, BigDecimal price, User seller,
                          String brand, String model, Integer year, Integer mileage,
                          FuelType fuelType, GearboxType gearboxType, Integer doors) {
        super(title, description, price, seller);
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.mileage = mileage;
        this.fuelType = fuelType;
        this.gearboxType = gearboxType;
        this.doors = doors;
    }

    // Getters
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public Integer getYear() { return year; }
    public Integer getMileage() { return mileage; }
    public FuelType getFuelType() { return fuelType; }
    public GearboxType getGearboxType() { return gearboxType; }
    public Integer getDoors() { return doors; }

    // Setters
    public void setBrand(String brand) { this.brand = brand; }
    public void setModel(String model) { this.model = model; }
    public void setYear(Integer year) { this.year = year; }
    public void setMileage(Integer mileage) { this.mileage = mileage; }
    public void setFuelType(FuelType fuelType) { this.fuelType = fuelType; }
    public void setGearboxType(GearboxType gearboxType) { this.gearboxType = gearboxType; }
    public void setDoors(Integer doors) { this.doors = doors; }
}
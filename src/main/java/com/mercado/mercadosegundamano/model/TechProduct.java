package com.mercado.mercadosegundamano.model;

import com.mercado.mercadosegundamano.enums.TechType;
import jakarta.persistence.*;

@Entity
@Table(name = "tech_products")
@DiscriminatorValue("TECHNOLOGY")
public class TechProduct extends Product {

    @Enumerated(EnumType.STRING)
    @Column(name = "tech_type")
    private TechType techType;

    @Column(name = "tech_brand")
    private String brand;

    @Column(name = "tech_model")
    private String model;

    @Column(name = "storage_capacity")
    private String storageCapacity;

    @Column(name = "ram")
    private String ram;

    @Column(name = "tech_color")
    private String color;

    // Constructores
    public TechProduct() {}

    // Getters — sobrescriben los @Transient del padre
    @Override public String getBrand()  { return brand; }
    @Override public String getModel()  { return model; }
    @Override public String getColor()  { return color; }

    public TechType getTechType()       { return techType; }
    public String   getStorageCapacity(){ return storageCapacity; }
    public String   getRam()            { return ram; }

    // Setters
    public void setTechType(TechType techType)          { this.techType = techType; }
    public void setBrand(String brand)                  { this.brand = brand; }
    public void setModel(String model)                  { this.model = model; }
    public void setColor(String color)                  { this.color = color; }
    public void setStorageCapacity(String storageCapacity) { this.storageCapacity = storageCapacity; }
    public void setRam(String ram)                      { this.ram = ram; }
}

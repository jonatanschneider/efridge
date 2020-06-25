package de.thm.mni.vs.gruppe5.common.model;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
public class Product implements Serializable {
    @Id @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    private String id;

    private String name;

    private int productionTime;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<ProductPart> productParts;

    public Product() {

    }

    public Product(String name, int productionTime, Set<ProductPart> productParts) {
        this.name = name;
        this.productionTime = productionTime;
        this.productParts = productParts;
    }

    public String getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProductionTime() {
        return productionTime;
    }

    public void setProductionTime(int productionTime) {
        this.productionTime = productionTime;
    }

    public Set<ProductPart> getProductParts() {
        return productParts;
    }

    public void setProductParts(Set<ProductPart> productParts) {
        this.productParts = productParts;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", productionTime=" + productionTime +
                ", productParts=" + productParts +
                '}';
    }
}

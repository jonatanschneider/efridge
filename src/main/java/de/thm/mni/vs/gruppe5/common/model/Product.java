package de.thm.mni.vs.gruppe5.common.model;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
public class Product implements Serializable {
    @Id
    @GeneratedValue
    private String id;

    private String name;

    private int productionTime;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<ProductPart> productParts;

    public Product() {

    }

    public Product(String name, int productionTime, Set<ProductPart> productParts) {
        this.name = name;
        this.productionTime = productionTime;
        this.productParts = productParts;
    }
}

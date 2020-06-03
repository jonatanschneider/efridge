package de.thm.mni.vs.gruppe5.common;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Map;

@Entity
public class Product {
    @Id
    private String id;

    private String name;

    private int productionTime;

    //private Map<String, Integer> parts;

}

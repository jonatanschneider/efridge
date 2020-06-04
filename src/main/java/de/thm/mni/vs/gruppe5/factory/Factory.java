package de.thm.mni.vs.gruppe5.factory;

import de.thm.mni.vs.gruppe5.common.model.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashSet;

public class Factory {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("eFridge");
        var em = emf.createEntityManager();
        var part = new Part(2.4, Supplier.CoolMechanics);

        var set = new HashSet<ProductPart>();
        var productPart = new ProductPart(part, 2);
        set.add(productPart);
        var product = new Product("Tolles Produkt", 4, set);

        var set2 = new HashSet<OrderItem>();
        var item = new OrderItem(product, 2);
        set2.add(item);
        var fridgeOrder = new FridgeOrder("customerId", set2, OrderStatus.RECEIVED, true);


        em.getTransaction().begin();
        em.persist(fridgeOrder);
        em.getTransaction().commit();
        em.close();
        emf.close();
    }
}

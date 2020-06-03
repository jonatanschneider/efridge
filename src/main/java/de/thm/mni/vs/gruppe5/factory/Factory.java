package de.thm.mni.vs.gruppe5.factory;

import de.thm.mni.vs.gruppe5.common.Order;
import de.thm.mni.vs.gruppe5.common.OrderStatus;
import de.thm.mni.vs.gruppe5.common.Part;
import de.thm.mni.vs.gruppe5.common.Supplier;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;

public class Factory {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("eFridge");
        var em = emf.createEntityManager();
        var part = new Part();
        em.persist(part);

    }
}

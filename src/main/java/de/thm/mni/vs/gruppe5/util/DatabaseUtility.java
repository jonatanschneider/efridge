package de.thm.mni.vs.gruppe5.util;

import javax.persistence.EntityManager;
import java.io.Serializable;

public class DatabaseUtility {

    public static void persist(EntityManager em, Serializable object) {
        em.getTransaction().begin();
        try {
            em.persist(object);
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        }
    }

    public static void merge(EntityManager em, Serializable object) {
        em.getTransaction().begin();
        try {
            em.merge(object);
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        }
    }


}

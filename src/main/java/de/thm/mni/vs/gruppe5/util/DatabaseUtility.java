package de.thm.mni.vs.gruppe5.util;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import java.io.Serializable;

public class DatabaseUtility {

    /**
     * Inserts an object into the database
     * @throws EntityExistsException if id is not unique in the database
     * @param em entity manager
     * @param object object to be inserted
     */
    public static void persist(EntityManager em, Serializable object) throws EntityExistsException {
        em.getTransaction().begin();
        try {
            em.persist(object);
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        }
    }

    /**
     * Inserts or updates objects, based ob whether the id is already existing
     * Note: Use with caution if you want to auto-generate the id
     * @param em entity manager
     * @param object object to be inserted/updated
     */
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

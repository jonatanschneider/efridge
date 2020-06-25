package de.thm.mni.vs.gruppe5.util;

import de.thm.mni.vs.gruppe5.common.Location;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
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

    /**
     * Get instance of EntityManagerFactory for specific location
     * @param location
     * @return EntitiyManagerFactory for persistenceUnit of the given location, null if no valid location was given
     */
    public static EntityManagerFactory getEntityManager(Location location) {
        switch (location) {
            case HEADQUARTER -> { return Persistence.createEntityManagerFactory("eFridge-hq"); }
            case USA -> { return Persistence.createEntityManagerFactory("eFridge-us"); }
            case CHINA -> { return Persistence.createEntityManagerFactory("eFridge-cn"); }
            default -> { return null; }
        }
    }


}

package de.thm.mni.vs.gruppe5.util;

import de.thm.mni.vs.gruppe5.common.Location;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.Serializable;

public class DatabaseUtility {
    /**
     * Inserts an object into the database
     * @throws EntityExistsException if id is not unique in the database
     * @param emf entity manager factory
     * @param object object to be inserted
     */
    public static void persist(EntityManagerFactory emf, Serializable object) throws EntityExistsException {
        var em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(object);
        em.getTransaction().commit();
        em.close();
    }

    /**
     * Inserts or updates objects, based ob whether the id is already existing
     * Note: Use with caution if you want to auto-generate the id
     * @param emf entity manager factory
     * @param object object to be inserted/updated
     */
    public static void merge(EntityManagerFactory emf, Serializable object) {
        var em = emf.createEntityManager();
        em.getTransaction().begin();
        em.merge(object);
        em.getTransaction().commit();
        em.close();
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
            case MEXICO -> { return Persistence.createEntityManagerFactory("eFridge-mx"); }
            case INDIA -> { return Persistence.createEntityManagerFactory("eFridge-in"); }
            default -> { return null; }
        }
    }

}

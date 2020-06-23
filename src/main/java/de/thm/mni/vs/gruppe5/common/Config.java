package de.thm.mni.vs.gruppe5.common;

import de.thm.mni.vs.gruppe5.common.model.Part;
import de.thm.mni.vs.gruppe5.common.model.Product;
import de.thm.mni.vs.gruppe5.common.model.ProductPart;
import de.thm.mni.vs.gruppe5.common.model.Supplier;
import org.eclipse.persistence.jpa.PersistenceProvider;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.*;

public class Config {
    public static final String INCOMING_ORDER_QUEUE = "incomingOrderQueue";
    public static final String INCOMING_TICKET_QUEUE = "incomingTicketQueue";
    public static final String FINISHED_ORDER_QUEUE = "finishedOrderQueue";
    public static final String FINISHED_TICKET_QUEUE = "finishedTicketQueue";
    public static final String ORDER_QUEUE = "orderQueue";
    public static final String TICKET_QUEUE = "ticketQueue";
    public static final String REPORT_QUEUE = "reportQueue";
    public static final float PRODUCTION_COST_PER_SECOND = 0.5f;
    public static final long DAY_DURATION_IN_SECONDS = 60;
    public static final long REPORTS_PER_DAY = 4;
    public static final float CHANCE_OF_CLOSING_TICKET = 0.4f;

    public static List<Product> initializeProducts(Location location) {
        EntityManagerFactory emf = null;
        switch (location) {
            case HEADQUARTER -> emf = Persistence.createEntityManagerFactory("eFridge-hq");
            case USA -> emf = Persistence.createEntityManagerFactory("eFridge-us");
            case CHINA -> emf = Persistence.createEntityManagerFactory("eFridge-cn");
        }

        var em = emf.createEntityManager();

        /* Only write products to database if they don't exist already */
        Query query = em.createQuery("SELECT p FROM Product p");
        List<Product> queryResult = query.getResultList();
        if (queryResult.size() == 5) {
            queryResult.sort(Comparator.comparingInt(p -> Integer.parseInt(p.getId())));
            return queryResult;
        };

        var createdProducts = createProducts();

        em.getTransaction().begin();
        createdProducts.forEach(em::persist);
        em.getTransaction().commit();

        em.close();
        emf.close();

        return createdProducts;
    }

    private static List<Product> createProducts() {
        var result = new ArrayList<Product>();
        var part1 = new Part(1, Supplier.CoolMechanics);
        var part2 = new Part(5, Supplier.CoolMechanics);
        var part3 = new Part(4, Supplier.CoolMechanics);
        var part4 = new Part(2, Supplier.CoolMechanics);
        var part5 = new Part(6, Supplier.CoolMechanics);
        var part6 = new Part(7, Supplier.ElectroStuff);
        var part7 = new Part(3, Supplier.ElectroStuff);
        var part8 = new Part(2, Supplier.ElectroStuff);
        var part9 = new Part(10, Supplier.ElectroStuff);
        var part10 = new Part(4, Supplier.ElectroStuff);

        var product1Set = new HashSet<ProductPart>();
        {
            var pp1 = new ProductPart(part1, 2);
            var pp2 = new ProductPart(part2, 2);
            var pp3 = new ProductPart(part9, 1);
            product1Set.add(pp1);
            product1Set.add(pp2);
            product1Set.add(pp3);
        }

        result.add(new Product("1", "Produkt 1", 2, product1Set));

        var product2Set = new HashSet<ProductPart>();
        {
            var pp1 = new ProductPart(part1, 2);
            var pp2 = new ProductPart(part2, 2);
            var pp3 = new ProductPart(part9, 1);
            product2Set.add(pp1);
            product2Set.add(pp2);
            product2Set.add(pp3);
        }
        result.add(new Product("2", "Produkt 2", 4, product2Set));

        var product3Set = new HashSet<ProductPart>();
        {
            var pp1 = new ProductPart(part3, 1);
            var pp2 = new ProductPart(part10, 2);
            product3Set.add(pp1);
            product3Set.add(pp2);
        }
        result.add(new Product("3", "Produkt 3", 3, product3Set));

        var product4Set = new HashSet<ProductPart>();
        {
            var pp1 = new ProductPart(part4, 1);
            var pp2 = new ProductPart(part8, 4);
            product4Set.add(pp1);
            product4Set.add(pp2);
        }
        result.add(new Product("4", "Produkt 4", 6, product4Set));

        var product5Set = new HashSet<ProductPart>();
        {
            var pp1 = new ProductPart(part5, 1);
            var pp2 = new ProductPart(part7, 1);
            var pp3 = new ProductPart(part9, 1);
            var pp4 = new ProductPart(part6, 2);
            product5Set.add(pp1);
            product5Set.add(pp2);
            product5Set.add(pp3);
            product5Set.add(pp4);
        }
        result.add(new Product("5", "Produkt 5", 5, product5Set));

        return result;
    }
}

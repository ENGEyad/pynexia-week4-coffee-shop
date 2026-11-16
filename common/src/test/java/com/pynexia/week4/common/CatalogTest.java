package com.pynexia.week4.common;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;

public class CatalogTest {
    @Test public void groceryCatalogHasProductsAcrossCategories() {
        List<Product> items = Catalog.grocery();
        assertTrue(items.size() >= 8);
        assertTrue(items.stream().anyMatch(item -> item.category.equals("Fruits")));
        assertTrue(items.stream().anyMatch(item -> item.category.equals("Dairy")));
    }

    @Test public void searchAndCategoryFiltersCanBeCombined() {
        List<Product> results = Catalog.filter(Catalog.grocery(), "ripe", "Fruits");
        assertEquals(1, results.size());
        assertEquals("banana", results.get(0).id);
    }

    @Test public void unknownSearchReturnsNoResults() {
        assertTrue(Catalog.filter(Catalog.retail(), "not a product", "All").isEmpty());
    }

    @Test public void coffeeMenuFiltersColdDrinks() {
        List<Product> results = Catalog.filter(Catalog.coffee(), "cold", "Cold");
        assertEquals(1, results.size());
        assertEquals("coldbrew", results.get(0).id);
    }

    @Test public void retailCatalogFiltersElectronics() {
        List<Product> results = Catalog.filter(Catalog.retail(), "", "Electronics");
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(item -> item.category.equals("Electronics")));
    }
}

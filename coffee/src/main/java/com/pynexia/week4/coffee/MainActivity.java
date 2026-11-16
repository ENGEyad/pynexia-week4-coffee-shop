package com.pynexia.week4.coffee;

import com.pynexia.week4.common.Catalog;
import com.pynexia.week4.common.CommerceActivity;
import com.pynexia.week4.common.Product;
import java.util.List;

public final class MainActivity extends CommerceActivity {
    @Override protected String storeName() { return "Brew & Bean"; }
    @Override protected String storeSubtitle() { return "Small batch coffee · thoughtfully made"; }
    @Override protected int accentColor() { return 0xff9b5c38; }
    @Override protected boolean coffeeMode() { return true; }
    @Override protected boolean cloudMode() { return false; }
    @Override protected List<Product> sampleCatalog() { return Catalog.coffee(); }
}

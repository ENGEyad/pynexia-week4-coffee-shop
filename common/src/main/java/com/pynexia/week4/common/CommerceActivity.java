package com.pynexia.week4.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Iterator;
import org.json.JSONObject;
import org.json.JSONArray;

public abstract class CommerceActivity extends Activity {
    private final List<Product> products = new ArrayList<>();
    private final List<Product> shown = new ArrayList<>();
    private final Cart cart = new Cart();
    private final Map<String, Double> skuPrices = new HashMap<>();
    private final Map<String, String> skuNames = new HashMap<>();
    private FirebaseApp firebaseApp;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private ListenerRegistration productListener;
    private ProductAdapter adapter;
    private RecyclerView productList;
    private LinearLayout chips;
    private TextView status;
    private TextView cartButton;
    private EditText search;
    private String selectedCategory = "All";
    private boolean customPricesLoaded;
    private static final int INK = Color.rgb(30, 42, 40);
    private static final int MUTED = Color.rgb(103, 117, 113);

    protected abstract String storeName();
    protected abstract String storeSubtitle();
    protected abstract int accentColor();
    protected abstract boolean coffeeMode();
    protected abstract boolean cloudMode();
    protected abstract List<Product> sampleCatalog();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(247, 248, 247));
        getWindow().setNavigationBarColor(Color.WHITE);
        products.addAll(sampleCatalog());
        restoreCart();
        if (cloudMode()) initializeFirebase();
        buildUi();
        refreshProducts();
    }

    @Override protected void onDestroy() {
        if (productListener != null) productListener.remove();
        super.onDestroy();
    }

    private void initializeFirebase() {
        firebaseApp = FirebaseBridge.initialize(this);
        if (firebaseApp == null) return;
        auth = FirebaseAuth.getInstance(firebaseApp);
        firestore = FirebaseFirestore.getInstance(firebaseApp);
        productListener = firestore.collection("products").addSnapshotListener((snapshot, error) -> {
            if (error != null || snapshot == null || snapshot.isEmpty()) return;
            List<Product> remote = new ArrayList<>();
            for (com.google.firebase.firestore.DocumentSnapshot document : snapshot.getDocuments()) {
                String id = document.getId();
                String name = document.getString("name");
                String category = document.getString("category");
                String detail = document.getString("detail");
                Double price = document.getDouble("price");
                if (name != null && category != null && price != null && price >= 0) remote.add(new Product(id, name, category, detail == null ? "" : detail, price));
            }
            if (!remote.isEmpty()) {
                products.clear(); products.addAll(remote); customPricesLoaded = true; refreshProducts();
                if (status != null) status.setText("Live catalog · Firebase");
            }
        });
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(16), dp(20), dp(10));
        root.setBackgroundColor(Color.rgb(247, 248, 247));

        LinearLayout top = row();
        LinearLayout titleGroup = new LinearLayout(this); titleGroup.setOrientation(LinearLayout.VERTICAL);
        titleGroup.addView(text(storeName(), 23, INK, true));
        titleGroup.addView(text(storeSubtitle(), 12, MUTED, false));
        top.addView(titleGroup, new LinearLayout.LayoutParams(0, -2, 1));
        Button account = button("Account", false); top.addView(account, wrapButton());
        account.setOnClickListener(v -> accountDialog());
        Button orders = button("Orders", false); top.addView(orders, margin(6, 0, 0, 0));
        orders.setOnClickListener(v -> showOrders());
        root.addView(top, margin(0, 0, 0, 12));

        status = text(cloudMode() ? (firebaseApp == null ? "Demo catalog · Firebase setup needed" : "Firebase connected · sample catalog") : "Freshly prepared · pickup and delivery", 12, cloudMode() && firebaseApp == null ? 0xff8b6a24 : accentColor(), false);
        status.setPadding(dp(12), dp(10), dp(12), dp(10)); status.setBackground(background(0xfff1f4f2, 12));
        root.addView(status, margin(0, 0, 0, 12));

        search = new EditText(this); search.setSingleLine(true); search.setTextSize(14); search.setHint("Search the menu");
        search.setPadding(dp(14), dp(9), dp(14), dp(9)); search.setBackground(background(Color.WHITE, 12));
        root.addView(search, margin(0, 0, 0, 11));
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { refreshProducts(); }
            @Override public void afterTextChanged(Editable s) { }
        });

        HorizontalScrollView chipScroll = new HorizontalScrollView(this); chipScroll.setHorizontalScrollBarEnabled(false);
        chips = row(); chipScroll.addView(chips); root.addView(chipScroll, margin(0, 0, 0, 8));

        productList = new RecyclerView(this); productList.setLayoutManager(new LinearLayoutManager(this)); productList.setNestedScrollingEnabled(true);
        adapter = new ProductAdapter(); productList.setAdapter(adapter);
        root.addView(productList, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout bottom = row(); bottom.setPadding(dp(12), dp(10), dp(12), dp(10)); bottom.setBackground(background(Color.WHITE, 16));
        TextView delivery = text(coffeeMode() ? "Ready when you are" : "Free delivery over $35", 13, MUTED, true);
        bottom.addView(delivery, new LinearLayout.LayoutParams(0, -2, 1));
        cartButton = text("Cart · 0", 15, Color.WHITE, true); cartButton.setGravity(Gravity.CENTER); cartButton.setPadding(dp(18), dp(13), dp(18), dp(13)); cartButton.setBackground(background(accentColor(), 14));
        bottom.addView(cartButton, wrapButton()); cartButton.setOnClickListener(v -> showCart());
        LinearLayout.LayoutParams bottomLp = new LinearLayout.LayoutParams(-1, -2); bottomLp.setMargins(0, dp(9), 0, 0); root.addView(bottom, bottomLp);

        setContentView(root);
        updateCartLabel();
        renderCategories();
    }

    private void renderCategories() {
        chips.removeAllViews();
        List<String> categories = new ArrayList<>(); categories.add("All");
        for (Product product : products) if (!categories.contains(product.category)) categories.add(product.category);
        for (String category : categories) {
            Button chip = button(category, category.equals(selectedCategory));
            chips.addView(chip, margin(0, 0, 8, 0));
            chip.setOnClickListener(v -> { selectedCategory = category; renderCategories(); refreshProducts(); });
        }
    }

    private void refreshProducts() {
        if (productList == null) return;
        List<Product> result = Catalog.filter(products, search == null ? "" : search.getText().toString(), selectedCategory);
        shown.clear(); shown.addAll(result); adapter.notifyDataSetChanged();
        if (chips != null) renderCategories();
    }

    private final class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.Holder> {
        @Override public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout card = row(); card.setGravity(Gravity.CENTER_VERTICAL); card.setPadding(dp(14), dp(13), dp(12), dp(13)); card.setBackground(background(Color.WHITE, 15));
            TextView mark = text("•", 25, accentColor(), true); mark.setGravity(Gravity.CENTER); card.addView(mark, new LinearLayout.LayoutParams(dp(34), dp(42)));
            LinearLayout info = new LinearLayout(CommerceActivity.this); info.setOrientation(LinearLayout.VERTICAL); info.setPadding(dp(8), 0, dp(4), 0);
            TextView name = text("", 15, INK, true); TextView detail = text("", 12, MUTED, false); TextView price = text("", 13, accentColor(), true);
            info.addView(name); info.addView(detail, margin(0, 2, 0, 2)); info.addView(price); card.addView(info, new LinearLayout.LayoutParams(0, -2, 1));
            Button add = button("Add", true); card.addView(add, wrapButton());
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(-1, -2); lp.setMargins(0, 0, 0, dp(10)); card.setLayoutParams(lp);
            return new Holder(card, name, detail, price, add);
        }
        @Override public void onBindViewHolder(Holder holder, int position) {
            Product product = shown.get(position);
            holder.name.setText(product.name); holder.detail.setText(product.detail);
            holder.price.setText(String.format(Locale.US, "$%.2f", product.price));
            holder.add.setOnClickListener(v -> addProduct(product));
            holder.itemView.setOnClickListener(v -> productDialog(product));
        }
        @Override public int getItemCount() { return shown.size(); }
        final class Holder extends RecyclerView.ViewHolder {
            final TextView name, detail, price; final Button add;
            Holder(View item, TextView name, TextView detail, TextView price, Button add) { super(item); this.name=name; this.detail=detail; this.price=price; this.add=add; }
        }
    }

    private void addProduct(Product product) {
        if (coffeeMode()) {
            String[] sizes = {"Small", "Regular", "Large"};
            double[] extras = {0, .75, 1.25};
            new AlertDialog.Builder(this).setTitle(product.name + " · choose a size").setItems(sizes, (dialog, which) -> {
                String key = product.id + "|" + sizes[which];
                skuPrices.put(key, product.price + extras[which]); skuNames.put(key, product.name + " · " + sizes[which]);
                cart.add(key); saveCart(); updateCartLabel();
            }).show();
        } else {
            skuPrices.put(product.id, product.price); skuNames.put(product.id, product.name);
            cart.add(product.id); saveCart(); updateCartLabel();
            Toast.makeText(this, product.name + " added", Toast.LENGTH_SHORT).show();
        }
    }

    private void productDialog(Product product) {
        new AlertDialog.Builder(this).setTitle(product.name).setMessage(product.detail + "\n\n" + String.format(Locale.US, "$%.2f", product.price))
            .setNegativeButton("Close", null).setPositiveButton(coffeeMode() ? "Choose size" : "Add to cart", (dialog, which) -> addProduct(product)).show();
    }

    private void showCart() {
        LinearLayout layout = new LinearLayout(this); layout.setOrientation(LinearLayout.VERTICAL); layout.setPadding(dp(22), dp(8), dp(22), dp(8));
        Map<String, Integer> entries = cart.entries();
        if (entries.isEmpty()) layout.addView(text("Your cart is waiting for something good.", 14, MUTED, false));
        for (Map.Entry<String, Integer> entry : entries.entrySet()) {
            String key = entry.getKey();
            LinearLayout line = row();
            line.addView(text(skuNames.getOrDefault(key, productName(key)), 13, INK, true), new LinearLayout.LayoutParams(0, -2, 1));
            TextView qty = text(String.valueOf(entry.getValue()), 13, INK, true); line.addView(qty, margin(6, 0, 6, 0));
            Button minus = button("−", false); line.addView(minus, wrapButton());
            minus.setOnClickListener(v -> { cart.removeOne(key); saveCart(); updateCartLabel(); showCart(); });
            Button plus = button("+", true); line.addView(plus, margin(6, 0, 0, 0));
            plus.setOnClickListener(v -> { cart.add(key); saveCart(); updateCartLabel(); showCart(); });
            layout.addView(line, margin(0, 0, 0, 10));
        }
        TextView total = text(String.format(Locale.US, "Subtotal · $%.2f", subtotal()), 16, INK, true); layout.addView(total, margin(0, 8, 0, 8));
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Your cart").setView(layout)
            .setNegativeButton("Keep shopping", null).setNeutralButton("Clear", (d, w) -> { cart.clear(); saveCart(); updateCartLabel(); })
            .setPositiveButton("Checkout", null).create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (cart.itemCount() == 0) { Toast.makeText(this, "Add an item before checkout", Toast.LENGTH_SHORT).show(); return; }
            dialog.dismiss(); checkoutDialog();
        }));
        dialog.show();
    }

    private void checkoutDialog() {
        EditText address = new EditText(this); address.setHint(coffeeMode() ? "Name for pickup" : "Delivery address"); address.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle(coffeeMode() ? "Pickup details" : "Delivery details").setView(address)
            .setNegativeButton("Back", null).setPositiveButton("Place order", (dialog, which) -> {
                String destination = address.getText().toString().trim();
                if (destination.length() < 3) { Toast.makeText(this, "Enter your delivery or pickup details", Toast.LENGTH_LONG).show(); return; }
                placeOrder(destination);
            }).show();
    }

    private void placeOrder(String destination) {
        if (cloudMode() && firebaseApp != null && auth.getCurrentUser() == null) {
            accountDialog(); Toast.makeText(this, "Sign in before placing a cloud order", Toast.LENGTH_LONG).show(); return;
        }
        double amount = subtotal();
        if (cloudMode() && firestore != null && auth.getCurrentUser() != null) {
            FirebaseUser user = auth.getCurrentUser();
            Map<String, Object> order = new LinkedHashMap<>();
            order.put("userId", user.getUid()); order.put("destination", destination); order.put("total", amount);
            order.put("items", new LinkedHashMap<>(cart.entries())); order.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
            firestore.collection("orders").add(order).addOnSuccessListener(ref -> orderComplete(ref.getId(), amount, true))
                .addOnFailureListener(error -> Toast.makeText(this, "Could not send order. Check Firebase rules and try again.", Toast.LENGTH_LONG).show());
        } else orderComplete("DEMO-" + System.currentTimeMillis(), amount, false);
    }

    private void orderComplete(String id, double amount, boolean cloud) {
        rememberOrder(id, amount, cloud);
        cart.clear(); saveCart(); updateCartLabel();
        String message = (cloud ? "Order placed" : "Demo order saved on this device") + "\nOrder " + id.substring(0, Math.min(10, id.length())) + "\nTotal $" + String.format(Locale.US, "%.2f", amount);
        new AlertDialog.Builder(this).setTitle("Thank you").setMessage(message).setPositiveButton("Done", null).show();
    }

    private void accountDialog() {
        if (!cloudMode()) { new AlertDialog.Builder(this).setTitle("Guest checkout").setMessage("This coffee shop demo keeps the cart on this device.").setPositiveButton("Got it", null).show(); return; }
        if (firebaseApp == null) {
            new AlertDialog.Builder(this).setTitle("Demo mode").setMessage("Browse products, manage the cart, and try checkout locally. To enable real authentication and cloud orders, add a Firebase project config as described in firebase-setup.md.").setPositiveButton("Continue in demo mode", null).show();
            return;
        }
        FirebaseUser current = auth.getCurrentUser();
        if (current != null) {
            new AlertDialog.Builder(this).setTitle("Signed in").setMessage(current.getEmail() == null ? current.getUid() : current.getEmail())
                .setNegativeButton("Close", null).setPositiveButton("Sign out", (d, w) -> { auth.signOut(); Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show(); }).show();
            return;
        }
        LinearLayout form = new LinearLayout(this); form.setOrientation(LinearLayout.VERTICAL); form.setPadding(dp(20), dp(8), dp(20), dp(4));
        EditText email = new EditText(this); email.setHint("Email address"); email.setInputType(33); form.addView(email);
        EditText password = new EditText(this); password.setHint("Password (6+ characters)"); password.setInputType(129); form.addView(password);
        new AlertDialog.Builder(this).setTitle("Sign in or create an account").setView(form).setNegativeButton("Cancel", null)
            .setNeutralButton("Create account", (d, w) -> authenticate(email.getText().toString().trim(), password.getText().toString(), true))
            .setPositiveButton("Sign in", (d, w) -> authenticate(email.getText().toString().trim(), password.getText().toString(), false)).show();
    }

    private void rememberOrder(String id, double amount, boolean cloud) {
        try {
            JSONArray orders = new JSONArray(getSharedPreferences("orders", MODE_PRIVATE).getString("history", "[]"));
            JSONObject order = new JSONObject();
            order.put("id", id); order.put("total", amount); order.put("cloud", cloud); order.put("time", System.currentTimeMillis());
            orders.put(order);
            getSharedPreferences("orders", MODE_PRIVATE).edit().putString("history", orders.toString()).apply();
        } catch (Exception ignored) { }
    }

    private void showOrders() {
        if (cloudMode() && firestore != null && auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            firestore.collection("orders").whereEqualTo("userId", uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) { showLocalOrders(); return; }
                    StringBuilder body = new StringBuilder();
                    for (com.google.firebase.firestore.DocumentSnapshot order : snapshot.getDocuments()) {
                        Double total = order.getDouble("total");
                        body.append("Order ").append(order.getId(), 0, Math.min(8, order.getId().length()));
                        if (total != null) body.append(" · $").append(String.format(Locale.US, "%.2f", total));
                        body.append(" · Placed\n");
                    }
                    new AlertDialog.Builder(this).setTitle("My orders").setMessage(body.toString()).setPositiveButton("Done", null).show();
                })
                .addOnFailureListener(error -> showLocalOrders());
        } else showLocalOrders();
    }

    private void showLocalOrders() {
        StringBuilder body = new StringBuilder();
        try {
            JSONArray orders = new JSONArray(getSharedPreferences("orders", MODE_PRIVATE).getString("history", "[]"));
            for (int i = orders.length() - 1; i >= 0; i--) {
                JSONObject order = orders.getJSONObject(i);
                String id = order.optString("id", "Local order");
                body.append("Order ").append(id, 0, Math.min(10, id.length()))
                    .append(" · $").append(String.format(Locale.US, "%.2f", order.optDouble("total")))
                    .append(order.optBoolean("cloud") ? " · Synced\n" : " · Demo\n");
            }
        } catch (Exception ignored) { }
        if (body.length() == 0) body.append("No orders yet.");
        new AlertDialog.Builder(this).setTitle("Order history").setMessage(body.toString()).setPositiveButton("Done", null).show();
    }

    private void authenticate(String email, String password, boolean create) {
        if (email.isEmpty() || password.length() < 6) { Toast.makeText(this, "Enter a valid email and a password of at least 6 characters", Toast.LENGTH_LONG).show(); return; }
        com.google.android.gms.tasks.Task<com.google.firebase.auth.AuthResult> task = create
            ? auth.createUserWithEmailAndPassword(email, password) : auth.signInWithEmailAndPassword(email, password);
        task.addOnSuccessListener(result -> { status.setText("Signed in · " + email); Toast.makeText(this, "Account ready", Toast.LENGTH_SHORT).show(); })
            .addOnFailureListener(error -> Toast.makeText(this, "Sign-in failed: " + error.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void restoreCart() {
        String saved = getSharedPreferences("cart", MODE_PRIVATE).getString("items", "{}");
        try {
            JSONObject json = new JSONObject(saved);
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                int count = json.optInt(key, 0);
                for (int i = 0; i < count; i++) cart.add(key);
                String id = key.contains("|") ? key.substring(0, key.indexOf('|')) : key;
                Product product = findProduct(id);
                if (product != null) {
                    double price = product.price;
                    String name = product.name;
                    if (key.contains("|")) {
                        String size = key.substring(key.indexOf('|') + 1); name += " · " + size;
                        price += size.equals("Large") ? 1.25 : size.equals("Regular") ? .75 : 0;
                    }
                    skuPrices.put(key, price); skuNames.put(key, name);
                }
            }
        } catch (Exception ignored) { cart.clear(); }
    }

    private void saveCart() {
        JSONObject json = new JSONObject();
        try { for (Map.Entry<String, Integer> entry : cart.entries().entrySet()) json.put(entry.getKey(), entry.getValue()); }
        catch (Exception ignored) { }
        getSharedPreferences("cart", MODE_PRIVATE).edit().putString("items", json.toString()).apply();
    }

    private Product findProduct(String id) { for (Product product : products) if (product.id.equals(id)) return product; return null; }
    private String productName(String key) { Product product = findProduct(key.contains("|") ? key.substring(0, key.indexOf('|')) : key); return product == null ? key : product.name; }
    private double subtotal() { Map<String, Double> prices = new HashMap<>(); for (String key : cart.entries().keySet()) prices.put(key, skuPrices.getOrDefault(key, 0.0)); return cart.subtotal(prices); }
    private void updateCartLabel() { if (cartButton != null) cartButton.setText("Cart · " + cart.itemCount() + " · $" + String.format(Locale.US, "%.2f", subtotal())); }

    private LinearLayout row() { LinearLayout layout = new LinearLayout(this); layout.setOrientation(LinearLayout.HORIZONTAL); layout.setGravity(Gravity.CENTER_VERTICAL); return layout; }
    private TextView text(String value, int size, int color, boolean bold) { TextView view = new TextView(this); view.setText(value); view.setTextSize(size); view.setTextColor(color); if (bold) view.setTypeface(null, Typeface.BOLD); return view; }
    private Button button(String label, boolean selected) { Button button = new Button(this); button.setText(label); button.setAllCaps(false); button.setTextSize(12); button.setTextColor(selected ? Color.WHITE : INK); button.setPadding(dp(10), dp(2), dp(10), dp(2)); button.setBackground(background(selected ? accentColor() : Color.WHITE, 12)); return button; }
    private GradientDrawable background(int color, int radius) { GradientDrawable drawable = new GradientDrawable(); drawable.setColor(color); drawable.setCornerRadius(dp(radius)); return drawable; }
    private LinearLayout.LayoutParams margin(int l, int t, int r, int b) { LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2); lp.setMargins(dp(l), dp(t), dp(r), dp(b)); return lp; }
    private LinearLayout.LayoutParams wrapButton() { return new LinearLayout.LayoutParams(-2, dp(44)); }
    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + .5f); }
}

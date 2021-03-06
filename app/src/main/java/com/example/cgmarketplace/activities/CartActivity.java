package com.example.cgmarketplace.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cgmarketplace.R;
import com.example.cgmarketplace.adapters.CartAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnProductSelectedListener {

    private static final String TAG = "CartActivity";
    private static final int LIMIT = 50;
    int totalPriceCart = 0;
    Double qtyItem;
    Double priceItem;
    String totalPriceFormat;
    private Button btn_minus, btn_plus, btn_goto_payment;
    private TextView tvTitle, tv_jumlah_cart, tv_total_price;
    private RecyclerView rv_cart;
    private String userId;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private Query mQuery;
    private CartAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();

        mFirestore = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.cart_title);
        rv_cart = findViewById(R.id.rv_cart);
        btn_minus = findViewById(R.id.btn_minus);
        btn_plus = findViewById(R.id.btn_plus);
        btn_goto_payment = findViewById(R.id.btn_goto_payment);
        tv_jumlah_cart = findViewById(R.id.tv_jumlah_cart);
        tv_total_price = findViewById(R.id.tv_total_price);

        btn_goto_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(CartActivity.this, ShippingAddressActivity.class);
                startActivity(i);
            }
        });


        bottomNav();
        initFirestore();
        initRecyclerView();

    }

    private void initFirestore() {

        tvTitle.setText(R.string.cart_title);

        mQuery = mFirestore.collection("Users").document(userId).collection("Cart");
    }

    private void bottomNav() {

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // set selected home
        bottomNavigationView.setSelectedItemId(R.id.cart);

        // item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class).setFlags(FLAG_ACTIVITY_CLEAR_TOP));
                        finish();
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.cart:
                        return true;

                    case R.id.wishlist:
                        startActivity(new Intent(getApplicationContext(), WishlistActivity.class).setFlags(FLAG_ACTIVITY_CLEAR_TOP));
                        finish();
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.transaction:
                        startActivity(new Intent(getApplicationContext(), TransactionActivity.class).setFlags(FLAG_ACTIVITY_CLEAR_TOP));
                        finish();
                        overridePendingTransition(0, 0);
                        return true;
                }

                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class).setFlags(FLAG_ACTIVITY_CLEAR_TOP));
            finish();
            overridePendingTransition(0, 0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initRecyclerView() {

        if (mQuery == null) {
            Log.w(TAG, "No query, not initializing RecyclerView");
        }

        mAdapter = new CartAdapter(mQuery, this) {

            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    rv_cart.setVisibility(View.GONE);
                    setContentView(R.layout.empty_cart);
                    bottomNav();

                    Log.w(TAG, "ItemCount = 0");
                } else {
                    rv_cart.setVisibility(View.VISIBLE);
                    initTotalPrice();
                    Log.w(TAG, "Show Produk");
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(findViewById(android.R.id.content),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show();
            }
        };

        rv_cart.setLayoutManager(new LinearLayoutManager(CartActivity.this));
        rv_cart.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @Override
    public void onProductSelected(DocumentSnapshot cartModel) {
        // Go to the details page for the selected restaurant
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.KEY_PRODUCT_ID, cartModel.getId());
        intent.putExtra("no-Button", true);

        startActivity(intent);
    }

    @Override
    public void onDeleteSelected(final DocumentSnapshot cartModel) {

        String deletedProduct = cartModel.getId();
        final DocumentReference priceProduct = mFirestore.collection("Users").document(userId).collection("Cart").document(deletedProduct);
        priceProduct.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                initTotalPrice();
            }
        });
        Toast.makeText(CartActivity.this, "Product with ID" + deletedProduct + "Deleted From Cart",
                Toast.LENGTH_LONG).show();
    }

    private void initTotalPrice() {

        CollectionReference docPrice = mFirestore.collection("Users").document(userId).collection("Cart");
        docPrice.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    List<DocumentSnapshot> listPrice = task.getResult().getDocuments();
                    listPrice.size();
                    for (int i = 0; i < listPrice.size(); i++) {

                        qtyItem = listPrice.get(i).getDouble("qty");
                        priceItem = listPrice.get(i).getDouble("price");
                        totalPriceCart += qtyItem * priceItem;
                        Log.w("qty item", String.valueOf(qtyItem)); //debug qty
                        Log.w("price item", String.valueOf(priceItem)); //debug total
                        Log.w("total", String.valueOf(totalPriceCart)); //debug price total

                    }

                    totalPriceFormat = NumberFormat.getCurrencyInstance(Locale.US).format(totalPriceCart);
                    tv_total_price.setText(totalPriceFormat);
                    totalPriceCart = 0;
                }
            }
        });
    }
}



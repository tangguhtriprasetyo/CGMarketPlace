package com.example.cgmarketplace.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.cgmarketplace.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TransactionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // set selected home
        bottomNavigationView.setSelectedItemId(R.id.transaction);

        // item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.cart:
                        startActivity(new Intent(getApplicationContext(),CartActivity.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.wishlist:
                        startActivity(new Intent(getApplicationContext(),WishlistActivity.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.transaction:
                        return true;
                }

                return false;
            }
        });

    }
}

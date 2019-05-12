package com.app.petify.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.widget.TextView;

import com.app.petify.R;
import com.facebook.Profile;

public class PerfilActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private NavigationView navView;
    private TextView drawerTitle;
    private MenuItem itemPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        navView = findViewById(R.id.driver_nav_view);
        navView.setNavigationItemSelectedListener(this);
        itemPerfil = navView.getMenu().getItem(2);
        drawerTitle = navView.getHeaderView(0).findViewById(R.id.nav_header_title);
        drawerTitle.setText("Hola " + Profile.getCurrentProfile().getFirstName() + "!");
        SpannableString s = new SpannableString(itemPerfil.getTitle());
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getBaseContext(), R.color.com_facebook_blue)), 0, s.length(), 0);
        itemPerfil.setTitle(s);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        Intent i = null;
        if (id == R.id.nav_item_inicio)
            i = new Intent(this, DriverHomeActivity.class);
        startActivity(i);
        return false;
    }
}

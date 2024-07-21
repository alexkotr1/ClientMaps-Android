package com.alexk.clientmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Client> clients;
    private RequestQueue requestQueue;
    private MyAdapter adapter = null;
    private SwipeRefreshLayout swipeRefreshLayout = null;
    private Boolean isActivityPaused = true;
    private TextView clientCount;

    @Override
    protected void onResume() {
        super.onResume();
        if (isActivityPaused) {
            load();
        }
        isActivityPaused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityPaused = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = new Intent(this, NetworkService.class);
        startService(intent);
        super.onCreate(savedInstanceState);
        Helper.loadCredentials(getApplicationContext());
        setTitle("Οδηγός Πελατών");
        isActivityPaused = false;
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerview);
        clientCount = findViewById(R.id.clientCount);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        load();
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                load();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab_add);
        SearchView searchView = findViewById(R.id.searchView);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        searchView.setQueryHint("Αναζήτηση");
        searchView.setIconified(false);
        searchView.clearFocus();

        ImageView searchCloseButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        if (searchCloseButton != null) {
            searchCloseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchView.setQuery("", false);
                    hideKeyboard();
                    searchView.clearFocus();
                    Log.d("SearchView", "Clear button clicked");
                }
            });
        }

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard();
                }
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchView.setQuery("", false);
                searchView.clearFocus();
                hideKeyboard();
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                hideKeyboard();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.filter(newText);
                }
                return true;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddController.class);
                startActivity(intent);
            }
        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingsController.class);
                startActivity(intent);
                return true;
            }
        });
    }

    private void load() {
        if (!Helper.hasInternetAccess(MainActivity.this)) {
            setOffline();
            return;
        }
        Client.getAllClients(MainActivity.this);
        ClientRequest request = new ClientRequest(Helper.DATA_ENDPOINT + Helper.PASSWORD, new Response.Listener<List<Client>>() {
            @Override
            public void onResponse(List<Client> response) {
                clients = response;
                Helper.cacheClients(response, MainActivity.this);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                Collections.sort(clients, new Comparator<Client>() {
                    @Override
                    public int compare(Client c1, Client c2) {
                        return c1.getName().compareToIgnoreCase(c2.getName());
                    }
                });
                clientCount.setText(String.format("Συνολικοί Πελάτες: %s", String.valueOf(clients.size())));
                adapter = new MyAdapter(getApplicationContext(), clients);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                recyclerView.setAdapter(adapter);
                adapter.setContext(MainActivity.this);
                adapter.notifyDataSetChanged();
                setTitle("Οδηγός Πελατών");

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swipeRefreshLayout.setRefreshing(false);
                error.printStackTrace();
                if (error instanceof TimeoutError) {
                    setOffline();
                }
            }

        });

        requestQueue.add(request);

    }

    public void setOffline() {
        List<Client> clients = Helper.retrieveCache(MainActivity.this);
        if (clients == null) return;
        Collections.sort(clients, new Comparator<Client>() {
            @Override
            public int compare(Client c1, Client c2) {
                return c1.getName().compareToIgnoreCase(c2.getName());
            }
        });
        clientCount.setText(String.format("Συνολικοί Πελάτες: %s", String.valueOf(clients.size())));
        adapter = new MyAdapter(getApplicationContext(), clients);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adapter);
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
        setTitle("Οδηγός Πελατών (Εκτός Σύνδεσης)");
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}

package com.teamihc.inventas.fragments;

import android.app.Fragment;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.teamihc.inventas.R;
import com.teamihc.inventas.backend.entidades.Articulo;
import com.teamihc.inventas.adapters.listaproductos.InventarioRVAdapter;

import java.io.File;
import java.util.ArrayList;


public class InventarioFragment extends Fragment
{
    private RecyclerView recyclerView;
    private LinearLayout bienvenida;
    private SearchView searchView;
    private ArrayList<Articulo> listaArticulosOriginal;
    private InventarioRVAdapter adapter;
    private Handler searchHandler;
    private Runnable searchRunnable;
    private Spinner spinnerCategoriaFiltro;
    private String categoriaSeleccionada = "Todas las categorías";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_inventario, container, false);
        recyclerView = view.findViewById(R.id.productos_inventarioRV);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        
        // Disable measurement cache to prevent issues with changing data
        recyclerView.getLayoutManager().setMeasurementCacheEnabled(false);
        
        // Set these properties to improve RecyclerView stability
        recyclerView.setHasFixedSize(false);
        recyclerView.setItemViewCacheSize(0);
        recyclerView.setDrawingCacheEnabled(false);
        recyclerView.setItemAnimator(null); // Disable animations to prevent ViewHolder issues
        
        bienvenida = view.findViewById(R.id.bienvenida_inventario);
        searchView = view.findViewById(R.id.searchView);
        //AGREGADO
        spinnerCategoriaFiltro = view.findViewById(R.id.spinnerCategoriaFiltro);
        setupSpinnerCategoria();
        listaArticulosOriginal = new ArrayList<>();
        Articulo.cargarInventarioEnLista(listaArticulosOriginal);

        adapter = new InventarioRVAdapter(new ArrayList<>(listaArticulosOriginal), R.layout.view_info_producto);
        recyclerView.setAdapter(adapter);

        searchHandler = new Handler(Looper.getMainLooper());

        setupSearchView();
        ColocarBienvenida();

        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        listaArticulosOriginal.clear();
        Articulo.cargarInventarioEnLista(listaArticulosOriginal);

        // Create a new adapter instance to ensure clean state
        adapter = new InventarioRVAdapter(new ArrayList<>(listaArticulosOriginal), R.layout.view_info_producto);
        recyclerView.setAdapter(adapter);
        
        // Scroll to top
        recyclerView.scrollToPosition(0);

        if (searchView != null)
        {
            if (!searchView.getQuery().toString().isEmpty()) {
                searchView.setQuery("", false);
            }
            searchView.clearFocus();
            // Resetear el spinner de categorías
            if (spinnerCategoriaFiltro != null)
            {
                spinnerCategoriaFiltro.setSelection(0); // Seleccionar "Todas las categorías"
            }
        }

        ColocarBienvenida();
    }

    private void setupSearchView()
    {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                // Cancel any previous search
                if (searchHandler != null && searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                // Create new search runnable
                searchRunnable = () -> filterProducts(newText);
                
                // Delay the search to avoid too frequent updates
                searchHandler.postDelayed(searchRunnable, 300);
                
                return true;
            }
        });
    }
    //AGREGADO
    private void setupSpinnerCategoria()
    {
        // Crear array con "Todas las categorías" al inicio
        String[] categoriasArray = getResources().getStringArray(R.array.categorias_array);
        String[] categoriasConTodas = new String[categoriasArray.length + 1];
        categoriasConTodas[0] = "Todas las categorías";
        System.arraycopy(categoriasArray, 0, categoriasConTodas, 1, categoriasArray.length);

        // Configurar el adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                categoriasConTodas
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoriaFiltro.setAdapter(adapter);

        // Configurar el listener
        spinnerCategoriaFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                categoriaSeleccionada = parent.getItemAtPosition(position).toString();
                filterProducts(searchView.getQuery().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                categoriaSeleccionada = "Todas las categorías";
            }
        });
    }

    private void filterProducts(String query)
    {
        ArrayList<Articulo> filteredList = new ArrayList<>();

        if (query == null || query.trim().isEmpty())
        {
            // Si no hay búsqueda, filtrar solo por categoría
            for (Articulo articulo : listaArticulosOriginal)
            {
                if (articulo != null)
                {
                    if (categoriaSeleccionada.equals("Todas las categorías") ||
                            articulo.getCategoria().equals(categoriaSeleccionada))
                    {
                        filteredList.add(articulo);
                    }
                }
            }
        }
        else
        {
            // Si hay búsqueda, filtrar por texto Y categoría
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Articulo articulo : listaArticulosOriginal)
            {
                if (articulo != null)
                {
                    String descripcion = articulo.getDescripcion() != null ? articulo.getDescripcion().toLowerCase() : "";
                    String codigo = articulo.getCodigo() != null ? articulo.getCodigo().toLowerCase() : "";

                    boolean matchesText = descripcion.contains(lowerCaseQuery) || codigo.contains(lowerCaseQuery);
                    boolean matchesCategory = categoriaSeleccionada.equals("Todas las categorías") ||
                            articulo.getCategoria().equals(categoriaSeleccionada);

                    if (matchesText && matchesCategory)
                    {
                        filteredList.add(articulo);
                    }
                }
            }
        }

        // Create a new adapter instance to avoid ViewHolder recycling issues
        adapter = new InventarioRVAdapter(filteredList, R.layout.view_info_producto);
        recyclerView.setAdapter(adapter);

        // Scroll to top to show the filtered results
        recyclerView.scrollToPosition(0);

        ColocarBienvenida();
    }

    private void ColocarBienvenida()
    {
        if (adapter.getItemCount() == 0)
        {
            bienvenida.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
        else
        {
            bienvenida.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}



package com.teamihc.inventas.adapters.listaproductos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.teamihc.inventas.R;
import com.teamihc.inventas.backend.entidades.Articulo;

import java.util.ArrayList;

import static com.teamihc.inventas.backend.Herramientas.formatearMonedaDolar;
import static com.teamihc.inventas.backend.Herramientas.formatearMonedaBs;
import static com.teamihc.inventas.backend.Herramientas.formatearMonedaSoles;
import static com.teamihc.inventas.backend.Herramientas.getCompressedBitmapImage;

public abstract class ListaProductosRVAdapter
        extends RecyclerView.Adapter<ListaProductosRVAdapter.ListaProductosAdapter>
        implements View.OnClickListener, View.OnLongClickListener
{
    
    protected int layoutId;
    protected ArrayList<Articulo> listaArticulos;
    protected CardView cardView;

    public ListaProductosRVAdapter(ArrayList<Articulo> listaArticulos, int layoutId)
    {
        this.listaArticulos = new ArrayList<>();
        this.layoutId = layoutId;
        
        // Make a defensive copy of the input list
        if (listaArticulos != null) {
            for (Articulo articulo : listaArticulos) {
                if (articulo != null) {
                    this.listaArticulos.add(articulo);
                }
            }
        }
        
        // Enable stable IDs to help with ViewHolder recycling
        setHasStableIds(true);
    }
    
    @NonNull
    @Override
    public ListaProductosAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        return new ListaProductosAdapter(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ListaProductosAdapter holder, int position)
    {
        if (position >= 0 && position < listaArticulos.size()) {
            Articulo articulo = listaArticulos.get(position);
            if (articulo != null) {
                holder.asignarDatos(articulo);
            }
        }
    }
    
    @Override
    public int getItemCount()
    {
        return listaArticulos.size();
    }
    
    
    public class ListaProductosAdapter extends RecyclerView.ViewHolder
    {
        private View view;
        
        public ListaProductosAdapter(@NonNull View itemView)
        {
            super(itemView);
            view = itemView;
            cardView = (CardView) itemView.findViewById(R.id.info_producto);
        }
        
        public void asignarDatos(Articulo articulo)
        {
            if (articulo == null || cardView == null) {
                return;
            }
            
            // Clear all views first to prevent showing old data
            ImageView imagenProd = (ImageView) cardView.findViewById(R.id.imagenProd);
            TextView descripcion = (TextView) cardView.findViewById(R.id.descripcion);
            TextView precioBsS = (TextView) cardView.findViewById(R.id.precioBsS);
            TextView cantidadStock = (TextView) cardView.findViewById(R.id.cantidadStock);
            TextView costoD = (TextView) cardView.findViewById(R.id.costoD);
            TextView precioD = (TextView) cardView.findViewById(R.id.precioD);
            
            // Clear all text views first
            if (descripcion != null) descripcion.setText("");
            if (precioBsS != null) precioBsS.setText("");
            if (cantidadStock != null) cantidadStock.setText("");
            if (costoD != null) costoD.setText("");
            if (precioD != null) precioD.setText("");
            if (imagenProd != null) imagenProd.setImageResource(0);
            
            // Now set the new data
            if (imagenProd != null)
            {
                String imagePath = articulo.getImagen_path();
                if (imagePath != null && !imagePath.isEmpty()){
                    Glide.with(view).load(imagePath).into(imagenProd);
                }
            }
            if (descripcion != null)
            {
                String desc = articulo.getDescripcion();
                descripcion.setText(desc != null ? desc : "");
            }
            if (precioBsS != null)
            {
                precioBsS.setText(formatearMonedaSoles(articulo.getPrecioBs()));
            }
            if (cantidadStock != null)
            {
                cantidadStock.setText(String.valueOf(articulo.getCantidad()));
            }
            if (costoD != null)
            {
                costoD.setText(formatearMonedaSoles(articulo.getCosto()));
            }
            if (precioD != null)
            {
                precioD.setText(formatearMonedaSoles(articulo.getPrecio()));
            }
        }
    }

    @Override
    public long getItemId(int position) {
        // Return a unique ID for each item to help with ViewHolder recycling
        if (position >= 0 && position < listaArticulos.size()) {
            Articulo articulo = listaArticulos.get(position);
            if (articulo != null && articulo.getDescripcion() != null) {
                return articulo.getDescripcion().hashCode();
            }
        }
        return RecyclerView.NO_ID;
    }
    
    @Override
    public int getItemViewType(int position) {
        // Return a consistent view type
        return 0;
    }
    
    public void updateData(ArrayList<Articulo> newData) {
        // Clear and update the data
        listaArticulos.clear();
        if (newData != null) {
            listaArticulos.addAll(newData);
        }
        
        // Force a complete refresh to avoid ViewHolder inconsistencies
        notifyDataSetChanged();
    }
    
    public void setData(ArrayList<Articulo> newData) {
        // Create a completely new list to avoid any reference issues
        ArrayList<Articulo> oldData = new ArrayList<>(this.listaArticulos);
        this.listaArticulos = new ArrayList<>();
        if (newData != null) {
            this.listaArticulos.addAll(newData);
        }
        
        // Force ViewHolder recreation by using specific notifications
        int oldSize = oldData.size();
        int newSize = this.listaArticulos.size();
        
        if (oldSize > 0) {
            notifyItemRangeRemoved(0, oldSize);
        }
        if (newSize > 0) {
            notifyItemRangeInserted(0, newSize);
        }
        
        // As a fallback, also call notifyDataSetChanged
        notifyDataSetChanged();
    }
}

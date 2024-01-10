package com.example.a14kapp;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a14kapp.Entidades.Producto;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FavoritosFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private static String email;
    private DatabaseReference database;

    private View view;
    private RecyclerView recyclerView;
    private FavoritosAdapter adapter;
    private List<Producto> productList;

    public FavoritosFragment(String email) {
        this.email = email;
    }

    public static FavoritosFragment newInstance(String param1, String param2) {
        FavoritosFragment fragment = new FavoritosFragment(email);
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_favoritos, container, false);

        recyclerView = view.findViewById(R.id.fav_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        productList = new ArrayList<>();
        adapter = new FavoritosAdapter(productList, getContext());

        recyclerView.setAdapter(adapter);

        // Realizar la consulta a la subcolección "favoritos" del cliente mediante el email
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://kapp-7cf35-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference clienteRef = database.getReference("cliente");

        Query query = clienteRef.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Se encontró un cliente con el email especificado
                    DataSnapshot clienteSnapshot = dataSnapshot.getChildren().iterator().next();
                    String clientId = clienteSnapshot.getKey();

                    // Obtener la referencia a la subcolección "favoritos" del cliente
                    DatabaseReference favoritosRef = clienteRef.child(clientId).child("favoritos");

                    favoritosRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            productList.clear();
                            for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                                Producto product = productSnapshot.getValue(Producto.class);
                                productList.add(product);
                            }
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.w("Firebase", "Error al obtener los productos de favoritos", error.toException());
                        }
                    });
                } else {
                    // No se encontró ningún cliente con el email especificado
                    Log.d("Firebase", "No se encontró ningún cliente con el email especificado");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Error al realizar la consulta
                Log.w("Firebase", "Error al buscar al cliente", databaseError.toException());
            }
        });

        return view;
    }


    private static class FavoritosAdapter extends RecyclerView.Adapter<FavoritosAdapter.ProductViewHolder> {

        private List<Producto> productList;
        private Context context;

        public FavoritosAdapter(List<Producto> productList, Context context) {
            this.productList = productList;
            this.context = context;
        }

        public void setProductList(List<Producto> productList) {
            this.productList = productList;
        }

        @NonNull
        @Override
        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favoritos_item, parent, false);
            return new ProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
            Producto product = productList.get(position);
            holder.nombreTextView.setText(product.getNombre());
            holder.descripcionTextView.setText(product.getDescripcion());
            holder.precioTextView.setText(product.getPrecio());
            if (TextUtils.isEmpty(product.getImagen())) {
                holder.imagen.setImageResource(R.drawable.border_productos);
            } else {
                Picasso.get().load(product.getImagen()).into(holder.imagen);
            }

            holder.fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Obtener el nombre del producto actual
                    String nombreProducto = productList.get(holder.getAdapterPosition()).getNombre();

                    FirebaseDatabase database = FirebaseDatabase.getInstance("https://kapp-7cf35-default-rtdb.europe-west1.firebasedatabase.app/");
                    DatabaseReference clienteRef = database.getReference("cliente");

                    Query query = clienteRef.orderByChild("email").equalTo(email);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                DataSnapshot clienteSnapshot = dataSnapshot.getChildren().iterator().next();
                                String clientId = clienteSnapshot.getKey();

                                DatabaseReference favoritosRef = clienteRef.child(clientId).child("favoritos");

                                // Buscar el producto por su nombre y eliminarlo
                                favoritosRef.orderByChild("nombre").equalTo(nombreProducto).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                                            productSnapshot.getRef().removeValue();
                                            Toast.makeText(context, "Se ha eliminado el producto de favoritos", Toast.LENGTH_SHORT).show();
                                            holder.fav.setBackgroundResource(R.drawable.fav_0);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                            } else {}
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            return productList.size();
        }

        public static class ProductViewHolder extends RecyclerView.ViewHolder {

            private TextView nombreTextView, descripcionTextView, precioTextView;
            private ImageView imagen;
            public Button fav;

            public ProductViewHolder(@NonNull View itemView) {
                super(itemView);
                nombreTextView = itemView.findViewById(R.id.fav_nombre);
                descripcionTextView = itemView.findViewById(R.id.fav_descripcion);
                precioTextView = itemView.findViewById(R.id.fav_precio);
                imagen = itemView.findViewById(R.id.fav_imagen);
                fav = itemView.findViewById(R.id.boton_eliminar_favorito);

            }
        }
    }


}

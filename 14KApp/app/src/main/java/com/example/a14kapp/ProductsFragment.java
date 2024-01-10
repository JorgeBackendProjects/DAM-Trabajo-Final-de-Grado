package com.example.a14kapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.a14kapp.Entidades.Producto;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProductsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProductsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static String email;

    private View view;
    private DatabaseReference database;
    private DatabaseReference productos;
    private static DatabaseReference cliente;

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Producto> productList;

    public ProductsFragment(String email) {
        this.email = email;
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProductsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProductsFragment newInstance(String param1, String param2) {
        ProductsFragment fragment = new ProductsFragment(email);
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

        database = FirebaseDatabase.getInstance("https://kapp-7cf35-default-rtdb.europe-west1.firebasedatabase.app/").getReference("productos");
        productos = FirebaseDatabase.getInstance("https://kapp-7cf35-default-rtdb.europe-west1.firebasedatabase.app/").getReference("productos");
        cliente = FirebaseDatabase.getInstance("https://kapp-7cf35-default-rtdb.europe-west1.firebasedatabase.app/").getReference("cliente");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_products, container, false);

        //Inicializar RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView01);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //Inicializar lista de productos y adapter
        productList = new ArrayList<>();

        adapter = new ProductAdapter(productList, getContext(), requireActivity().getSupportFragmentManager());
        /*adapter.setAgregarProductoAFavoritosListener(new ProductAdapter.AgregarProductoAFavoritosListener() {
            @Override
            public void onAgregarProductoAFavoritos(Producto producto) {
                agregarProductoAFavoritos(getContext(), producto);
            }
        });*/

        recyclerView.setAdapter(adapter);

        //Obtener los productos de Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://kapp-7cf35-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference reference = database.getReference("productos");
        reference.addValueEventListener(new ValueEventListener() {
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
                Log.w("Firebase", "Error al obtener los productos", error.toException());
            }
        });

        return view;
    }

    public static class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

        private List<Producto> productList;
        private Context context;
        private static FragmentManager fragmentManager;
        private Drawable imagenCorazon;

        public ProductAdapter(List<Producto> productList, Context context, FragmentManager fragmentManager) {
            this.productList = productList;
            this.context = context;
            this.fragmentManager = fragmentManager;
        }

        private AgregarProductoAFavoritosListener listener;

        public interface AgregarProductoAFavoritosListener {
            void onAgregarProductoAFavoritos(Producto producto);
        }

        public void setAgregarProductoAFavoritosListener(AgregarProductoAFavoritosListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.producto_item, parent, false);
            return new ProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductViewHolder holder, @SuppressLint("RecyclerView") int position) {
            Producto product = productList.get(position);
            holder.nombreTextView.setText(product.getNombre());
            holder.descriptionTextView.setText(product.getDescripcion());
            holder.precioTextView.setText(product.getPrecio());
            if (TextUtils.isEmpty(product.getImagen())) {
                holder.imageView.setImageResource(R.drawable.border_productos);
            } else {
                Picasso.get().load(product.getImagen()).into(holder.imageView);
            }


            //COMPRUEBA SI EL PRODUCTO ESTÁ EN FAVORITOS DEL CLIENTE PARA DARLE LOS BACKGROUND CORRECTOS AL BOTÓN ANTES DE CARGAR EL PRODUCTO A LA LISTA.
            Query query = cliente.orderByChild("email").equalTo(email);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        DataSnapshot clienteSnapshot = dataSnapshot.getChildren().iterator().next();
                        String clientId = clienteSnapshot.getKey();

                        DatabaseReference productoRef = cliente.child(clientId).child("favoritos").child(product.getNombre());
                        productoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // El producto está en favoritos
                                    holder.fav.setBackgroundResource(R.drawable.fav_1);
                                } else {
                                    // El producto no está en favoritos
                                    holder.fav.setBackgroundResource(R.drawable.fav_0);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                        });
                    } else {}
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });

            //ESTE MÉTODO TIENE QUE IR SUELTO PORQUE NO SE PUEDE REFERENCIAR EL BOTÓN EN OTRA PARTE.
            holder.fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    DatabaseReference clienteRef = FirebaseDatabase.getInstance("https://kapp-7cf35-default-rtdb.europe-west1.firebasedatabase.app/").getReference("cliente");
                    Query query = clienteRef.orderByChild("email").equalTo(email);

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                DataSnapshot clienteSnapshot = dataSnapshot.getChildren().iterator().next();
                                String clienteId = clienteSnapshot.getKey();
                                DatabaseReference favoritosRef = clienteRef.child(clienteId).child("favoritos");

                                favoritosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.child(product.getNombre()).exists()) {
                                            // El producto está en favoritos, se debe eliminar
                                            favoritosRef.child(product.getNombre()).removeValue()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(context, "Producto eliminado de favoritos", Toast.LENGTH_SHORT).show();
                                                            // Actualizar la interfaz de usuario para reflejar el cambio
                                                            holder.fav.setBackgroundResource(R.drawable.fav_0);
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(context, "Error al eliminar el producto de favoritos", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            // El producto no está en favoritos, se debe agregar
                                            favoritosRef.child(product.getNombre()).setValue(product)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(context, "Producto agregado a favoritos", Toast.LENGTH_SHORT).show();
                                                            // Actualizar la interfaz de usuario para reflejar el cambio
                                                            holder.fav.setBackgroundResource(R.drawable.fav_1);
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(context, "Error al agregar el producto a favoritos", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(context, "Error al obtener el estado del producto en favoritos", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            } else {
                                Toast.makeText(context, "No se encontró ningún cliente con el email especificado", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(context, "Error al buscar al cliente", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        }

        @Override
        public int getItemCount() {
            return productList.size();
        }

        public static class ProductViewHolder extends RecyclerView.ViewHolder {

            public ImageView imageView;
            public TextView nombreTextView, descriptionTextView, precioTextView;
            public Button fav;

            public ProductViewHolder(@NonNull View itemView) {
                super(itemView);
                nombreTextView = itemView.findViewById(R.id.nombre_item_P);
                descriptionTextView = itemView.findViewById(R.id.descripcion_item_P);
                precioTextView = itemView.findViewById(R.id.precio_item_P);
                imageView = itemView.findViewById(R.id.product_image);
                fav = itemView.findViewById(R.id.botonFavorito);
            }
        }
    }


}
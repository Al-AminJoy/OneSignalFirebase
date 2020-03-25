package com.trustedoffer.onesignalfirebase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private Context context;
    private ArrayList<ProductModelClass> datalist;
    private FirebaseStorage storageReference;
    private DatabaseReference databaseReference;

    public ProductAdapter(Context context, ArrayList<ProductModelClass> datalist) {
        this.context = context;
        this.datalist = datalist;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.product_layout, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, final int position) {
        final ProductModelClass data = datalist.get(position);
        storageReference = FirebaseStorage.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("product");

        holder.tvTitle.setText(data.getTitle());
        holder.tvPrice.setText(String.valueOf(data.getPrice()));
        Picasso.with(context).load(data.getImage()).fit().into(holder.ivProductImage);
        //For Delete Data From Recyclerview
        holder.btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String key = data.getKey();
                StorageReference reference = storageReference.getReferenceFromUrl(data.getImage());
                reference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        databaseReference.child(key).removeValue();
                        Toast.makeText(context, "File Removed", Toast.LENGTH_SHORT).show();
                        //Remove From ArrayList
                        datalist.remove(position);
                        notifyDataSetChanged();
                    }
                });
            }
        });
        //For Update Data From Recyclerview
        holder.btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String key = data.getKey();
                final String image = data.getImage();
                databaseReference.child(key)
                        .setValue(new ProductModelClass(key, "Updated", 1000, image))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Data Updated", Toast.LENGTH_SHORT).show();
                                //Update In ArrayList
                                datalist.set(position, new ProductModelClass(key, "Updated", 1000, image));
                                notifyDataSetChanged();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
        //This Is For Without Image
        //Same as Update
       /* holder.btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key=data.getKey();
                databaseReference.child(key)
                        .removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context,"Data Deleted",Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,"Failed",Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });*/

    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvPrice;
        private ImageView ivProductImage;
        private Button btDelete, btUpdate;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvProductLayTitle);
            tvPrice = itemView.findViewById(R.id.tvProductLayPrice);
            ivProductImage = itemView.findViewById(R.id.ivProductLayProductImage);
            btDelete = itemView.findViewById(R.id.btProductLayDeleteButtonId);
            btUpdate = itemView.findViewById(R.id.btProductLayUpdateButtonId);
        }
    }

}

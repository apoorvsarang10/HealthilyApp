package com.application.healthapp.healthily;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {
    private Button button;
    private StorageReference storageReference;
    private TextView tvName;
    private TextView tvGoal;
    private TextView tvHeight;
    private TextView tvWeight;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;
    private CircleImageView setupImage;



    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        tvName = getView().findViewById(R.id.profileName);
        tvGoal = getView().findViewById(R.id.profileGoal);
        tvHeight = getView().findViewById(R.id.profileHeight);
        tvWeight = getView().findViewById(R.id.profileWeight);
        setupImage = getView().findViewById(R.id.setup_image);

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task){
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        Log.d("TAG", document.getString("name")); //Print the name
                        String s = document.get("name").toString(); //Print name
                        String v = document.get("goal").toString(); //Print goal
                        String m = document.get("weight").toString(); //Print Weight
                        String k = document.get("height").toString(); //Print Height
                        String image = task.getResult().getString("image");
                        tvName.setText(s);
                        tvGoal.setText(v);
                        tvWeight.setText(m);
                        tvHeight.setText(k);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.default_image);

                        Glide.with(AccountFragment.this).setDefaultRequestOptions(placeholderRequest).load(image).into(setupImage);
                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }

            }
        });




    }

}

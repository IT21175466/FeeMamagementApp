package com.example.scienceclass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddStudent extends AppCompatActivity {

    EditText sName, sPhoneNumber, sClass, sLastPaidDate;
    AppCompatButton add;

    ProgressDialog pd;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        sName = findViewById(R.id.student_Name);
        sPhoneNumber = findViewById(R.id.phone_Number);
        sClass = findViewById(R.id.student_Class);
        sLastPaidDate = findViewById(R.id.student_LastPDate);

        add = findViewById(R.id.add_to_DB);

        pd = new ProgressDialog(this);

        db = FirebaseFirestore.getInstance();

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = sName.getText().toString().trim();
                String phoneNum = sPhoneNumber.getText().toString().trim();
                String student_Class = sClass.getText().toString().trim();
                String lastPaidDate = sLastPaidDate.getText().toString().trim();

                uploadData(name, phoneNum, student_Class, lastPaidDate);
            }
        });
    }

    private void uploadData(String name, String phoneNum, String student_class, String lastPaidDate) {
        pd.setTitle("Adding Student...");
        pd.show();

        String id = UUID.randomUUID().toString();

        Map<String, Object> doc = new HashMap<>();
        doc.put("id", id);
        doc.put("name", name);
        doc.put("phoneNumber", phoneNum);
        doc.put("stdClass", student_class);
        doc.put("lastPaidDate", lastPaidDate);

        db.collection("Students").document(id).set(doc)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        pd.dismiss();
                        Toast.makeText(AddStudent.this,"Student Added Success!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddStudent.this,"Student Add Failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
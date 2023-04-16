package com.example.scienceclass;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScanActivity extends AppCompatActivity {

    TextView name, phone, stdClass, lDate, tvSetPaidStatus, tvDates, dina;
    AppCompatButton scan, setTOMONTH, tvdelete_state, addDate, send_whatsapp;
    EditText enterId, enterDates;

    FirebaseFirestore db;

    ProgressDialog progressDialog;

    Calendar calendar;

    Months months;

    String strMessage, strMobileNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        name = findViewById(R.id.scanedName);
        phone = findViewById(R.id.scanedNumber);
        stdClass = findViewById(R.id.scanedClass);
        lDate = findViewById(R.id.scanedLDate);
        addDate = findViewById(R.id.add_date);
        setTOMONTH = findViewById(R.id.setTOMONTH);
        send_whatsapp = findViewById(R.id.send_whatsapp);

        dina = findViewById(R.id.dina);

        tvSetPaidStatus = findViewById(R.id.tvSetPaidStatus);
        tvDates = findViewById(R.id.tvDates);
        tvdelete_state = findViewById(R.id.delete_state);

        enterId = findViewById(R.id.enterID);
        enterDates = findViewById(R.id.enterDates);

        scan = findViewById(R.id.scan);

        db = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(this);

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //startActivity(new Intent(ScanActivity.this, ScanView.class));
                tvSetPaidStatus.setText("");
                tvDates.setText("");
                enterDates.setText("");
                scanCode();

                //String id = enterId.toString().trim();
                //feachData(id);

            }
        });

        addDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enterDate = enterDates.getText().toString().trim();
                //String status = setASPaid.getText().toString().trim();


                uploadDate(enterDate);
                feachDate(enterId.getText().toString().trim());

            }
        });

        setTOMONTH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mDate = enterDates.getText().toString();
                //String mIsPaid = setASPaid.getText().toString();
                addMonth(mDate);

                addMonthCount(mDate);
                enterDates.setText("");

                tvSetPaidStatus.setText("NOT PAID");


            }
        });

        send_whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvSetPaidStatus.setText("");

                calendar = Calendar.getInstance();
                String currentDate = DateFormat.getDateInstance().format(calendar.getTime());

                sendWhatsapp(currentDate);

            }
        });

        tvdelete_state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDates();

                tvDates.setText("Paid Success!");

                calendar = Calendar.getInstance();
                String currentDate = DateFormat.getDateInstance().format(calendar.getTime());
                lDate.setText(currentDate);

                addLastPDateToDatabase(currentDate);

            }
        });

    }

    public void feachData(String id){

        progressDialog.setTitle("Getting Student....");
        progressDialog.show();

        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Students").document(id);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    progressDialog.dismiss();
                    name.setText(documentSnapshot.getString("name"));
                    phone.setText(documentSnapshot.getString("phoneNumber"));
                    stdClass.setText(documentSnapshot.getString("stdClass"));
                    lDate.setText(documentSnapshot.getString("lastPaidDate"));

                    Toast.makeText(ScanActivity.this,"Student got Success!", Toast.LENGTH_SHORT).show();

                }else {
                    Toast.makeText(ScanActivity.this,"Student not Found!", Toast.LENGTH_SHORT).show();
                }

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ScanActivity.this,"Student got Failed!", Toast.LENGTH_SHORT).show();

                    }
                });

    }

    public void scanCode(){
        ScanOptions options = new ScanOptions();
        //options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLuncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLuncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null){

            enterId.setText(result.getContents());
            String id = enterId.getText().toString().trim();
            feachData(id);

            AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
            builder.setTitle("සිසුවාගේ විස්තර ලබාගැනීම සාර්ථකයි!");
            builder.setPositiveButton("ඉදිරියට යමු.", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    feachDate(id);
                    feachCardData(id);
                }
            }).show();

            //System.out.println(enterId.getText().toString().trim());


            /*AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
            builder.setTitle("result");
            builder.setMessage(result.getContents());
           builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialogInterface, int i) {
                   dialogInterface.dismiss();
               }
           }).show();*/
        }
    });
    private void uploadDate(String name) {

        progressDialog.setTitle("Adding Date....");
        progressDialog.show();

        String sid = enterId.getText().toString().trim();
        String isPaid = "NOT PAID";

        Map<String, Object> doc = new HashMap<>();
        doc.put("sid", sid);
        doc.put("name", name);
        doc.put("status", isPaid);

        db.collection("Dates").document(sid).set(doc)
                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        Toast.makeText(ScanActivity.this,"Update Success!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ScanActivity.this,"Update Failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void feachDate(String id){

        progressDialog.setTitle("Getting Dates....");
        progressDialog.show();

        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Dates").document(id);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){
                            progressDialog.dismiss();
                            enterDates.setText(documentSnapshot.getString("name"));

                            Toast.makeText(ScanActivity.this,"Success!", Toast.LENGTH_SHORT).show();

                        }else {
                            progressDialog.dismiss();
                            Toast.makeText(ScanActivity.this,"Failed", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ScanActivity.this,"Failed!", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void addMonth(String date) {

        progressDialog.setTitle("Updating....");
        progressDialog.show();

        String ssid = enterId.getText().toString().trim();
        String isPaid = "NOT PAID";

        Map<String, Object> doc = new HashMap<>();
        doc.put("sid", ssid);
        doc.put("name", date);
        doc.put("status", isPaid);

        db.collection("Months").document(ssid).set(doc)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        Toast.makeText(ScanActivity.this,"Update Success!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ScanActivity.this,"Update Failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addMonthCount(String date) {

        progressDialog.setTitle("Adding Month....");
        progressDialog.show();

        String ssid = enterId.getText().toString().trim();
        String isPaid = "NOT PAID";

        Map<String, Object> doc = new HashMap<>();
        doc.put("smid", ssid);
        doc.put("name", date);
        doc.put("status", isPaid);

        //db.collection("rooms").document("roomA").collection("messages").document("message1");

        db.collection("Months").document(ssid).collection("MonthCount").document(ssid).set(doc)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        Toast.makeText(ScanActivity.this,"Success!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ScanActivity.this,"Failed!", Toast.LENGTH_SHORT).show();
                    }
                });

        feachCardData(ssid);

    }

    public void feachCardData(String idf){

        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Months").document(idf).collection("MonthCount").document(idf);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){
                            progressDialog.dismiss();
                            tvDates.setText(documentSnapshot.getString("name"));
                            tvSetPaidStatus.setText(documentSnapshot.getString("status"));

                            Toast.makeText(ScanActivity.this,"Success!", Toast.LENGTH_SHORT).show();

                        }else {
                            Toast.makeText(ScanActivity.this,"Found!", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ScanActivity.this,"Student got Failed!", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    public void deleteDates(){

        String sid = enterId.getText().toString().trim();

        db.collection("Months").document(sid).collection("MonthCount").document(sid).delete();

    }

    public void addLastPDateToDatabase(String cDate){

        String sid = enterId.getText().toString().trim();

        db.collection("Students").document(sid).update("lastPaidDate", cDate);
    }

    public void sendWhatsapp(String dateToday){

        strMessage = "Name of the Student : "+name.getText().toString()+"\n"+"Paid For Below Dates"+"\n"+tvDates.getText().toString()+"\n"+"Paid Date : "+dateToday+"\n"+ "Jinadasa Widyapathi (Science Class)"+"\n";
        System.out.println(strMessage);
        strMobileNumber = phone.getText().toString().trim();

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=" + strMobileNumber
                                                                + "&text=" + strMessage));
        startActivity(intent);
    }


}
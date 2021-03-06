package com.lee.woosuk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.lee.woosuk.Adapters.ChatAdapter;
import com.lee.woosuk.Adapters.ImgAdapter;
import com.lee.woosuk.DTOs.ChatDTO;
import com.lee.woosuk.DTOs.ImgDTO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private final int REQ_CODE = 1234;
    private Uri filePath;
    private ImageView imgPreview;

    private RelativeLayout layout;

    private InputMethodManager imm;

    private String CHAT_NAME;
    private String USER_NAME;

    private ListView chat_view;
    private ChatAdapter adapter;
    private ImgAdapter imgadapter;

    private EditText chat_edit;
    private Button chat_send, backbtn, addbtn;
    private ImageButton sttbtn;
    private TextView room_title;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    //?????? ??????, ??????
    Date today = new Date();
    SimpleDateFormat today_file_format = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
    SimpleDateFormat timeNow = new SimpleDateFormat("a K:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

        // ?????? ID ??????
        layout = (RelativeLayout) findViewById(R.id.layout);
        chat_view = (ListView) findViewById(R.id.chat_view);
        chat_edit = (EditText) findViewById(R.id.chat_edit);
        chat_send = (Button) findViewById(R.id.chat_sent);
        sttbtn = (ImageButton) findViewById(R.id.stt_btn);
        room_title = (TextView) findViewById(R.id.room_title);
        backbtn = (Button) findViewById(R.id.backbtn);
        addbtn = (Button) findViewById(R.id.addbtn);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);

        // ????????? ??????(MainActivity)?????? ????????? ????????? ??????, ?????? ?????? ??????
        Intent intent = getIntent();
        CHAT_NAME = intent.getStringExtra("chatName");
        USER_NAME = intent.getStringExtra("userName");

        // ?????? ??? ??????
        openChat(CHAT_NAME);
        openimg(CHAT_NAME);
        room_title.setText(CHAT_NAME + " ????????? ?????????.");

        //STT ??????
        sttbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartSTT();
            }
        });

        // ????????? ?????? ????????? ?????? ?????? ????????? ??????
        chat_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chat_edit.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "???????????? ?????? ??????????????????.", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (filePath != null) {
                    uploadFile();

                    //????????? ????????? ??? ????????? ???????????? ????????? ????????? ??????
                    imgPreview.getLayoutParams().width = 0;
                    imgPreview.getLayoutParams().height = 0;
                    imgPreview.requestLayout();


                }

                ChatDTO chat = new ChatDTO(USER_NAME, chat_edit.getText().toString(), timeNow.format(today)); //ChatDTO??? ???????????? ???????????? ?????????.
                databaseReference.child("chat").child(CHAT_NAME).push().setValue(chat); // ????????? ????????? ??????
                chat_edit.setText(""); //????????? ?????????

                imm.hideSoftInputFromWindow(chat_edit.getWindowToken(), 0); // SEND?????? ????????? ????????? ?????? ?????????

                new Handler().postDelayed(new Runnable() { // ????????? ??????x ???????????? ?????? -> ??????: ?????????????????? ??????.
                    @Override
                    public void run() {
                        chat_view.smoothScrollToPosition(adapter.getCount()-1); // ????????? ???????????? ?????? ??????????????? ???????????? ????????? ?????????
                    }
                }, 100);

            }
        });


        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        switch(pref.getString("chat_theme", "")){
            case "red" :
                layout.setBackgroundResource(R.drawable.background_chat_red);
                backbtn.setBackgroundResource(R.drawable.button_back_chat_red);
                addbtn.setBackgroundResource(R.drawable.button_add_chat_red);
                sttbtn.setBackgroundResource(R.drawable.clickbtn_chat_red);
                chat_send.setBackgroundResource(R.drawable.clickbtn_chat_red);
                break;
            case "blue" :
                layout.setBackgroundResource(R.drawable.background_chat_blue);
                backbtn.setBackgroundResource(R.drawable.button_back_chat_blue);
                addbtn.setBackgroundResource(R.drawable.button_add_chat_blue);
                sttbtn.setBackgroundResource(R.drawable.clickbtn_chat_blue);
                chat_send.setBackgroundResource(R.drawable.clickbtn_chat_blue);
                break;
            case "green" :
                layout.setBackgroundResource(R.drawable.background_chat_green);
                backbtn.setBackgroundResource(R.drawable.button_back_chat_green);
                addbtn.setBackgroundResource(R.drawable.button_add_chat_green);
                sttbtn.setBackgroundResource(R.drawable.clickbtn_chat_green);
                chat_send.setBackgroundResource(R.drawable.clickbtn_chat_green);
                break;
        }


        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

    private void StartSTT() {
        //onActivityResult ?????? ?????? ?????? 323~
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //??????????????? ????????? ?????? ?????? ?????? ?????? STT ???????????? ??????
        //ex) ??????????????? ??????????????? ???????????? -> STT ???????????? ?????? ????????? ??????
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"???????????? ??? ...");
        //intent.putExtra ??????(?????? ????????? ???) ???????????? ??????(int??? ?????? REQ_CODE)??? ??????
        //??????????????? ???????????? ?????? ???????????? ??????????????? ?????? int ????????? ????????? ???????????? ??????
        //??? ??????????????? STT??? ????????? ??????????????? 1???????????? ??? ????????? ?????? (??? ????????? ????????? ?????? "1234"??? ??????)
        startActivityForResult(intent, REQ_CODE);
    }

    /*
    private void addMessage(DataSnapshot dataSnapshot, ArrayAdapter<String> adapter) {
        ChatDTO value = dataSnapshot.getValue(ChatDTO.class);
        adapter.add(value.getUserName() + " : " + value.getMessage());
    }
    */
    private void openChat(final String chatName) {


        // ????????? ????????? ?????? ??? ??????
        adapter = new ChatAdapter();
        chat_view.setAdapter(adapter); // ????????? ?????? ????????? ??????

        // ????????? ???????????? ??? ????????? ????????? ?????? ??? ?????? ???..????????? ??????
        databaseReference.child("chat").child(chatName).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {


                //addMessage(dataSnapshot, adapter);
                ChatDTO value = dataSnapshot.getValue(ChatDTO.class);
                adapter.addItem(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon), value.getUserName(), "  " + value.getMessage() + "  ", value.getTime());

                chat_view.smoothScrollToPosition(adapter.getCount()-1); // ????????? ???????????? ?????? ??????????????? ???????????? ????????? ?????????

                adapter.notifyDataSetChanged();//?????????(????????????) ?????? ????????????

                //Log.e("LOG", "s:"+s);

                addbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupMenu popup = new PopupMenu(getApplicationContext(), view);

                        getMenuInflater().inflate(R.menu.option_menu, popup.getMenu());

                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                switch (menuItem.getItemId()){
                                    case R.id.m1:
                                        //???????????? ??????
                                        Intent intent = new Intent();
                                        intent.setType("image/*");
                                        intent.setAction(Intent.ACTION_GET_CONTENT);
                                        //????????? ?????????????????? ????????????
                                        startActivityForResult(Intent.createChooser(intent, "???????????? ???????????????."), 0);
                                        break;
                                    case R.id.m2: //?????? ?????? ??????
                                        databaseReference.child("chat").child(chatName).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                //????????? ????????? ?????? ????????? ?????????????????? String??? ??????
                                                //replaceAll ??? ???????????? ?????? ???????????? ????????? ?????? ????????? ????????? ??????????????????.

                                                String replace = "????????? ?????? : "+chatName + dataSnapshot.getValue().toString();
                                                String replace1 = replace.replaceAll(", ", "\n");
                                                String replace2 = replace1.replaceAll("\\{-", "\n\n"); //chatName
                                                String replace3 = replace2.replaceAll("=\\{", "\n"); //time??????
                                                String replace4 = replace3.replaceAll("\\}", "\n");//??????????????????

                                                //?????? ?????????(External Storage)??? ?????????(??????) ????????? ??? ??????
                                                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                                                    //Environment.DIRECTORY_DOWNLOADS - ????????? ?????? ???????????? ??????
                                                    //???????????? ????????? ????????????+?????? ???????????? txt ?????? ??????
                                                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), today_file_format.format(today) + ".txt");
                                                    try{
                                                        FileWriter fw = new FileWriter(file, false);
                                                        fw.write(replace4);
                                                        fw.close();
                                                    } catch (IOException e){
                                                        e.printStackTrace();
                                                        Toast.makeText(getApplicationContext(),"ERROR",Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                                else{
                                                    Toast.makeText(getApplicationContext(),"ERROR",Toast.LENGTH_SHORT).show();
                                                }
                                            }


                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                        Toast.makeText(getApplicationContext(),"?????? ?????? ?????? ??????!",Toast.LENGTH_LONG).show();
                                        break;
                                    default:
                                        break;
                                }
                                return false;
                            }
                        });
                        popup.show();
                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    //?????? ??????, ???????????? A?????? B??? ????????? ?????? A??? ????????? ??? ???????????? ?????????
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //request????????? 0?????? OK??? ???????????? data??? ????????? ?????? ?????????
        if(requestCode == 0 && resultCode == RESULT_OK){ // RESULT_OK = ????????? ?????????????????? ?????? ?????? (???, ??????????????? ?????? ????????? ???????????????..)
            filePath = data.getData();
            //Log.d(TAG, "uri:" + String.valueOf(filePath));
            try {
                //Uri ????????? Bitmap?????? ???????????? ImageView??? ?????? ?????????.
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imgPreview.setImageBitmap(bitmap);
                imgPreview.getLayoutParams().width = 200;
                imgPreview.getLayoutParams().height = 400;
                imgPreview.requestLayout();
                chat_edit.setText("????????? ??????");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //STT?????? ??????
        switch (requestCode) {
            case REQ_CODE: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    chat_edit.setText(result.get(0));
                }
                break;
            }

        }
    }

    //upload the file ???????????????
    private void uploadFile() {
        //???????????? ????????? ????????? ??????
        if (filePath != null) {
            //????????? ?????? Dialog ?????????
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("????????????...");
            progressDialog.show();

            //????????? ??????
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_mmss");
            Date now = new Date();
            final String filename = formatter.format(now) + ".png";

            //?????????????????? ?????? storage
            final FirebaseStorage storage = FirebaseStorage.getInstance();

            //storage ????????? ?????? ???????????? ????????? ??????.
            final StorageReference storageRef = storage.getReferenceFromUrl("gs://macos-dceae.appspot.com").child("images/" + filename);

            storageRef.putFile(filePath)
                    //?????????
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss(); //????????? ?????? Dialog ?????? ??????

                            Toast.makeText(getApplicationContext(), "????????? ??????!", Toast.LENGTH_SHORT).show();

                            //Glide ?????? ??????
                            RequestOptions requestOptions = new RequestOptions()
                                    // ?????? ?????? ??????, ?????????????????? ?????? ??? ????????? ????????? ??????
                                    .skipMemoryCache(true)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE);

                            //?????? ??????????????? ???????????? ????????? Url??? ?????? ????????? ?????? Glide??? ????????? ????????????.
                            storageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if(task.isSuccessful()){
                                        ImgDTO chat = new ImgDTO(USER_NAME, chat_edit.getText().toString(), timeNow.format(today), task.getResult().toString()); //ImgDTO??? ???????????? ???????????? ?????????.
                                        databaseReference.child("chat").child(CHAT_NAME).push().setValue(chat); // ????????? ????????? ??????
                                        chat_view.smoothScrollToPosition(adapter.getCount()-1); // ????????? ???????????? ?????? ??????????????? ???????????? ????????? ?????????
                                        imgadapter.notifyDataSetChanged();//?????????(????????????) ?????? ????????????
                                        filePath = null; //????????? ?????? ??? filePath ?????? ????????? ??? ?????????
                                    }
                                }
                            });

                        }
                    })
                    //?????????
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "????????? ??????!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    //?????????
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            @SuppressWarnings("VisibleForTests") //?????? ?????? ?????? ???????????? ????????? ????????????.
                            double progress = (100 * taskSnapshot.getBytesTransferred()) /  taskSnapshot.getTotalByteCount();
                            //dialog??? ???????????? ???????????? ????????? ??????
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "% ...");
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openimg (String CHAT_NAME){
        imgadapter = new ImgAdapter();
        chat_view.setAdapter(imgadapter);
        databaseReference.child("chat").child(CHAT_NAME).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ImgDTO value = dataSnapshot.getValue(ImgDTO.class);
                imgadapter.addimg(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon), value.getUserName(), "  " + value.getMessage() + "  ", value.getTime(), value.getImg());

                new Handler().postDelayed(new Runnable() { // ????????? ??????x ???????????? ?????? -> ??????: ?????????????????? ??????.
                    @Override
                    public void run() {
                        chat_view.smoothScrollToPosition(adapter.getCount()-1); // ????????? ???????????? ?????? ??????????????? ???????????? ????????? ?????????
                    }
                }, 200);

                imgadapter.notifyDataSetChanged();//?????????(????????????) ?????? ????????????

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
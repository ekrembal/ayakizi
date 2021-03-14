package com.example.ayakizi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;




public class MainActivity extends AppCompatActivity {
    private SurfaceView surfaceView;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    //This class provides methods to play DTMF tones
    private ToneGenerator toneGen1;
    private TextView co_ayakizi_tv, h2o_ayakizi_tv, urun_adi_tv;
    private ImageView co_image, h2o_image;
    private String barcodeData;


    class Urun {
        String barkod_no = "0";
        String co_ayakizi = "CO2 ayak izi bilgisi bulunamadı.";
        String h2o_ayakizi = "H2O ayak izi bilgisi bulunamadı.";
        String urun_adi = "Ürün adı bilgisi bulunamadı.";
        public Urun(String yeni_barkod_no, String yeni_urun_adi, String yeni_co_ayakizi, String yeni_h2o_ayakizi) {
            barkod_no = yeni_barkod_no;
            urun_adi = yeni_urun_adi;
            co_ayakizi = yeni_co_ayakizi;
            h2o_ayakizi = yeni_h2o_ayakizi;
        }

        public void yazdir(){
            if(co_ayakizi.equals("NULL") || urun_adi.equals("NULL") || h2o_ayakizi.equals("NULL")){
                error();
                return;
            }
            co_ayakizi_tv.setVisibility(View.VISIBLE);
            h2o_ayakizi_tv.setVisibility(View.VISIBLE);
            co_image.setVisibility(View.VISIBLE);
            h2o_image.setVisibility(View.VISIBLE);
            urun_adi_tv.setText(urun_adi);
            co_ayakizi_tv.setText(co_ayakizi + " kg");
            h2o_ayakizi_tv.setText(h2o_ayakizi + " L");
        }

        public void error(){
            co_ayakizi_tv.setVisibility(View.INVISIBLE);
            h2o_ayakizi_tv.setVisibility(View.INVISIBLE);
            co_image.setVisibility(View.INVISIBLE);
            h2o_image.setVisibility(View.INVISIBLE);
            urun_adi_tv.setText("Bu ürün bulunamadı.");
//            Toast.makeText(getApplicationContext(), "Error var", Toast.LENGTH_LONG).show();

        }
    }


    String base_url ="http://161.35.152.160/barkod/";
    String url ="";
    HashMap<String, Urun> foundBarcodes = new HashMap<String, Urun>();

//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC,     100);
        surfaceView = findViewById(R.id.surface_view);
        co_ayakizi_tv = findViewById(R.id.co_ayakizi_tv);
        h2o_ayakizi_tv = findViewById(R.id.h2o_ayakizi_tv);
        urun_adi_tv = findViewById(R.id.urun_adi_tv);
        co_image = findViewById(R.id.co2_image);
        h2o_image = findViewById(R.id.h2o_image);


        initialiseDetectorsAndSources();
    }

    private void initialiseDetectorsAndSources() {
        RequestQueue queue = Volley.newRequestQueue(this);


        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            Date date1 = new Date();
            Date date2 = new Date();
            Urun urun = new Urun("0", "NULL", "NULL", "NULL");



            @Override
            public void release() {
                // Toast.makeText(getApplicationContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                date2 = new Date();
                if (barcodes.size() != 0 && ((int) (date2.getTime() - date1.getTime()))/100 > 10) {
                    date1 = new Date();
                    barcodeData = barcodes.valueAt(0).displayValue;
                    if(barcodeData != urun.barkod_no) {
                        if (foundBarcodes.containsKey(barcodeData)) {
                            urun = foundBarcodes.get(barcodeData);
                            urun.yazdir();
                        } else {
                            url = base_url + barcodeData;
                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {

                                            try {
                                                urun = new Urun(barcodeData,(String) response.get("urun"), (String) response.get("co2"), (String) response.get("su"));
                                            } catch (JSONException e) {
                                                urun.error();
                                            }
                                            urun.yazdir();
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    urun.error();
                                }
                            });

                            queue.add(jsonObjectRequest);
                        }

                        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                    }

                }
            }
        });
    }
}
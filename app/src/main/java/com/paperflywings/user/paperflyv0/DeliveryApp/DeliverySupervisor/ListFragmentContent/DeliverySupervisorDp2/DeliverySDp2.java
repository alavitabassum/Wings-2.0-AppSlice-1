package com.paperflywings.user.paperflyv0.DeliveryApp.DeliverySupervisor.ListFragmentContent.DeliverySupervisorDp2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.paperflywings.user.paperflyv0.Config;
import com.paperflywings.user.paperflyv0.Databases.BarcodeDbHelper;
import com.paperflywings.user.paperflyv0.DeliveryApp.DeliverySupervisor.DeliverySuperVisorLandingPage.DeliverySuperVisorTablayout;
import com.paperflywings.user.paperflyv0.NetworkStateChecker;
import com.paperflywings.user.paperflyv0.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeliverySDp2  extends AppCompatActivity {

    BarcodeDbHelper db;
    private DecoratedBarcodeView barcodeView;
    private BeepManager beepManager;
    public String lastText;
    public String barcode;
    private Button done;
    private int Counter = 0;
    private TextView scanCount,scanCountText;

    public static final int NAME_SYNCED_WITH_SERVER = 1;
    public static final int NAME_NOT_SYNCED_WITH_SERVER = 0;
    private NotificationManagerCompat notificationManager;

    //a broadcast to know weather the data is synced or not
    public static final String UPDATE_DP2_RECEIVE = "http://paperflybd.com/DeliverySupervisorAPI.php";
    //public static final String DATA_SAVED_BROADCAST = "net.simplifiedcoding.datasaved";

    // //Broadcast receiver to know the sync status
    private BroadcastReceiver broadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.continuous_scan_quick_delivery_pick);
        scanCountText = findViewById(R.id.scanCountTitle);
        scanCount = findViewById(R.id.scanCount);

        notificationManager = NotificationManagerCompat.from(this);

        barcodeView = findViewById(R.id.barcode_scanner);
        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.UPC_A);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        barcodeView.initializeFromIntent(getIntent());
        barcodeView.decodeContinuous(callback);

        beepManager = new BeepManager(this);

        //the broadcast receiver to update sync status
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };
        //registering the broadcast receiver to update sync status
        // registerReceiver(broadcastReceiver, new IntentFilter(DATA_SAVED_BROADCAST));
    }

    private BarcodeCallback callback = new BarcodeCallback()  {
        @Override
        public void barcodeResult(BarcodeResult result) {
            //Fetching email from shared preferences
            SharedPreferences sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
            final String username = sharedPreferences.getString(Config.EMAIL_SHARED_PREF, "Not Available");
            final String empcode = sharedPreferences.getString(Config.EMP_CODE_SHARED_PREF, "Not Available");

            db = new BarcodeDbHelper(DeliverySDp2.this);

            barcode = result.getText();
            try {
                lastText = barcode.substring(0, 11);
            } catch (StringIndexOutOfBoundsException e){
                e.printStackTrace();
            }


            barcodeView.setStatusText("Barcode" + result.getText());
            //pickedfordelivery(lastText, username, empcode, "PickAndAssignFromApp");


//            dialog.dismiss();
            try{
               /* AlertDialog.Builder builder = new AlertDialog.Builder(DeliverySDp2.this);

                builder.setTitle("Scanned for Barcode number: " + result.getText());
                onPause();
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deliveryDP2Receive(lastText, username, empcode);
                        dialog.dismiss();
                        onResume();
                    }
                });

                builder.setCancelable(false);
                final AlertDialog alert1 = builder.create();
                alert1.show();*/
                deliveryDP2Receive(lastText, username, empcode);

            }catch(Exception e){
                e.printStackTrace();
            }

            db.close();

            beepManager.playBeepSoundAndVibrate();

            //Added preview of scanned barcode
            ImageView imageView = findViewById(R.id.barcodePreview);
            imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW));

            done = findViewById(R.id.done);
            done.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), DeliverySuperVisorTablayout.class);
                    startActivity(intent);
                }
            });
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };


    @Override
    protected void onResume() {
        super.onResume();

        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        barcodeView.pause();
    }

    public void pause(View view) {
        barcodeView.pause();
    }

    public void resume(View view) {
        barcodeView.resume();
    }

    public void triggerScan(View view) {
        barcodeView.decodeSingle(callback);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), DeliverySuperVisorTablayout.class);
        startActivity(intent);
    }

    //API HIT
    private void deliveryDP2Receive(final String barcode, final String username, final String empcode) {

        StringRequest postRequest = new StringRequest(Request.Method.POST, UPDATE_DP2_RECEIVE, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray array = jsonObject.getJSONArray("summary");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject o = array.getJSONObject(i);
                        String statusCode = o.getString("responseCode");
                        if(statusCode.equals("200")){
                            Toast.makeText(DeliverySDp2.this, o.getString("success"), Toast.LENGTH_SHORT).show();
                            onResume();

                            Counter++;
                            scanCount.setText(String.valueOf(Counter));

                        } else if(statusCode.equals("404")){
                            onPause();
                            AlertDialog.Builder alertDialogBuilder404 = new AlertDialog.Builder(DeliverySDp2.this);
                            alertDialogBuilder404.setCancelable(false);
                            alertDialogBuilder404.setMessage(o.getString("unsuccess"));
                            alertDialogBuilder404.setNegativeButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            arg0.dismiss();
                                            onResume();
                                        }
                                    });

                            AlertDialog alertDialog404 = alertDialogBuilder404.create();
                            alertDialog404.show();
                        }   else if(statusCode.equals("405")){
                            onPause();
                            AlertDialog.Builder alertDialogBuilder405 = new AlertDialog.Builder(DeliverySDp2.this);
                            alertDialogBuilder405.setCancelable(false);
                            alertDialogBuilder405.setMessage(o.getString("noData"));
                            alertDialogBuilder405.setNegativeButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            arg0.dismiss();
                                            onResume();
                                        }
                                    });

                            AlertDialog alertDialog405 = alertDialogBuilder405.create();
                            alertDialog405.show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(DeliverySDp2.this, "Internet Connection Error! Try again later.", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("order", barcode);
                params.put("username", username);
                params.put("data", empcode);
                params.put("flagreq", "search_order_details_for_dp2");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(postRequest);
    }

}

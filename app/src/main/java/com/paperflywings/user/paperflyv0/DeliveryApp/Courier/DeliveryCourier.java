package com.paperflywings.user.paperflyv0.DeliveryApp.Courier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.DefaultRetryPolicy;
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
import com.paperflywings.user.paperflyv0.DeliveryApp.DeliveryOfficer.DeliveryOfficerLandingPageTabLayout.DeliveryTablayout;
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

public class DeliveryCourier extends AppCompatActivity {
    BarcodeDbHelper db;
    public SwipeRefreshLayout swipeRefreshLayout;
    private DecoratedBarcodeView barcodeView;
    private BeepManager beepManager;
    private String lastText;
    private String barcode;
    private Button done;
    private TextView scanCountText;
    private TextView scanCount;
    private int Counter = 0;
    private RequestQueue requestQueue;

    LocationManager locationManager;
    Geocoder geocoder;
    List<Address> addresses;
    // int sql_primary_id;
    private static final int REQUEST_LOCATION = 1;
    //1 means data is synced and 0 means data is not synced
    public static final int NAME_SYNCED_WITH_SERVER = 1;
    public static final int NAME_NOT_SYNCED_WITH_SERVER = 0;

    //a broadcast to know weather the data is synced or not
    // TODO add sql_primary_id to fulfillment barcode factory
    public static final String COURIER_DETAILS = "http://paperflybd.com/delivery_courier_details.php";
    public static final String URL_lOCATION = "http://paperflybd.com/GetLatlong.php";
    public static final String DATA_SAVED_BROADCAST = "net.simplifiedcoding.datasaved";
    //Broadcast receiver to know the sync status
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delivery_courier_scan);

        barcodeView = (DecoratedBarcodeView) findViewById(R.id.barcode_scanner);
        done = (Button)findViewById(R.id.done);

        scanCountText = (TextView) findViewById(R.id.scanCountTitle);
        scanCount = (TextView) findViewById(R.id.scanCount);

        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.UPC_A);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        barcodeView.initializeFromIntent(getIntent());
        barcodeView.decodeContinuous(callback);

        // location enabled
        isLocationEnabled();
        if(!isLocationEnabled()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setCancelable(false);
            builder.setTitle("Turn on location!")
                    .setMessage("This application needs location permission.Please turn on the location service from Settings. .")
                    .setPositiveButton("Settings",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DeliveryCourier.this,
                        DeliveryTablayout.class);
                startActivity(intent);
            }
        });

        beepManager = new BeepManager(this);
        //the broadcast receiver to update sync status
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            }
        };
        //registering the broadcast receiver to update sync status
        registerReceiver(broadcastReceiver, new IntentFilter(DATA_SAVED_BROADCAST));
    }

    protected boolean isLocationEnabled(){
        String le = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) getSystemService(le);
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private BarcodeCallback callback = new BarcodeCallback()  {
        @Override
        public void barcodeResult(BarcodeResult result)  {
            //Fetching email from shared preferences
            SharedPreferences sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
            String username = sharedPreferences.getString(Config.EMAIL_SHARED_PREF,"Not Available");

            db = new BarcodeDbHelper(DeliveryCourier.this);

            barcode = result.getText();
            lastText = barcode.substring(0,11);

           /* if(result.getText().equals(lastText)) {

                Toast.makeText(DeliveryCourier.this, "Already Scanned", Toast.LENGTH_SHORT).show();

            } else {*/
                barcodeView.setStatusText("Barcode"+result.getText());
                SearchCourierDetails(lastText, username);
                onPause();

//            }

            db.close();
            beepManager.playBeepSoundAndVibrate();
            //Added preview of scanned barcode
            ImageView imageView = (ImageView) findViewById(R.id.barcodePreview);
            imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW));
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

    private void SearchCourierDetails(final String barcode, final String username){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, COURIER_DETAILS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray array = jsonObject.getJSONArray("summary");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject o = array.getJSONObject(i);
                        String statusCode = o.getString("responseCode");
                        if(statusCode.equals("409")){

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DeliveryCourier.this);
                            alertDialogBuilder.setCancelable(false);
                            alertDialogBuilder.setMessage(o.getString("duplicate"));

                            alertDialogBuilder.setNegativeButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            arg0.dismiss();
                                            onResume();
                                        }
                                    });
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();

                        } else if(statusCode.equals("200")){
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DeliveryCourier.this);
                            alertDialogBuilder.setCancelable(false);
                            final View mViewCourierRcv = getLayoutInflater().inflate(R.layout.courier_receive_layout, null);

                            final TextView  pickupPointName = mViewCourierRcv.findViewById(R.id.pickup_point_name);
                            final TextView  courierName = mViewCourierRcv.findViewById(R.id.courier_name);
                            final TextView  orderCount = mViewCourierRcv.findViewById(R.id.percent_amt);
                            final TextView  pointId = mViewCourierRcv.findViewById(R.id.point_code);
                            final TextView  barcodeNumber = mViewCourierRcv.findViewById(R.id.barcode_number);
                            final TextView  vanNumber = mViewCourierRcv.findViewById(R.id.van_number);
                            final String primary_id = o.getString("primary_key");
                            pickupPointName.setText(o.getString("pickPoint"));
                            courierName.setText(o.getString("courierName"));
                            orderCount.setText(o.getString("orderid"));
                            pointId.setText(o.getString("pointId"));
                            barcodeNumber.setText(o.getString("barcodeNumber"));
                            vanNumber.setText(o.getString("van"));

                            alertDialogBuilder.setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            arg0.dismiss();
                                            onResume();
                                        }
                                    });

                            alertDialogBuilder.setPositiveButton("Receive",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                        }
                                    });

                            alertDialogBuilder.setView(mViewCourierRcv);
                            final AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();

                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
//                                    String comment = recv_comment.getText().toString();
//                                    if (comment.isEmpty()){
//                                        error_msg.setText("Please enter comment!");
//                                    } else {
                                        ReceiveSack("Y", username,primary_id);
                                        alertDialog.dismiss();
                                        onResume();
                                    //}

                                }
                            });


                        } else if(statusCode.equals("404")){
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DeliveryCourier.this);

                            alertDialogBuilder.setMessage(o.getString("notFound"));
                            alertDialogBuilder.setCancelable(false);
                            alertDialogBuilder.setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            arg0.dismiss();
                                            onResume();
                                        }
                                    });

                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
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
                        Toast.makeText(getApplicationContext(), "Check Your Internet Connection" ,Toast.LENGTH_SHORT).show();

                    }
                }){

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params1 = new HashMap<String, String>();
                params1.put("barcode", barcode);
                params1.put("flagreq", "search_courier_details");
                return params1;
            }
        };

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(this);
        }
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }


    private void ReceiveSack(final String received, final String received_by, final String primary_id){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, COURIER_DETAILS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray array = jsonObject.getJSONArray("summary1");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject o = array.getJSONObject(i);
                        String statusCode = o.getString("responseCode");
                        if(statusCode.equals("200")){
                            Toast.makeText(DeliveryCourier.this, o.getString("responseMsg"), Toast.LENGTH_SHORT).show();
                            onResume();

                            Counter++;
                            scanCount.setText(String.valueOf(Counter));

                        } else if(statusCode.equals("404")){
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DeliveryCourier.this);
                            alertDialogBuilder.setCancelable(false);
                            alertDialogBuilder.setMessage(o.getString("notFound"));
                            alertDialogBuilder.setNegativeButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            arg0.dismiss();
                                            onResume();
                                        }
                                    });

                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
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
                        Toast.makeText(getApplicationContext(), "Check Your Internet Connection" ,Toast.LENGTH_SHORT).show();

                    }
                }){

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("received", received);
                params.put("received_by", received_by);
                params.put("primary_key", primary_id);
                params.put("flagreq", "Courier_received_At_point");
                return params;
            }
        };

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(this);
        }
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }
}
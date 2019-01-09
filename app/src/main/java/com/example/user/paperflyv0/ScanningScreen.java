package com.example.user.paperflyv0;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.user.paperflyv0.MyPickupList_Executive.CREATED_AT;
import static com.example.user.paperflyv0.MyPickupList_Executive.MERCHANT_ID;
import static com.example.user.paperflyv0.MyPickupList_Executive.MERCHANT_NAME;
import static com.example.user.paperflyv0.MyPickupList_Executive.SUB_MERCHANT_NAME;

public class ScanningScreen extends AppCompatActivity {

    BarcodeDbHelper db;
    public SwipeRefreshLayout swipeRefreshLayout;
    private DecoratedBarcodeView barcodeView;
    private BeepManager beepManager;
    private String lastText;
    private Button done;
    private TextView merchant_name_textview;
    private TextView sub_merchant_name_textview;
    private TextView scan_count1 ;
    private pickuplistForExecutiveAdapter pickuplistForExecutiveAdapter;
    RecyclerView recyclerView_pul;
    RecyclerView.LayoutManager layoutManager_pul;
    RecyclerView.Adapter adapter_pul;
    android.widget.RelativeLayout vwParentRow;
    private List<PickupList_Model_For_Executive> list;
    //1 means data is synced and 0 means data is not synced
    public static final int NAME_SYNCED_WITH_SERVER = 1;
    public static final int NAME_NOT_SYNCED_WITH_SERVER = 0;
    private NotificationManagerCompat notificationManager;

    //a broadcast to know weather the data is synced or not
    public static final String UPDATE_SCAN_AND_PICKED= "http://paperflybd.com/updateTableForFulfillment.php";
    public static final String DATA_SAVED_BROADCAST = "net.simplifiedcoding.datasaved";

    //Broadcast receiver to know the sync status
    private BroadcastReceiver broadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.continuous_scan);

        notificationManager = NotificationManagerCompat.from(this);

        // Pass value through intent
        Intent intent = getIntent();
        String merchant_name = intent.getStringExtra(MERCHANT_NAME);
        String sub_merchant_name = intent.getStringExtra(SUB_MERCHANT_NAME);
        scan_count1 = (TextView) findViewById(R.id.scan_count);

        merchant_name_textview = (TextView) findViewById(R.id.merchant_name);
        sub_merchant_name_textview = (TextView) findViewById(R.id.sub_merchant_name);

        if( sub_merchant_name.equals("None") || sub_merchant_name.equals("")) {
            merchant_name_textview.setText("Scan started for: " +merchant_name);
        } else {
            sub_merchant_name_textview.setText("Scan started for: " +sub_merchant_name);
        }

        barcodeView = (DecoratedBarcodeView) findViewById(R.id.barcode_scanner);
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
        registerReceiver(broadcastReceiver, new IntentFilter(DATA_SAVED_BROADCAST));
    }


    private BarcodeCallback callback = new BarcodeCallback()  {

        @Override
        public void barcodeResult(BarcodeResult result)  {
            //Fetching email from shared preferences
            SharedPreferences sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
            String username = sharedPreferences.getString(Config.EMAIL_SHARED_PREF,"Not Available");
            final String user = username.toString();

            // current date and time
            final String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

            db = new BarcodeDbHelper(ScanningScreen.this);
//            if(result.getText() == null || result.getText().equals(lastText)) {
            if(result.getText().equals(lastText) || result.getText().trim().length() != 12) {

                // Set the toast and duration
                int toastDurationInMilliSeconds = 1000;
                final Toast mToastToShow = Toast.makeText(ScanningScreen.this,
                        result + " already scanned.", Toast.LENGTH_SHORT);
                mToastToShow.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                mToastToShow.show();

                // Set the countdown to display the toast
                CountDownTimer toastCountDown;
                toastCountDown = new CountDownTimer(toastDurationInMilliSeconds, 1000 /*Tick duration*/) {
                    public void onTick(long millisUntilFinished) {
                        mToastToShow.show();
                    }
                    public void onFinish() {
                        mToastToShow.cancel();
                    }
                };

                // Show the toast and starts the countdown
                mToastToShow.show();
                toastCountDown.start();
                return;
            }

            lastText = result.getText();

            // get merchant id
            Intent intentID = getIntent();
            final String merchant_id = intentID.getStringExtra(MERCHANT_ID);
            final String sub_merchant_name = intentID.getStringExtra(SUB_MERCHANT_NAME);
            final String match_date = intentID.getStringExtra(CREATED_AT);

            barcodeView.setStatusText("Barcode"+result.getText());

            // TODO: add added-by, current-date , vaiia says to add flag in this table
            final boolean state = true;
            final String updated_by = user;
//            final String updated_at = currentDateTimeString;

            Date c = Calendar.getInstance().getTime();
            System.out.println("Current time => " + c);
            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
            final String updated_at = df.format(c);

            // db.add(merchant_id, lastText, state, updated_by, updated_at);

            if ( lastText.trim().length() != 12 ) {

                Toast.makeText(ScanningScreen.this, "garbage", Toast.LENGTH_LONG).show();

            } else {

                barcodesave(merchant_id, sub_merchant_name, lastText, state, updated_by, updated_at);

            }
//            final int barcode_per_merchant_counts = db.getRowsCount(merchant_id, sub_merchant_name, updated_at);

//            Toast.makeText(ScanningScreen.this, "Merchant Id" +merchant_id + " Count:" + strI + " Successfull",  Toast.LENGTH_LONG).show();
//            scan_count1.setText("Scan count: " +strI);

//          builder.setTitle(strI);
            db.close();

            beepManager.playBeepSoundAndVibrate();

            //Added preview of scanned barcode
            ImageView imageView = (ImageView) findViewById(R.id.barcodePreview);
            imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW));

            done = (Button) findViewById(R.id.done);

            done.setOnClickListener(new View.OnClickListener(){

                @Override
                //On click function
                public void onClick(View view) {
                    SharedPreferences sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                    String username = sharedPreferences.getString(Config.EMAIL_SHARED_PREF,"Not Available");
                    String user1 = username.toString();

                    // current date and time
                    final String currentDateTimeString1 = DateFormat.getDateTimeInstance().format(new Date());
                    final String updated_by1 = user1;
//                    final String updated_at1 = currentDateTimeString1;

                    Date c = Calendar.getInstance().getTime();
                    System.out.println("Current time => " + c);
                    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                    final String updated_at1 = df.format(c);

                    boolean state1 = false;
                    db.update_state(state1, merchant_id, sub_merchant_name, updated_at1);
                    // TODO: Merchant id, scan count, created-by, creation-date, flag
//                    db.update_row(strI, updated_by1, updated_at1, merchant_id);
                    try{
                        final String strI = String.valueOf(db.getRowsCount(merchant_id, sub_merchant_name, match_date));
                        updateScanCount(strI, updated_by1, updated_at1, merchant_id, sub_merchant_name, match_date);
                    } catch (Exception e) {
                        Toast.makeText(ScanningScreen.this, "ScanningScreen" +e, Toast.LENGTH_SHORT).show();
                    }
//                    getData(username);
//                    sentOnChannel1();
                    Intent intent = new Intent(view.getContext(), MyPickupList_Executive.class);
                    startActivity(intent);

                }
            });
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

   /* public void sentOnChannel1(){
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_notifucation)
                .setContentTitle("Title")
                .setContentText("Notification message")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();
        notificationManager.notify(1, notification);
    }*/

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


    //API HIT
    private void barcodesave(final String merchant_id, final String sub_merchant_name, final String lastText, final Boolean state, final String updated_by, final String updated_at) {

        // get created date for match
        Intent intentID = getIntent();
        final String match_date = intentID.getStringExtra(CREATED_AT);

        StringRequest postRequest = new StringRequest(Request.Method.POST, "http://paperflybd.com/insert_barcode.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                //if there is a success
                                //storing the name to sqlite with status synced
                                db.add(merchant_id, sub_merchant_name, lastText, state, updated_by, updated_at,NAME_SYNCED_WITH_SERVER);
                                final String strI = String.valueOf(db.getRowsCount(merchant_id, sub_merchant_name, match_date));
                                scan_count1.setText("Scan count: " +strI);
//                                Toast.makeText(ScanningScreen.this, "Barcode Number Added" ,  Toast.LENGTH_LONG).show();
                                Toast toast= Toast.makeText(ScanningScreen.this,
                                        "Barcode Number Added", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                                toast.show();
                            } else {
                                //if there is some error
                                //saving the name to sqlite with status unsynced
                                db.add(merchant_id, sub_merchant_name, lastText, state, updated_by, updated_at,NAME_NOT_SYNCED_WITH_SERVER);
                                final String strI = String.valueOf(db.getRowsCount(merchant_id, sub_merchant_name, match_date));
                                scan_count1.setText("Scan count: " +strI);
//                                Toast.makeText(ScanningScreen.this, "barcode save with error" +obj.getBoolean("error"),  Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        db.add(merchant_id,sub_merchant_name, lastText, state, updated_by, updated_at,NAME_NOT_SYNCED_WITH_SERVER);
                        final String strI = String.valueOf(db.getRowsCount(merchant_id, sub_merchant_name, match_date));
                        scan_count1.setText("Scan count: " +strI);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("merchant_code", merchant_id);
                params.put("sub_merchant_name", sub_merchant_name);
                params.put("barcodeNumber", lastText);
                params.put("state", String.valueOf(state));
                params.put("updated_by", updated_by);
                params.put("updated_at", updated_at);

                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(postRequest);
    }

    // API for update
    // StrI, updated_by1, updated_at1, merchant_id
    public void updateScanCount(final String strI, final String updated_by, final String updated_at, final String merchant_id, final String sub_merchant_name, final String match_date) {
        final BarcodeDbHelper db = new BarcodeDbHelper(getApplicationContext());
        StringRequest postRequest = new StringRequest(Request.Method.POST, UPDATE_SCAN_AND_PICKED,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                //if there is a success
                                //storing the name to sqlite with status synced
//                                db.add(merchant_id, lastText, state, updated_by, updated_at,);
                                db.update_row(strI, updated_by, updated_at, merchant_id, sub_merchant_name, match_date, NAME_SYNCED_WITH_SERVER);
                            } else {
                                //if there is some error
                                //saving the name to sqlite with status unsynced
                                db.update_row(strI, updated_by, updated_at, merchant_id,sub_merchant_name, match_date, NAME_NOT_SYNCED_WITH_SERVER);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        db.update_row(strI, updated_by, updated_at, merchant_id, sub_merchant_name, match_date, NAME_NOT_SYNCED_WITH_SERVER);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("merchant_code", merchant_id);
                params.put("p_m_name", sub_merchant_name);
                params.put("created_at", match_date);
                params.put("scan_count", strI);
                params.put("picked_qty", "0");
                params.put("updated_by", updated_by);
                params.put("updated_at", updated_at);
                return params;
            }
        };
        try { RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(postRequest);
        } catch (Exception e) {
            Toast.makeText(ScanningScreen.this, "Request Queue" +e, Toast.LENGTH_SHORT).show();
        }

    }
}

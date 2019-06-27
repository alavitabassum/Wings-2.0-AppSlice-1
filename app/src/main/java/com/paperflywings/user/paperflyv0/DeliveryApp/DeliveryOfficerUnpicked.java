package com.paperflywings.user.paperflyv0.DeliveryApp;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.paperflywings.user.paperflyv0.Databases.BarcodeDbHelper;
import com.paperflywings.user.paperflyv0.Config;
import com.paperflywings.user.paperflyv0.LoginActivity;
import com.paperflywings.user.paperflyv0.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.CAMERA;

public class DeliveryOfficerUnpicked extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,Delivery_unpicked_adapter.OnItemClickListener,SwipeRefreshLayout.OnRefreshListener {

    BarcodeDbHelper db;
    public SwipeRefreshLayout swipeRefreshLayout;
    private Button delivery_quick_pick;
    private String lastText;

    public static final String BARCODE_NO= "barcode";
    public static final String ORDERID = "orderid";
    public static final String MERCHANT_ORDER_REF = "merOrderRef";
    public static final String MERCHANTS_NAME = "merchantName";
    public static final String PICK_MERCHANTS_NAME = "pickMerchantName";
    public static final String CUSTOMER_NAME = "custname";
    public static final String PHONE = "custphone";
    public static final String CUSTOMER_ADDRESS = "custaddress";
    public static final String PACKAGE_PRICE = "packagePrice";
    public static final String PRODUCT_BRIEF= "productBrief";
    public static final String DELIVERY_TIME= "deliveryTime";

    TextView unpicked_text;

    //public static final String SQL_PRIMARY_ID = "Sql Primary Id";
    private static final String URL_DATA = "";
    private ProgressDialog progress;
    private Delivery_unpicked_adapter Delivery_unpicked_adapter;
    RecyclerView recyclerView_pul;
    RecyclerView.LayoutManager layoutManager_pul;
    RecyclerView.Adapter adapter_pul;
    android.widget.RelativeLayout vwParentRow;
    private static final int REQUEST_CAMERA = 1;

    public static final String UNPICKED_LIST = "http://paperflybd.com/DeliveryUnpickedApis.php";

    private List<Delivery_unpicked_model> list;
    public static final int NAME_NOT_SYNCED_WITH_SERVER = 0;
    public static final int NAME_SYNCED_WITH_SERVER = 1;

    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db=new BarcodeDbHelper(getApplicationContext());
        db.getWritableDatabase();

        setContentView(R.layout.activity_delivery_officer_unpicked);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView_pul = (RecyclerView) findViewById(R.id.recycler_view_myunpickup_list);
        recyclerView_pul.setAdapter(Delivery_unpicked_adapter);

        list = new ArrayList<Delivery_unpicked_model>();

        // get the logged in username by shared preference
        SharedPreferences sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString(Config.EMAIL_SHARED_PREF,"Not Available");

        // check internet connectivity
        ConnectivityManager cManager = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cManager.getActiveNetworkInfo();

        delivery_quick_pick = (Button) findViewById(R.id.delivery_quick_pick);


        unpicked_text = (TextView)findViewById(R.id.unpicks_);

        Intent intent = getIntent();
        String str = intent.getStringExtra("message");
        unpicked_text.setText(str);
        /*new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                list.remove(viewHolder.getAdapterPosition());
                Toast.makeText(DeliveryOfficerUnpicked.this,"Item Removed"+ viewHolder.getAdapterPosition(),Toast.LENGTH_SHORT).show();
                Delivery_unpicked_adapter.notifyDataSetChanged();
            }
        }).attachToRecyclerView(recyclerView_pul);*/

        layoutManager_pul = new LinearLayoutManager(this);
        recyclerView_pul.setLayoutManager(layoutManager_pul);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        list.clear();
        swipeRefreshLayout.setRefreshing(true);

        //If internet connection is available or not
        if(nInfo!= null && nInfo.isConnected())
        {
            loadRecyclerView(username);
        }
        else{
            getData(username);

            Toast.makeText(this,"Check Your Internet Connection",Toast.LENGTH_LONG).show();
        }

        // Redirect for quick pick by scanning barcode
        delivery_quick_pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DeliveryOfficerUnpicked.this,
                        Delivery_quick_pick_scan.class);


                startActivity(intent);
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout_deliver_officer_unpicked);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = (TextView) headerView.findViewById(R.id.delivery_officer_name);
        navUsername.setText(username);
        navigationView.setNavigationItemSelectedListener(this);

        int currentApiVersion = Build.VERSION.SDK_INT;

        if(currentApiVersion >=  Build.VERSION_CODES.KITKAT)
        {
            if(checkPermission())
            {
//                Toast.makeText(getApplicationContext(), "Permission already granted!", Toast.LENGTH_LONG).show();
            }
            else
            {
                requestPermission();
            }
        }


    }

    private void getData(String user){
        try{
            list.clear();
            Date date = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
            final String currentDateTimeString = df.format(date);

            SQLiteDatabase sqLiteDatabase = db.getReadableDatabase();
            Cursor c = db.get_delivery_unpicked(sqLiteDatabase,user);

            while (c.moveToNext()){

                String barcode = c.getString(0);
                String orderid = c.getString(1);
                String merOrderRef = c.getString(2);
                String merchantName = c.getString(3);
                String pickMerchantName = c.getString(4);
                String custname = c.getString(5);
                String custaddress = c.getString(6);
                String custphone = c.getString(7);
                String packagePrice = c.getString(8);
                String productBrief = c.getString(9);
                String deliveryTime = c.getString(10);

                Delivery_unpicked_model unpickedmodel = new Delivery_unpicked_model(barcode,orderid,merOrderRef,merchantName,pickMerchantName,custname,custaddress,custphone,packagePrice,productBrief,deliveryTime);

                list.add(unpickedmodel);
            }

            Delivery_unpicked_adapter = new Delivery_unpicked_adapter(list,getApplicationContext());
            recyclerView_pul.setAdapter(Delivery_unpicked_adapter);
            Delivery_unpicked_adapter.notifyDataSetChanged();
            Delivery_unpicked_adapter.setOnItemClickListener(DeliveryOfficerUnpicked.this);
            swipeRefreshLayout.setRefreshing(false);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadRecyclerView (final String user){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        final String match_date = df.format(c);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, UNPICKED_LIST,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        SQLiteDatabase sqLiteDatabase = db.getWritableDatabase();
                        db.deleteAssignedList(sqLiteDatabase);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray array = jsonObject.getJSONArray("summary");

                            for(int i =0;i<array.length();i++)
                            {
                                JSONObject o = array.getJSONObject(i);
                                Delivery_unpicked_model unpickedmodel = new  Delivery_unpicked_model(

                                        o.getString("barcode"),
                                        o.getString("orderid"),
                                        o.getString("merOrderRef"),
                                        o.getString("merchantName"),
                                        o.getString("pickMerchantName"),
                                        o.getString("custname"),
                                        o.getString("custaddress"),
                                        o.getString("custphone"),
                                        o.getString("packagePrice"),
                                        o.getString("productBrief"),
                                        o.getString("deliveryTime"));

                                db.insert_delivery_unpicked_count(

                                        o.getString("barcode"),
                                        o.getString("orderid"),
                                        o.getString("merOrderRef"),
                                        o.getString("merchantName"),
                                        o.getString("pickMerchantName"),
                                        o.getString("custname"),
                                        o.getString("custaddress"),
                                        o.getString("custphone"),
                                        o.getString("packagePrice"),
                                        o.getString("productBrief"),
                                        o.getString("deliveryTime")
                                        , NAME_NOT_SYNCED_WITH_SERVER );

                                list.add(unpickedmodel);

                            }

//                    swipeRefreshLayout.setRefreshing(false);

                            Delivery_unpicked_adapter = new Delivery_unpicked_adapter(list,getApplicationContext());
                            recyclerView_pul.setAdapter(Delivery_unpicked_adapter);
                            swipeRefreshLayout.setRefreshing(false);
                            Delivery_unpicked_adapter.setOnItemClickListener(DeliveryOfficerUnpicked.this);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        progress.dismiss();
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(getApplicationContext(), "Serve not connected" ,Toast.LENGTH_LONG).show();

                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String,String> params1 = new HashMap<String,String>();
                params1.put("username",user);
//                params1.put("created_at",match_date);
                return params1;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private boolean checkPermission()
    {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    // Request for camera permission
    private void requestPermission()
    {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    // Actions on camera permission grant result
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted){
                        Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access camera", Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access and camera", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{CAMERA},
                                                            REQUEST_CAMERA);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    // Camera permission ok or cancel
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(DeliveryOfficerUnpicked.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_deliver_officer_unpicked);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent homeIntent = new Intent(DeliveryOfficerUnpicked.this,
                    DeliveryOfficerCardMenu.class);
            startActivity(homeIntent);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        try{  searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Delivery_unpicked_adapter.getFilter().filter(newText);
                return false;
            }
        });
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Intent intent_stay = new Intent(DeliveryOfficerUnpicked.this,DeliveryWithoutStatus.class);
            Toast.makeText(this, "Page Loading...", Toast.LENGTH_SHORT).show();
            startActivity(intent_stay);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent homeIntent = new Intent(DeliveryOfficerUnpicked.this,
                    DeliveryOfficerCardMenu.class);
            startActivity(homeIntent);
        }
      /*  else if (id == R.id.nav_pickup_sum) {
            Intent pickupIntent = new Intent(DeliveryOfficerCardMenu.this,
                    PickupsToday_Executive.class);
            startActivity(pickupIntent);
        } else if (id == R.id.nav_exe_pickup) {
            Intent assignIntent = new Intent(DeliveryOfficerCardMenu.this,
                    MyPickupList_Executive.class);
            startActivity(assignIntent);
        }*/
//        else if (id == R.id.nav_pickStatus) {
//            Intent historyIntent = new Intent(ExecutiveCardMenu.this,
//                    PickupStatus_Executive.class);
//            startActivity(historyIntent);
//        }
        else if (id == R.id.nav_logout) {
            //Creating an alert dialog to confirm logout
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Are you sure you want to logout?");
            alertDialogBuilder.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                            SQLiteDatabase sqLiteDatabase = db.getWritableDatabase();
                            db.deleteAssignedList(sqLiteDatabase);

                            //Getting out sharedpreferences
                            SharedPreferences preferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);

                            //Getting editor
                            SharedPreferences.Editor editor = preferences.edit();

                            //Puting the value false for loggedin
                            editor.putBoolean(Config.LOGGEDIN_SHARED_PREF, false);

                            //Putting blank value to email
                            editor.putString(Config.EMAIL_SHARED_PREF, "");

                            //Saving the sharedpreferences
                            editor.commit();

                            //Starting login activity
                            Intent intent = new Intent(DeliveryOfficerUnpicked.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    });

            alertDialogBuilder.setNegativeButton("No",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });

            //Showing the alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout_deliver_officer_unpicked);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRefresh() {
        ConnectivityManager cManager = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cManager.getActiveNetworkInfo();

        SharedPreferences sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString(Config.EMAIL_SHARED_PREF,"Not Available");
        list.clear();
        Delivery_unpicked_adapter.notifyDataSetChanged();
        if(nInfo!= null && nInfo.isConnected())
        {
            loadRecyclerView(username);
        }
        else{
            getData(username);
        }
    }



    @Override
    public void onItemClick_view(View view2, int position2) {

        final Delivery_unpicked_model clickedITem = list.get(position2);

        SharedPreferences sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        final String username = sharedPreferences.getString(Config.EMAIL_SHARED_PREF, "Not Available");
        final String empcode = sharedPreferences.getString(Config.EMP_CODE_SHARED_PREF,"Not Available");


        String lastText = clickedITem.getBarcode();

        pickedfordelivery(lastText,username,empcode);
    }

   /* @Override
    public void onItemClick_view(View view2, int position2) {

    }

    @Override
    public void onItemClick_view_orderIDs(View view3, int position3) {

    }
*/
    @Override
    public void onItemClick_call(View view4, int position4) {
        Intent callIntent =new Intent(Intent.ACTION_CALL);
        String phoneNumber = list.get(position4).getCustphone();
        String lastFourDigits = phoneNumber.substring(phoneNumber.length() - 10);
        callIntent.setData(Uri.parse("tel: +880" +lastFourDigits));
        if (ActivityCompat.checkSelfPermission(view4.getContext(),
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) view4.getContext(),
                    new String[]{Manifest.permission.CALL_PHONE},
                    MY_PERMISSIONS_REQUEST_CALL_PHONE);
            return;
        }
        view4.getContext().startActivity(callIntent);
    }

    private void pickedfordelivery(final String barcode, final String username, final String empcode) {

        StringRequest postRequest = new StringRequest(Request.Method.POST, "http://paperflybd.com/DeliveryPick.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                //if there is a success
                                //storing the name to sqlite with status synced
                                db.getUnpickedOrderData(barcode,username,empcode,NAME_SYNCED_WITH_SERVER);
                                Toast toast= Toast.makeText(DeliveryOfficerUnpicked.this,
                                        "Product Picked Successful", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                                toast.show();
                            } else {
                                //if there is some error
                                //saving the name to sqlite with status unsynced
                                db.getUnpickedOrderData(barcode,username,empcode,NAME_NOT_SYNCED_WITH_SERVER);
                                Toast.makeText(DeliveryOfficerUnpicked.this, "Unsuccessful" +response,  Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        db.getUnpickedOrderData(barcode,username,empcode,NAME_NOT_SYNCED_WITH_SERVER);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("barcode", barcode);
                params.put("username", username);
                params.put("empcode", empcode);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(postRequest);
    }
}

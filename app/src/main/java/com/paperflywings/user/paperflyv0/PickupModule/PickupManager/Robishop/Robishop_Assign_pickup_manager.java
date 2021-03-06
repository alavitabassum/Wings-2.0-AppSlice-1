package com.paperflywings.user.paperflyv0.PickupModule.PickupManager.Robishop;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.paperflywings.user.paperflyv0.PickupModule.PickupManager.AjkerdealdirectdeliveryManager.AjkerDealOther_Assign_Pickup_manager;
import com.paperflywings.user.paperflyv0.PickupModule.AssignManager_ExecutiveList;
import com.paperflywings.user.paperflyv0.PickupModule.PickupManager.LogisticAssignManager.AssignPickup_Manager;
import com.paperflywings.user.paperflyv0.Config;
import com.paperflywings.user.paperflyv0.Databases.Database;
import com.paperflywings.user.paperflyv0.PickupModule.PickupManager.FulfillmentAssignManager.Fulfillment_Assign_pickup_Manager;
import com.paperflywings.user.paperflyv0.LoginActivity;
import com.paperflywings.user.paperflyv0.PickupModule.PickupManager.ManagerCardMenu;
import com.paperflywings.user.paperflyv0.NetworkStateChecker;
import com.paperflywings.user.paperflyv0.PickupModule.PickupsToday_Manager;
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

public class Robishop_Assign_pickup_manager extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, robishopAssignExecutiveAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener{

    public SwipeRefreshLayout swipeRefreshLayout;
    private ProgressDialog progress;
    public static final String INSERT_URL = "http://paperflybd.com/insertassign.php";
    private String MERCHANT_URL = "http://paperflybd.com/r_api_pull_for_pick.php";
    private String EXECUTIVE_URL = "http://paperflybd.com/executiveList.php";
    public static final String UPDATE_ASSIGN_URL = "http://paperflybd.com/updateUnassignedAPI1.php";

    private robishopAssignExecutiveAdapter RobishopAssignExecutiveAdapter;
    List<AssignManager_ExecutiveList> executiveLists;
    List<RobishopAssignManager_Model> robishop_modelList;
    Database database;

    public static final int NAME_SYNCED_WITH_SERVER = 1;
    public static final int NAME_NOT_SYNCED_WITH_SERVER = 0;
    //a broadcast to know weather the data is synced or not
    public static final String DATA_SAVED_BROADCAST = "net.simplifiedcoding.datasaved";

    //Broadcast receiver to know the sync status
    private BroadcastReceiver broadcastReceiver;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robishop__assign_pickup_manager);
        database = new Database(getApplicationContext());
        database.getWritableDatabase();
        executiveLists = new ArrayList<>();
        robishop_modelList = new ArrayList<>();


        ConnectivityManager cManager = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cManager.getActiveNetworkInfo();

        //Fetching email from shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString(Config.EMAIL_SHARED_PREF, "Not Available");
        String user = username.toString();

        //recycler with cardview
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_assign_robishop);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        swipeRefreshLayout = findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        robishop_modelList.clear();
        swipeRefreshLayout.setRefreshing(true);

        //Offline sync
        registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        //If internet connection is available or not
        if(nInfo!= null && nInfo.isConnected())
        {
            loadmerchantlist();
            loadexecutivelist(user);
        }
        else{
            getallmerchant();
            getallexecutives();
            Toast.makeText(this,"Check Your Internet Connection",Toast.LENGTH_LONG).show();
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            }
        };


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = (TextView) headerView.findViewById(R.id.manager_name);
        navUsername.setText(username);
        navigationView.setNavigationItemSelectedListener(this);
    }

    //Load executive from api
    private void loadexecutivelist(final String user) {
        StringRequest postRequest1 = new StringRequest(Request.Method.POST, EXECUTIVE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray array = jsonObject.getJSONArray("executivelist");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject o = array.getJSONObject(i);
                                AssignManager_ExecutiveList assignManager_executiveList = new AssignManager_ExecutiveList(
                                        o.getString("userName"),
                                        o.getString("empCode"),
                                        o.getString("empName"),
                                        o.getString("contactNumber")
                                );
                                executiveLists.add(assignManager_executiveList);
                                database.addexecutivelist(  o.getString("userName"),
                                        o.getString("empCode"),
                                        o.getString("empName"),
                                        o.getString("contactNumber"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params1 = new HashMap<String, String>();
                params1.put("username", user);
                return params1;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(postRequest1);
    }


    //Get Executive List from sqlite
    private void getallexecutives() {
        try {

            SQLiteDatabase sqLiteDatabase = database.getReadableDatabase();
            Cursor c = database.get_executivelist(sqLiteDatabase);
            while (c.moveToNext()) {
                String empName = c.getString(0);
                String empCode = c.getString(1);
                AssignManager_ExecutiveList assignManager_executiveList = new AssignManager_ExecutiveList(empName, empCode);
                executiveLists.add(assignManager_executiveList);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Merchant List API hit
    private void loadmerchantlist() {
        progress=new ProgressDialog(this);
        progress.setMessage("Loading Data");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(true);
        progress.setProgress(0);
        progress.show();

        StringRequest postRequest1 = new StringRequest(Request.Method.POST, MERCHANT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        SQLiteDatabase sqLiteDatabase = database.getWritableDatabase();
                        database.deletemerchantList_robishop(sqLiteDatabase);
                        progress.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray array = jsonObject.getJSONArray("summary");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject o = array.getJSONObject(i);
                                RobishopAssignManager_Model todaySummary = new RobishopAssignManager_Model(
                                        o.getString("merchantCode"),
                                        o.getString("address"),
                                        o.getString("merchantName"),
                                        o.getString("pickMerchantName"),
                                        o.getString("pickMerchantAddress"),
                                        o.getString("pickupMerchantPhone"),
                                        o.getString("merOrderRef"),
                                        o.getString("productBrief"),
                                        o.getString("orderDate"),
                                        o.getString("pickAssignedStatus"));
                              // the name of the attributes have to be the same as the api attributes
                                database.addRobishop(
                                        o.getString("merchantCode"),
                                        o.getString("address"),
                                        o.getString("merchantName"),
                                        o.getString("pickMerchantName"),
                                        o.getString("pickMerchantAddress"),
                                        o.getString("pickupMerchantPhone"),
                                        o.getString("merOrderRef"),
                                        o.getString("productBrief"),
                                        o.getString("orderDate"),
                                        o.getString("pickAssignedStatus"));
                                robishop_modelList.add(todaySummary);

                            }
                            RobishopAssignExecutiveAdapter = new robishopAssignExecutiveAdapter(robishop_modelList, getApplicationContext());
                            recyclerView.setAdapter(RobishopAssignExecutiveAdapter);
                            swipeRefreshLayout.setRefreshing(false);
                            RobishopAssignExecutiveAdapter.setOnItemClickListener(Robishop_Assign_pickup_manager.this);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            swipeRefreshLayout.setRefreshing(false);


                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progress.dismiss();
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(getApplicationContext(), "NO INTERNET CONNECTION" , Toast.LENGTH_SHORT).show();
                    }
                }
        );

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        postRequest1.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(postRequest1);
    }

    /* merchant List generation from sqlite*/
    private void getallmerchant() {

        try {
            robishop_modelList.clear();
            SQLiteDatabase sqLiteDatabase = database.getReadableDatabase();
            Cursor c = database.get_robishop_merchantlist(sqLiteDatabase);

            while (c.moveToNext()) {
                String merchantCode = c.getString(0);
                String address = c.getString(1);
                String merchantName = c.getString(2);
                String pickMerchantName = c.getString(3);
                String pickMerchantAddress = c.getString(4);
                String pickupMerchantPhone = c.getString(5);
                String merOrderRef = c.getString(6);
                String productBrief = c.getString(7);
                String created_at = c.getString(8);
                String pickAssignedStatus = c.getString(9);
                RobishopAssignManager_Model todaySummary = new RobishopAssignManager_Model(merchantCode,address,merchantName,pickMerchantName, pickMerchantAddress, pickupMerchantPhone, merOrderRef, productBrief,created_at,pickAssignedStatus);
                robishop_modelList.add(todaySummary);
            }

            RobishopAssignExecutiveAdapter = new robishopAssignExecutiveAdapter(robishop_modelList, getApplicationContext());
            recyclerView.setAdapter(RobishopAssignExecutiveAdapter);
            swipeRefreshLayout.setRefreshing(false);
            RobishopAssignExecutiveAdapter.setOnItemClickListener(Robishop_Assign_pickup_manager.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // String status = "0";
    /*private void assignexecutivetosqlite(final String ex_name, final String empcode, final String product_name, final String sum, final String product_id, final String user, final String currentDateTimeString, final int status,final String m_name,final String contactNumber,final String pick_m_name,final String pick_m_address, final String complete_status,final String apiOrderID, final String demo, final String pick_from_merchant_status, final  String received_from_HQ_status) {
        database.assignexecutive(ex_name, empcode, product_name, sum, String.valueOf(product_id), user, currentDateTimeString, status,m_name,contactNumber,pick_m_name,pick_m_address, complete_status,apiOrderID, demo,pick_from_merchant_status, received_from_HQ_status);
     }*/

    private void updateAssignedStatus(final String merchant_code, final int status, final String pickAssignedStatus, final String demo) {
        database.updateAssignedStatusRobi(merchant_code, status, pickAssignedStatus, demo);
    }

    //For assigning executive API into mysql
    private void assignexecutive(final String ex_name, final String empcode,final String product_name, final String sum, final String product_id, final String user, final String currentDateTimeString, final String m_name,final String contactNumber,final String pick_m_name,final String pick_m_address, final String complete_status,final String apiOrderID, final String demo, final String pick_from_merchant_status,final String received_from_HQ_status) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, INSERT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                //if there is a success
                                //storing the name to sqlite with status synced
                                database.assignexecutive(ex_name, empcode, product_name, sum, product_id, user, currentDateTimeString,m_name,contactNumber,pick_m_name,pick_m_address, complete_status,apiOrderID, demo,pick_from_merchant_status, received_from_HQ_status,NAME_SYNCED_WITH_SERVER);

//                                assignexecutivetosqlite(ex_name, empcode, product_name, sum,String.valueOf(product_id), user, currentDateTimeString, NAME_SYNCED_WITH_SERVER,m_name,contactNumber,pick_m_name,pick_m_address,complete_status,apiOrderID, demo, pick_from_merchant_status,received_from_HQ_status);
                            } else {
                                //if there is some error
                                //saving the name to sqlite with status unsynced
                                database.assignexecutive(ex_name, empcode, product_name, sum, product_id, user, currentDateTimeString,m_name,contactNumber,pick_m_name,pick_m_address, complete_status,apiOrderID, demo,pick_from_merchant_status, received_from_HQ_status,NAME_NOT_SYNCED_WITH_SERVER);

//                                assignexecutivetosqlite(ex_name, empcode, product_name, sum, String.valueOf(product_id), user, currentDateTimeString, NAME_NOT_SYNCED_WITH_SERVER,m_name,contactNumber,pick_m_name,pick_m_address, complete_status, apiOrderID, demo,pick_from_merchant_status,received_from_HQ_status);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        database.assignexecutive(ex_name, empcode, product_name, sum, product_id, user, currentDateTimeString,m_name,contactNumber,pick_m_name,pick_m_address, complete_status,apiOrderID, demo,pick_from_merchant_status, received_from_HQ_status,NAME_NOT_SYNCED_WITH_SERVER);

//                        assignexecutivetosqlite(ex_name, empcode,product_name, sum, String.valueOf(product_id), user, currentDateTimeString, NAME_NOT_SYNCED_WITH_SERVER,m_name,contactNumber,pick_m_name,pick_m_address, complete_status,apiOrderID, demo, pick_from_merchant_status, received_from_HQ_status);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("executive_name", ex_name);
                params.put("executive_code", empcode);
                params.put("product_name", product_name);
                params.put("order_count", sum);
                params.put("merchant_code", product_id);
                params.put("assigned_by", user);
                params.put("created_at", currentDateTimeString);
                params.put("merchant_name", m_name);
                params.put("phone_no", contactNumber);
                params.put("p_m_name",pick_m_name);
                params.put("p_m_address",pick_m_address);
                params.put("complete_status", complete_status);
                params.put("api_order_id", apiOrderID);
                params.put("demo", demo);
                params.put("pick_from_merchant_status",pick_from_merchant_status);
                params.put("received_from_HQ_status",received_from_HQ_status);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(postRequest);
    }

    private void updatePickAssigedStatus(final String merchant_code, final String pickAssignedStatus, final String demo){
        StringRequest postRequest = new StringRequest(Request.Method.POST, UPDATE_ASSIGN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                // if there is a success
                                // storing the name to sqlite with status synced
                                // assignexecutivetosqlite(ex_name, empcode, product_name, order_count, merchant_code, user, currentDateTimeString, NAME_SYNCED_WITH_SERVER,m_name,contactNumber,pick_m_name,pick_m_address, complete_status, apiOrderID,demo,pick_from_merchant_status, received_from_HQ_status);
                                updateAssignedStatus(merchant_code, NAME_SYNCED_WITH_SERVER, pickAssignedStatus,demo);
                            } else {
                                // if there is some error
                                // saving the name to sqlite with status unsynced
                                updateAssignedStatus( merchant_code, NAME_NOT_SYNCED_WITH_SERVER, pickAssignedStatus,demo);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        updateAssignedStatus( merchant_code, NAME_NOT_SYNCED_WITH_SERVER, pickAssignedStatus,demo);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("merchantCode", merchant_code);
                params.put("pickAssignedStatus", pickAssignedStatus);
                params.put("merOrderRef", demo);
                return params;
            }

        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(postRequest);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent homeIntentSuper = new Intent(Robishop_Assign_pickup_manager.this,
                    ManagerCardMenu.class);
            startActivity(homeIntentSuper);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        try {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    RobishopAssignExecutiveAdapter.getFilter().filter(newText);
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Intent intent_stay = new Intent(Robishop_Assign_pickup_manager.this, AssignPickup_Manager.class);
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
        if (id == R.id.action_settings) {
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
            Intent homeIntent = new Intent(Robishop_Assign_pickup_manager.this,
                    ManagerCardMenu.class);
            startActivity(homeIntent);
        } else if (id == R.id.nav_pickDue) {
            Intent pickupIntent = new Intent(Robishop_Assign_pickup_manager.this,
                    PickupsToday_Manager.class);
            startActivity(pickupIntent);
        } else if (id == R.id.nav_assign) {
            Intent assignIntent = new Intent(Robishop_Assign_pickup_manager.this,
                    AssignPickup_Manager.class);
            startActivity(assignIntent);
        } else if (id == R.id.nav_fulfill) {
            Intent assignFulfillmentIntent = new Intent(Robishop_Assign_pickup_manager.this,
                    Fulfillment_Assign_pickup_Manager.class);
            startActivity(assignFulfillmentIntent);
        }  else if (id == R.id.nav_robishop) {
            Intent robishopIntent = new Intent(Robishop_Assign_pickup_manager.this,
                    Robishop_Assign_pickup_manager.class);
            startActivity(robishopIntent);
        }  else if (id == R.id.nav_adeal_direct) {
            Intent adealdirectIntent = new Intent(Robishop_Assign_pickup_manager.this,
                    AjkerDealOther_Assign_Pickup_manager.class);
            startActivity(adealdirectIntent);
        }
        else if (id == R.id.nav_logout) {
            //Creating an alert dialog to confirm logout
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Are you sure you want to logout?");
            alertDialogBuilder.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            SQLiteDatabase sqLiteDatabase = database.getWritableDatabase();
                            database.clearPTMList(sqLiteDatabase);
                            database.deletemerchantList(sqLiteDatabase);
                            database.deletemerchantList_Fulfillment(sqLiteDatabase);
                            database.deletemerchantList_ajkerDeal(sqLiteDatabase);
                            database.deletemerchantList_ajkerDealEkshopList(sqLiteDatabase);
                            database.deletemerchantList_ajkerDealOtherList(sqLiteDatabase);
                            database.deletemerchants(sqLiteDatabase);
                            database.deletemerchantsfor_executives(sqLiteDatabase);
                            database.deletecom_ex(sqLiteDatabase);
                            database.delete_fullfillment_merchantList(sqLiteDatabase);
                            database.deletecom_fulfillment_supplier(sqLiteDatabase);
                            database.deletecom_fullfillment_product(sqLiteDatabase);
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
                            Intent intent = new Intent(Robishop_Assign_pickup_manager.this, LoginActivity.class);
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onItemClick(View view, int position) {
        final RobishopAssignManager_Model clickeditem = robishop_modelList.get(position);

        AlertDialog.Builder spinnerBuilder = new AlertDialog.Builder(Robishop_Assign_pickup_manager.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_spinner_fulfillment, null);
        spinnerBuilder.setTitle("Select executive and assign number.");

        final TextView dialog_mName = mView.findViewById(R.id.dialog_m_name);

        final AutoCompleteTextView mAutoComplete = mView.findViewById(R.id.auto_exe);
        final EditText et1 = mView.findViewById(R.id.spinner1num);
        final TextView tv1 = mView.findViewById(R.id.textView3);
        dialog_mName.setText(clickeditem.getPickMerchantName());

        String totalCount = "1";
        et1.setText(totalCount);

        final String product_id = clickeditem.getMerchantCode();
        final String m_name = clickeditem.getPickMerchantName();
        final String contactNumber = clickeditem.getPickupMerchantPhone();
        final String pick_merchant_name = clickeditem.getPickMerchantName();
        final String product_name = clickeditem.getProductBrief();
        final String pick_merchant_address = clickeditem.getPickMerchantAddress();
        final String complete_status = "r";
        final String apiOrderID = clickeditem.getMerOrderRef();
        final String demo = clickeditem.getMerOrderRef();
        final String pick_from_merchant_status = "0";
        final String received_from_HQ_status = "0";
        final String pickAssignedStatus = "1";
        SharedPreferences sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString(Config.EMAIL_SHARED_PREF, "Not Available");
        final String user = username.toString();

        //final String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        final String currentDateTimeString = df.format(c);

        List<String> lables = new ArrayList<String>();

        for (int z = 0; z < executiveLists.size(); z++) {
            lables.add(executiveLists.get(z).getExecutive_name());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(Robishop_Assign_pickup_manager.this,
                android.R.layout.simple_list_item_1, lables);

        mAutoComplete.setAdapter(adapter);

        spinnerBuilder.setPositiveButton("Assign", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int i1) {

                if(et1.getText().toString().trim().isEmpty()) {
                    tv1.setText("Order count can't be empty");
                } else {
                    String empname = mAutoComplete.getText().toString();

                    final String empcode = database.getSelectedEmployeeCode(empname);

                    assignexecutive(empname, empcode, product_name, et1.getText().toString(), product_id, user, currentDateTimeString, m_name, contactNumber, pick_merchant_name, pick_merchant_address, complete_status,apiOrderID, demo, pick_from_merchant_status,received_from_HQ_status);
                    updatePickAssigedStatus(product_id, pickAssignedStatus, demo);
                    if (!mAutoComplete.getText().toString().isEmpty() || mAutoComplete.getText().toString().equals(null)) {
                    Toast.makeText(Robishop_Assign_pickup_manager.this, mAutoComplete.getText().toString()
                                    + "(" + et1.getText().toString() + ")",
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();

                    }
                }

            }
        });

        spinnerBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i1) {
                dialog.dismiss();
            }
        });
        spinnerBuilder.setCancelable(false);
        spinnerBuilder.setView(mView);
        final AlertDialog dialog2 = spinnerBuilder.create();
        dialog2.show();
        dialog2.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String empname = mAutoComplete.getText().toString();
                final String empcode = database.getSelectedEmployeeCode(empname);
                if(et1.getText().toString().trim().isEmpty() || empname.trim().isEmpty()) {
                    tv1.setText("Field can't be empty");
//                    dialog.equals("Order count can't be empty");

                } else {
                    assignexecutive(mAutoComplete.getText().toString(), empcode, product_name, et1.getText().toString(), product_id, user, currentDateTimeString, m_name, contactNumber, String.valueOf(pick_merchant_name), pick_merchant_address, complete_status,apiOrderID, demo,pick_from_merchant_status,received_from_HQ_status);
                    updatePickAssigedStatus(product_id, pickAssignedStatus, demo);
                    if (!mAutoComplete.getText().toString().isEmpty() || mAutoComplete.getText().toString().equals(null)) {
                        Toast.makeText(Robishop_Assign_pickup_manager.this, mAutoComplete.getText().toString()
                                        + "(" + et1.getText().toString() + ")",
                                Toast.LENGTH_SHORT).show();
                        dialog2.dismiss();

                    }
                }
            }
        });
    }


    @Override
    public void onRefresh() {
        ConnectivityManager cManager = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cManager.getActiveNetworkInfo();
        SharedPreferences sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString(Config.EMAIL_SHARED_PREF,"Not Available");
        robishop_modelList.clear();
        RobishopAssignExecutiveAdapter.notifyDataSetChanged();
        //If internet connection is available or not
        if(nInfo!= null && nInfo.isConnected())
        {
            loadmerchantlist();
        }
        else{
            getallmerchant();
        }
    }

}
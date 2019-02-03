package com.paperflywings.user.paperflyv0;

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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AjkerDealEkshop_Assign_Pickup_manager extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener , AjkerDealAssignExecutiveAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener{

    public SwipeRefreshLayout swipeRefreshLayout;
    private ProgressDialog progress;
    public static final String INSERT_URL = "http://paperflybd.com/insertassign.php";
    private String MERCHANT_URL = "http://paperflybd.com/ajkerdeal_ekshop_api.php";
    private String EXECUTIVE_URL = "http://paperflybd.com/executiveList.php";

    private String MAIN_MERCHANT_URL = "http://paperflybd.com/fulfillmentMerchantAPI.php";
    private String SUPPLIER_NAME_URL = "http://paperflybd.com/fulfillmentSupplierAPI.php";
    private String PRODUCT_LIST_URL = "http://paperflybd.com/fulfillmentProductAPI.php";

    private AjkerDealAssignExecutiveAdapter ajkerDealAssignExecutiveAdapter;
    List<AssignManager_ExecutiveList> executiveLists;
    List<AjkerDealAssignManager_Model> ajkerdeal_modelList;
    Database database;

    public static final int NAME_SYNCED_WITH_SERVER = 1;
    public static final int NAME_NOT_SYNCED_WITH_SERVER = 0;
    //a broadcast to know weather the data is synced or not
    public static final String DATA_SAVED_BROADCAST = "net.simplifiedcoding.datasaved";

    //Broadcast receiver to know the sync status
    private BroadcastReceiver broadcastReceiver;


    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    private FloatingActionMenu fabmenu;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajker_deal_ekshop__assign__pickup_manager);
        database = new Database(getApplicationContext());
        database.getWritableDatabase();
        executiveLists = new ArrayList<>();
        ajkerdeal_modelList = new ArrayList<>();


        ConnectivityManager cManager = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cManager.getActiveNetworkInfo();

        //Fetching email from shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString(Config.EMAIL_SHARED_PREF, "Not Available");
        String user = username.toString();

        //recycler with cardview
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_assign_ajker_deal_ekshop);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        swipeRefreshLayout = findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        ajkerdeal_modelList.clear();
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

//        loadmainmerchantlist();
//        loadSuppliermerchantlist();
//        loadProductlist();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //loading the names again

            }
        };


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fabmenu = (FloatingActionMenu) findViewById(R.id.menu);
        fab1 = (FloatingActionButton) findViewById(R.id.menu_item1);
        /*     fab2 = (FloatingActionButton) findViewById(R.id.menu_item2);*/

        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             /*   Snackbar.make(view, "Coming soon", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/

                Intent intentorder = new Intent(AjkerDealEkshop_Assign_Pickup_manager.this,
                        NewOrderEntry_ful.class);
                startActivity(intentorder);
            }
        });

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
                                        o.getString("empCode")
                                );
                                executiveLists.add(assignManager_executiveList);
                                database.addexecutivelist(o.getString("userName"), o.getString("empCode"));
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

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        final String match_date = df.format(date);

        StringRequest postRequest1 = new StringRequest(Request.Method.GET, MERCHANT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        SQLiteDatabase sqLiteDatabase = database.getWritableDatabase();
                        database.deletemerchantList_ajkerDeal(sqLiteDatabase);
                        progress.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray array = jsonObject.getJSONArray("summary");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject o = array.getJSONObject(i);
                                AjkerDealAssignManager_Model todaySummary = new AjkerDealAssignManager_Model(
                                        o.getString("merchantName"),
                                        o.getString("companyPhone"),
                                        o.getString("address"),
                                        String.valueOf(o.getInt("apiOrderID")),
                                        o.getString("merOrderRef"),
                                        o.getString("date"));

                                database.addAjkerDeal(
                                        o.getString("merchantName"),
                                        o.getString("companyPhone"),
                                        o.getString("address"),
                                        String.valueOf(o.getInt("apiOrderID")),
                                        o.getString("merOrderRef"),
                                        o.getString("date"));
                                ajkerdeal_modelList.add(todaySummary);
                            }

                            ajkerDealAssignExecutiveAdapter = new AjkerDealAssignExecutiveAdapter(ajkerdeal_modelList, getApplicationContext());
                            recyclerView.setAdapter(ajkerDealAssignExecutiveAdapter);
                            swipeRefreshLayout.setRefreshing(false);
                            ajkerDealAssignExecutiveAdapter.setOnItemClickListener(AjkerDealEkshop_Assign_Pickup_manager.this);

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
        requestQueue.add(postRequest1);
    }

    /* merchant List generation from sqlite*/
    private void getallmerchant() {
/*
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        final String match_date = df.format(date);*/

        try {
            ajkerdeal_modelList.clear();
            SQLiteDatabase sqLiteDatabase = database.getReadableDatabase();
            Cursor c = database.get_ajkerDeal_merchantlist(sqLiteDatabase);

            while (c.moveToNext()) {
                String main_merchant = c.getString(0);
                String supplier_phone = c.getString(1);
                String supplier_address = c.getString(2);
                String supplier_name = c.getString(3);
                String apiOrderID = c.getString(4);
//                String product_name = c.getString(4);
                String product_id = c.getString(5);
//                int sum = c.getInt(6);
                String created_at = c.getString(6);
                AjkerDealAssignManager_Model todaySummary = new AjkerDealAssignManager_Model(main_merchant,supplier_phone, supplier_address, supplier_name,apiOrderID, product_id,created_at);
                ajkerdeal_modelList.add(todaySummary);
            }

            ajkerDealAssignExecutiveAdapter = new AjkerDealAssignExecutiveAdapter(ajkerdeal_modelList, getApplicationContext());
            recyclerView.setAdapter(ajkerDealAssignExecutiveAdapter);
            swipeRefreshLayout.setRefreshing(false);
            ajkerDealAssignExecutiveAdapter.setOnItemClickListener(AjkerDealEkshop_Assign_Pickup_manager.this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Merchant List API hit
    /*private void loadmainmerchantlist() {

        StringRequest postRequest1 = new StringRequest(Request.Method.GET, MAIN_MERCHANT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray array = jsonObject.getJSONArray("summary");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject o = array.getJSONObject(i);
                                database.addfulfillmentmerchantlist(o.getString("main_merchant"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();


                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Check Your Internet Connection", Toast.LENGTH_SHORT).show();
                    }
                }
        ) ;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(postRequest1);
    }

    private void loadSuppliermerchantlist() {

        StringRequest postRequest1 = new StringRequest(Request.Method.GET, SUPPLIER_NAME_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray array = jsonObject.getJSONArray("summary");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject o = array.getJSONObject(i);
                                database.addsuppliernamelist(o.getString("supplier_name"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();


                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Check Your Internet Connection", Toast.LENGTH_SHORT).show();
                    }
                }
        ) ;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(postRequest1);
    }
*/
   /* private void loadProductlist() {

        StringRequest postRequest1 = new StringRequest(Request.Method.GET, PRODUCT_LIST_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray array = jsonObject.getJSONArray("summary");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject o = array.getJSONObject(i);
                                database.addproductlist(
                                        o.getString("product_name"),
                                        String.valueOf(o.getInt("product_id")));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();


                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Check Your Internet Connection", Toast.LENGTH_SHORT).show();
                    }
                }
        ) ;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(postRequest1);
    }*/

//    String status = "0";
    private void assignexecutivetosqlite(final String ex_name, final String empcode, final String product_name, final String sum, final String product_id, final String user, final String currentDateTimeString, final int status,final String m_name,final String contactNumber,final String pick_m_name,final String pick_m_address, final String complete_status, final String apiOrderID, final String demo, final String pick_from_merchant_status, final  String received_from_HQ_status) {

        database.assignexecutive(ex_name, empcode, product_name, sum, String.valueOf(product_id), user, currentDateTimeString, status,m_name,contactNumber,pick_m_name,pick_m_address, complete_status, apiOrderID, demo, pick_from_merchant_status, received_from_HQ_status);
        //final int total_assign = database.getTotalOfAmount(merchant_code);
        //final String strI = String.valueOf(total_assign);
        //database.update_row(strI, merchant_code);

    }

    //For assigning executive API into mysql
    private void assignexecutive(final String ex_name, final String empcode,final String product_name, final String sum, final String product_id, final String user, final String currentDateTimeString, final String m_name,final String contactNumber,final String pick_m_name,final String pick_m_address, final String complete_status, final String apiOrderID, final String demo,final String pick_from_merchant_status,final String received_from_HQ_status) {

//        checkDataEntered( sum);
        StringRequest postRequest = new StringRequest(Request.Method.POST, INSERT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                //if there is a success
                                //storing the name to sqlite with status synced
                                assignexecutivetosqlite(ex_name, empcode, product_name, sum,String.valueOf(product_id), user, currentDateTimeString, NAME_SYNCED_WITH_SERVER,m_name,contactNumber,pick_m_name,pick_m_address,complete_status, apiOrderID, demo, pick_from_merchant_status,received_from_HQ_status);
                            } else {
                                //if there is some error
                                //saving the name to sqlite with status unsynced
                                assignexecutivetosqlite(ex_name, empcode, product_name, sum, String.valueOf(product_id), user, currentDateTimeString, NAME_NOT_SYNCED_WITH_SERVER,m_name,contactNumber,pick_m_name,pick_m_address, complete_status, apiOrderID, demo,pick_from_merchant_status,received_from_HQ_status);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        assignexecutivetosqlite(ex_name, empcode,product_name, sum, String.valueOf(product_id), user, currentDateTimeString, NAME_NOT_SYNCED_WITH_SERVER,m_name,contactNumber,pick_m_name,pick_m_address, complete_status, apiOrderID, demo, pick_from_merchant_status, received_from_HQ_status);
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
                params.put("merchant_code", String.valueOf(product_id));
                params.put("assigned_by", user);
                params.put("created_at", currentDateTimeString);
                params.put("merchant_name", m_name);
                params.put("phone_no", contactNumber);
                params.put("p_m_name",pick_m_name);
                params.put("p_m_address",pick_m_address);
                params.put("complete_status","ad");
                params.put("api_order_id",apiOrderID);
                params.put("demo",demo);
                params.put("pick_from_merchant_status",pick_from_merchant_status);
                params.put("received_from_HQ_status",received_from_HQ_status);

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
            super.onBackPressed();
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
                    ajkerDealAssignExecutiveAdapter.getFilter().filter(newText);
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Intent intent_stay = new Intent(AjkerDealEkshop_Assign_Pickup_manager.this, AssignPickup_Manager.class);
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
            Intent homeIntent = new Intent(AjkerDealEkshop_Assign_Pickup_manager.this,
                    ManagerCardMenu.class);
            startActivity(homeIntent);
        } else if (id == R.id.nav_pickDue) {
            Intent pickupIntent = new Intent(AjkerDealEkshop_Assign_Pickup_manager.this,
                    PickupsToday_Manager.class);
            startActivity(pickupIntent);
        } else if (id == R.id.nav_assign) {
            Intent assignIntent = new Intent(AjkerDealEkshop_Assign_Pickup_manager.this,
                    AssignPickup_Manager.class);
            startActivity(assignIntent);
        } else if (id == R.id.nav_fulfill) {
            Intent assignFulfillmentIntent = new Intent(AjkerDealEkshop_Assign_Pickup_manager.this,
                    AjkerDealEkshop_Assign_Pickup_manager.class);
            startActivity(assignFulfillmentIntent);
        }
        /*  else if (id == R.id.nav_pickCompleted) {
            Intent historyIntent = new Intent(AssignPickup_Manager.this,
                    PickupHistory_Manager.class);
            startActivity(historyIntent);
        } */
        else if (id == R.id.nav_logout) {
            //Creating an alert dialog to confirm logout
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Are you sure you want to logout?");
            alertDialogBuilder.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

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
                            Intent intent = new Intent(AjkerDealEkshop_Assign_Pickup_manager.this, LoginActivity.class);
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

        final AjkerDealAssignManager_Model clickeditem = ajkerdeal_modelList.get(position);

        AlertDialog.Builder spinnerBuilder = new AlertDialog.Builder(AjkerDealEkshop_Assign_Pickup_manager.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_spinner_fulfillment, null);
        spinnerBuilder.setTitle("Select executive and assign number.");


        final TextView dialog_mName = mView.findViewById(R.id.dialog_m_name);

        final AutoCompleteTextView mAutoComplete = mView.findViewById(R.id.auto_exe);
        final EditText et1 = mView.findViewById(R.id.spinner1num);
        final TextView tv1 = mView.findViewById(R.id.textView3);
        dialog_mName.setText(clickeditem.getMerchantName());

//        String totalCount = String.valueOf(clickeditem.getSum());
        String totalCount = "0";
        et1.setText(totalCount);

        final String product_name = "Nothing";
        final String product_id = String.valueOf(clickeditem.getMerOrderRef());
        final String m_name = clickeditem.getMerchantName();
        final String contactNumber = clickeditem.getCompanyPhone();
         final String  pick_merchant_name = "ekshop";
         final String apiOrderID = clickeditem.getApiOrderID();
         final String demo = clickeditem.getApiOrderID();
        final String pick_merchant_address = clickeditem.getAddress();
        final String complete_status = "ad";
        final String pick_from_merchant_status = "0";
        final String received_from_HQ_status = "0";
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

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AjkerDealEkshop_Assign_Pickup_manager.this,
                android.R.layout.simple_list_item_1, lables);

        mAutoComplete.setAdapter(adapter);

        spinnerBuilder.setPositiveButton("Assign", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int i1) {


                if(et1.getText().toString().trim().isEmpty()) {
                    tv1.setText("Order count can't be empty");
//                    dialog.equals("Order count can't be empty");


                } else {
                    String empname = mAutoComplete.getText().toString();

                    final String empcode = database.getSelectedEmployeeCode(empname);

                    assignexecutive(empname, empcode, product_name, et1.getText().toString(), product_id, user, currentDateTimeString, m_name, contactNumber, String.valueOf(pick_merchant_name), pick_merchant_address, complete_status, apiOrderID, demo,pick_from_merchant_status,received_from_HQ_status);

                if (!mAutoComplete.getText().toString().isEmpty() || mAutoComplete.getText().toString().equals(null)) {
                    Toast.makeText(AjkerDealEkshop_Assign_Pickup_manager.this, mAutoComplete.getText().toString()
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
        // vwParentRow2.refreshDrawableState();
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
                    assignexecutive(mAutoComplete.getText().toString(), empcode, product_name, et1.getText().toString(), product_id, user, currentDateTimeString, m_name, contactNumber, String.valueOf(pick_merchant_name), pick_merchant_address, complete_status, apiOrderID, demo,pick_from_merchant_status,received_from_HQ_status);

                    if (!mAutoComplete.getText().toString().isEmpty() || mAutoComplete.getText().toString().equals(null)) {
                        Toast.makeText(AjkerDealEkshop_Assign_Pickup_manager.this, mAutoComplete.getText().toString()
                                        + "(" + et1.getText().toString() + ")",
                                Toast.LENGTH_SHORT).show();
                        dialog2.dismiss();

                    }
                }
            }
        });
    }

//    boolean isEmpty(String text){
//        CharSequence et1 = text.toString();
//       return TextUtils.isEmpty(et1);
//    }
//
//    private void checkDataEntered(String et1) {
//         if (isEmpty(et1)){
//             Toast t = Toast.makeText(this, "Enter Order number", Toast.LENGTH_LONG);
//             t.show();
//         }
//    }

    @Override
    public void onItemClick_view(View view2, int position2) {

        final AjkerDealAssignManager_Model clickeditem2 = ajkerdeal_modelList.get(position2);
        SharedPreferences sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString(Config.EMAIL_SHARED_PREF, "Not Available");
        final String user = username.toString();
        final String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        String merchantname = clickeditem2.getMerchantName();
        String p_m_name = "ekshop";
        String product_name = "Nothing";
        String merchantcode = String.valueOf(clickeditem2.getMerOrderRef());
        Intent intent = new Intent(AjkerDealEkshop_Assign_Pickup_manager.this, ViewAssigns.class);
        intent.putExtra("MERCHANTNAME", merchantname);
        intent.putExtra("MERCHANTCODE", merchantcode);
        intent.putExtra("SUBMERCHANT", p_m_name);
        intent.putExtra("PRODUCTNAME", product_name);
        startActivity(intent);

    }


    @Override
    public void onItemClick_update(View view3, int position3) {
        final AjkerDealAssignManager_Model clickeditem3 = ajkerdeal_modelList.get(position3);
        SharedPreferences sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString(Config.EMAIL_SHARED_PREF, "Not Available");
        final String user = username.toString();
        final String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        String merchantname = clickeditem3.getMerchantName();
        String p_m_name = "ekshop";
        String product_name = "Nothing";
        String merchantcode = String.valueOf(clickeditem3.getMerOrderRef());
        Intent intent = new Intent(AjkerDealEkshop_Assign_Pickup_manager.this, UpdateAssigns.class);
        intent.putExtra("MERCHANTNAME", merchantname);
        intent.putExtra("MERCHANTCODE", merchantcode);
        intent.putExtra("SUBMERCHANT", p_m_name);
        intent.putExtra("PRODUCTNAME", product_name);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        ConnectivityManager cManager = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cManager.getActiveNetworkInfo();
        SharedPreferences sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString(Config.EMAIL_SHARED_PREF,"Not Available");
        ajkerdeal_modelList.clear();
        ajkerDealAssignExecutiveAdapter.notifyDataSetChanged();
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

package com.paperflywings.user.paperflyv0.DeliveryApp.DeliveryOfficer.DeliveryDOBankByDO;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.paperflywings.user.paperflyv0.Config;
import com.paperflywings.user.paperflyv0.Databases.BarcodeDbHelper;
import com.paperflywings.user.paperflyv0.DeliveryApp.DeliveryOfficer.DeliveryDOBankByDO.DeliveryDOMultipleBatchSelectByDO.DeliveryDOMultipleBatchSelection;
import com.paperflywings.user.paperflyv0.DeliveryApp.DeliveryOfficer.DeliveryOfficerCTS.DeliveryCTS;
import com.paperflywings.user.paperflyv0.DeliveryApp.DeliveryOfficer.DeliveryOfficerLandingPageTabLayout.DeliveryTablayout;
import com.paperflywings.user.paperflyv0.DeliveryApp.DeliveryOfficer.DeliveryOfficerOnHold.DeliveryOnHold;
import com.paperflywings.user.paperflyv0.DeliveryApp.DeliveryOfficer.DeliveryOfficerPettyCash.DeliveryAddNewExpense;
import com.paperflywings.user.paperflyv0.DeliveryApp.DeliveryOfficer.DeliveryOfficerPreReturn.ReturnRequest;
import com.paperflywings.user.paperflyv0.DeliveryApp.DeliveryOfficer.DeliveryOfficerRTS.Delivery_ReturnToSupervisor;
import com.paperflywings.user.paperflyv0.DeliveryApp.DeliveryOfficer.DeliveryOfficerUnpicked.DeliveryOfficerUnpicked;
import com.paperflywings.user.paperflyv0.DeliveryApp.DeliveryOfficer.DeliveyrOfficerWithoutStatus.DeliveryWithoutStatus;
import com.paperflywings.user.paperflyv0.DeliveryApp.DeliverySupervisor.BankFragmentContent.BankDepositeBySUP.MultipleBankDepositeBySUP;
import com.paperflywings.user.paperflyv0.DeliveryApp.DeliverySupervisor.BankFragmentContent.Bank_DepositeSlip_Image;
import com.paperflywings.user.paperflyv0.LoginActivity;
import com.paperflywings.user.paperflyv0.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*public class DeliveryDOBankByDO extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {*/

public class DeliveryDOBankByDO extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,DeliveryDOBankByDOAdapter.OnItemClickListener,SwipeRefreshLayout.OnRefreshListener {

    BarcodeDbHelper db;
    public int totalCash = 0;
    private long mLastClickTime = 0;
    public SwipeRefreshLayout swipeRefreshLayout;
    boolean is_in_Action = false;
    private DeliveryDOBankByDOAdapter deliveryDOBankByDOAdapter;
    RecyclerView recyclerView_pul;
    RecyclerView.LayoutManager layoutManager_pul;
    private RequestQueue requestQueue;
    private ProgressDialog progress;
    private TextView btnselect, btndeselect,  totalOrdersSelected;
    public TextView totalCashCollection;
    private Button btnnext,btnpendingdo;

    String itemOrders = "";
    String itemPrimaryIds = "";
    String serialNo = "";
    int count = 0;
    int clickCount = 0;
    String totalCash1 = "";
    public static final String CTS_BY = "cts_by";
    public static final String TOTAL_CASH = "total_cash";
    public static final String SERIAL_NO = "serial_no";
    public static final String TOTAL_ORDER= "total_order";
    private List<DeliveryDOBankByDOModel> list;
    public static final String DELIVERY_SUPERVISOR_API= "http://paperflybd.com/DeliverySupervisorAPI.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new BarcodeDbHelper(getApplicationContext());
        db.getWritableDatabase();

        setContentView(R.layout.activity_delivery_dobank_by_do);
        totalCashCollection = findViewById(R.id.CashCollectionForBank);
        btnpendingdo = findViewById(R.id.btn_pending_bank_depo_by_do);
        totalOrdersSelected = findViewById(R.id.CTS_id_);
        btnselect = findViewById(R.id.selectForBank);
        btndeselect = findViewById(R.id.deselectForBank);
        btnnext = findViewById(R.id.nextForBank);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        recyclerView_pul = findViewById(R.id.recycler_view_cash_receive_list_by_do);
        recyclerView_pul.setAdapter(deliveryDOBankByDOAdapter);
        layoutManager_pul = new LinearLayoutManager(this);
        recyclerView_pul.setLayoutManager(layoutManager_pul);

        list = new ArrayList<DeliveryDOBankByDOModel>();

        SharedPreferences sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        final String username = sharedPreferences.getString(Config.EMAIL_SHARED_PREF, "Not Available");
        final String pointCode = sharedPreferences.getString(Config.SELECTED_POINTCODE_SHARED_PREF, "ALL");
        Toast.makeText(this, "PointCode: " + pointCode, Toast.LENGTH_SHORT).show();

        ConnectivityManager cManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        final NetworkInfo nInfo = cManager.getActiveNetworkInfo();

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        list.clear();

        swipeRefreshLayout.setRefreshing(true);

        if (nInfo != null && nInfo.isConnected()) {
            loadCashReceiveData(username, pointCode);
        } else {
            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
        }

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.delivery_officer_name);
        navUsername.setText(username);
        navigationView.setNavigationItemSelectedListener(this);

        btnpendingdo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pendingbankdepo = new Intent(DeliveryDOBankByDO.this, DeliveryDOMultipleBatchSelection.class);
                startActivity(pendingbankdepo);
            }
        });

        btnselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                totalCash = 0;
                try {
                    list = getModel(true);
                    deliveryDOBankByDOAdapter = new DeliveryDOBankByDOAdapter(list, getApplicationContext());
                    recyclerView_pul.setAdapter(deliveryDOBankByDOAdapter);
                    deliveryDOBankByDOAdapter.setOnItemClickListener(DeliveryDOBankByDO.this);
                } catch (NullPointerException e) {
                    Toast.makeText(DeliveryDOBankByDO.this, "Nothing to select", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btndeselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{

                    list = getModel(false);
                    deliveryDOBankByDOAdapter = new DeliveryDOBankByDOAdapter(list,getApplicationContext());
                    recyclerView_pul.setAdapter(deliveryDOBankByDOAdapter);
                    deliveryDOBankByDOAdapter.setOnItemClickListener(DeliveryDOBankByDO.this);
                } catch (NullPointerException e){
                    Toast.makeText(DeliveryDOBankByDO.this, "Nothing to select", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                totalCash = 0;
                itemOrders = "";
                itemPrimaryIds = "";
                serialNo = "";
                // mis-clicking prevention, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                int count=0;
                SharedPreferences sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                final String username = sharedPreferences.getString(Config.EMAIL_SHARED_PREF,"Not Available");

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DeliveryDOBankByDO.this);
                final View mView = getLayoutInflater().inflate(R.layout.bank_deposite_accept_by_sup, null);
                final TextView tv = mView.findViewById(R.id.tv);
                final TextView error_msg = mView.findViewById(R.id.error_msg111);

                for (int i = 0; i < DeliveryDOBankByDOAdapter.imageModelArrayList.size(); i++){
                    if(DeliveryDOBankByDOAdapter.imageModelArrayList.get(i).getSelected()) {
                        count++;
                        itemOrders = itemOrders + "," + DeliveryDOBankByDOAdapter.imageModelArrayList.get(i).getOrderidList();
                        itemPrimaryIds = itemPrimaryIds + "," + DeliveryDOBankByDOAdapter.imageModelArrayList.get(i).getOrdPrimaryKey();
                        serialNo = serialNo + "," + DeliveryDOBankByDOAdapter.imageModelArrayList.get(i).getSerialNo();
                    }
                    tv.setText(count + " batch selected.");
                }
                //orderIds.setText(item);
                count = 0;

                alertDialogBuilder.setPositiveButton("Submit",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                            }
                        });

                alertDialogBuilder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                itemOrders = "";
                                itemPrimaryIds = "";
                                serialNo = "";
                            }
                        });
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setView(mView);

                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (SystemClock.elapsedRealtime() - mLastClickTime < 500){
                            return;
                        }
                        mLastClickTime = SystemClock.elapsedRealtime();

                        try{
                            String totalCash = String.valueOf(db.getTotalReceivedCash());
                            Intent intent = new Intent(DeliveryDOBankByDO.this, MultipleBankDepositeBySUP.class);
                            if (itemOrders.equals("")){
                                error_msg.setText("Please Select Batch First!!");
                            } else if(tv.getText().equals("0 batches have been selected.")){
                                error_msg.setText("Please Select Batch First!!");
                            } else {
                                UpdateBankInfo(username,itemPrimaryIds,serialNo,itemOrders, totalCash, totalCash,"C","P");
                            }
                            intent.putExtra(TOTAL_CASH, totalCash);

                        } catch (NullPointerException e){
                            Toast.makeText(DeliveryDOBankByDO.this, "Nothing to bank", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void UpdateBankInfo(final String createdBy,final String sqlPrimaryIds,final String serialNo,final String items,final String totalCashAmt,final String submittedCashAmt,final String CashComment, final String type) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, DELIVERY_SUPERVISOR_API,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                //Intent intentBankDeposite = new Intent(DeliveryDOBankByDO.this, MultipleBankDepositeBySUP.class);
                                Intent intentBankDeposite = new Intent(DeliveryDOBankByDO.this, DeliveryDOMultipleBatchSelection.class);
                                startActivity(intentBankDeposite);
                                Toast.makeText(DeliveryDOBankByDO.this, "Successful", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(DeliveryDOBankByDO.this, "UnSuccessful", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(DeliveryDOBankByDO.this, "Server disconnected! "+error, Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", createdBy);
                params.put("cashSubmissionType", type);
                params.put("sqlPrimaryId", sqlPrimaryIds);
                params.put("serialNo", serialNo);
                params.put("flagreq", "delivery_supervisor_bank_orders");
                params.put("orderid", items);
                params.put("totalCashAmt", totalCashAmt);
                params.put("submittedCashAmt", submittedCashAmt);
                params.put("comment", CashComment);
                return params;
            }
        };
        try {
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(this);
            }
            requestQueue.add(postRequest);
        } catch (Exception e) {
            Toast.makeText(DeliveryDOBankByDO.this, "Server Error! cts", Toast.LENGTH_LONG).show();
        }
    }

    public ArrayList<DeliveryDOBankByDOModel> getModel(boolean isSelect){
        ArrayList<DeliveryDOBankByDOModel> listOfOrders = new ArrayList<>();

        if(isSelect == true){

            int totalOrders = db.getTotalOrders();
            totalOrdersSelected.setText(totalOrders+"");

            for(int i = 0; i < list.size(); i++){
                DeliveryDOBankByDOModel model = new DeliveryDOBankByDOModel();

                model.setSelected(isSelect);
                model.setOrderidList(list.get(i).getOrderidList());
                model.setOrdPrimaryKey(list.get(i).getOrdPrimaryKey());
                model.setSerialNo(list.get(i).getSerialNo());
                model.setTotalOrders(list.get(i).getTotalOrders());
                model.setCtsBy(list.get(i).getCtsBy());
                model.setCtsTime(list.get(i).getCtsTime());
                model.setTotalCashAmt(list.get(i).getTotalCashAmt());
                model.setSubmittedCashAmt(list.get(i).getSubmittedCashAmt());
                model.setTotalCashReceive(String.valueOf(list.get(i).getTotalCashReceive()));
                model.setCts(list.get(i).getCts());
                listOfOrders.add(model);

            }

        } else if(isSelect == false){
            totalOrdersSelected.setText("0");

            for(int i = 0; i < list.size(); i++){
                DeliveryDOBankByDOModel model = new DeliveryDOBankByDOModel();

                model.setSelected(isSelect);
                model.setOrderidList(list.get(i).getOrderidList());
                model.setOrdPrimaryKey(list.get(i).getOrdPrimaryKey());
                model.setSerialNo(list.get(i).getSerialNo());
                model.setTotalOrders(list.get(i).getTotalOrders());
                model.setCtsBy(list.get(i).getCtsBy());
                model.setCtsTime(list.get(i).getCtsTime());
                model.setTotalCashAmt(list.get(i).getTotalCashAmt());
                model.setTotalCashAmt(list.get(i).getTotalCashAmt());
                model.setSubmittedCashAmt(list.get(i).getSubmittedCashAmt());
                model.setTotalCashReceive(list.get(i).getTotalCashReceive());

                model.setCts(list.get(i).getCts());
                listOfOrders.add(model);
            }
        }

        for (int i = 0; i < listOfOrders.size(); i++){
            if(listOfOrders.get(i).getSelected() == true) {
                if (!listOfOrders.contains(i)) {
                    totalCash = totalCash + Integer.parseInt(String.valueOf(listOfOrders.get(i).getTotalCashReceive()));
                }else {
                   // totalCash = totalCash + 0;
                }
            } else {
                if(totalCash != 0){
                    totalCash = totalCash - Integer.parseInt(String.valueOf(listOfOrders.get(i).getTotalCashReceive()));
                } else {
                    totalCash = 0;
                }
            }
            // tv.setText(count + " Orders have been selected for cash.");
        }

        totalCashCollection.setText(totalCash+" Taka");
        return listOfOrders;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            Intent intent = new Intent(getApplicationContext(), DeliveryTablayout.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
                // deliveryCashReceiveSupervisorAdapter.getFilter().filter(newText);
                return false;
            }
        });
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Intent intent_stay = new Intent(DeliveryDOBankByDO.this, DeliveryDOBankByDO.class);
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
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent homeIntent = new Intent(DeliveryDOBankByDO.this,
                    DeliveryTablayout.class);
            startActivity(homeIntent);
            // Handle the camera action
        } else if (id == R.id.nav_unpicked) {
            Intent homeIntent = new Intent(DeliveryDOBankByDO.this,
                    DeliveryOfficerUnpicked.class);
            startActivity(homeIntent);
            // Handle the camera action
        } else if (id == R.id.nav_without_status) {
            Intent homeIntent = new Intent(DeliveryDOBankByDO.this,
                    DeliveryWithoutStatus.class);
            startActivity(homeIntent);
            // Handle the camera action
        } else if (id == R.id.nav_on_hold) {
            Intent homeIntent = new Intent(DeliveryDOBankByDO.this,
                    DeliveryOnHold.class);
            startActivity(homeIntent);
            // Handle the camera action
        } else if (id == R.id.nav_return_request) {
            Intent homeIntent = new Intent(DeliveryDOBankByDO.this,
                    ReturnRequest.class);
            startActivity(homeIntent);
            // Handle the camera action
        } else if (id == R.id.nav_return) {
            Intent homeIntent = new Intent(DeliveryDOBankByDO.this,
                    Delivery_ReturnToSupervisor.class);
            startActivity(homeIntent);
            // Handle the camera action
        } else if (id == R.id.nav_new_expense) {
            Intent expenseIntent = new Intent(DeliveryDOBankByDO.this,
                    DeliveryAddNewExpense.class);
            startActivity(expenseIntent);
        } else if (id == R.id.nav_cash) {
            Intent homeIntent = new Intent(DeliveryDOBankByDO.this,
                    DeliveryCTS.class);
            startActivity(homeIntent);
        } else if (id == R.id.nav_logout) {
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
                            Intent intent = new Intent(DeliveryDOBankByDO.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    });

            alertDialogBuilder.setNegativeButton("No",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadCashReceiveData (final String username,final String pointCode){
        progress=new ProgressDialog(this);
        progress.setMessage("Loading Data");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setCancelable(false);
        progress.setIndeterminate(true);
        progress.setProgress(0);
        progress.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, DELIVERY_SUPERVISOR_API,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        SQLiteDatabase sqLiteDatabase = db.getWritableDatabase();
                        db.deleteBannkDepositeList(sqLiteDatabase);

                        progress.dismiss();
                        swipeRefreshLayout.setRefreshing(false);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray array = jsonObject.getJSONArray("getData");


                            for(int i =0;i<array.length();i++)
                            {
                                JSONObject o = array.getJSONObject(i);
                                DeliveryDOBankByDOModel withoutStatus_model = new DeliveryDOBankByDOModel(
                                        o.getInt("id"),
                                        o.getString("orderidList"),
                                        o.getString("totalCashReceive"),
                                        o.getString("ordPrimaryKey"),
                                        o.getString("serialNoCTRS"),
                                        o.getString("totalOrders"),
                                        o.getString("totalCashAmt"),
                                        o.getString("submittedCashAmt"),
                                        o.getString("dropPointEmp"),
                                        o.getString("dropPointCode"),
                                        o.getString("CashAmt"),
                                        o.getString("partialReceive"),
                                        o.getString("packagePrice"),
                                        o.getString("CTS"),
                                        o.getString("CTSTime"),
                                        o.getString("CTSBy"),
                                        o.getString("CRSTime"),
                                        o.getString("CRSBy"));

                               /* db.insertCash(o.getString("packagePrice"),
                                        o.getString("CashAmt"));*/

                                db.insert_delivery_Cash_to_bank(
                                        o.getInt("id"),
                                        o.getString("totalCashReceive"),
                                        o.getString("serialNoCTRS"),
                                        o.getString("totalOrders"),
                                        o.getString("submittedCashAmt"),
                                        o.getString("dropPointEmp"),
                                        o.getString("dropPointCode"),
                                        o.getString("CashAmt"));

                                list.add(withoutStatus_model);
                            }

                            deliveryDOBankByDOAdapter = new DeliveryDOBankByDOAdapter(list,getApplicationContext());
                            recyclerView_pul.setAdapter(deliveryDOBankByDOAdapter);
                            deliveryDOBankByDOAdapter.setOnItemClickListener(DeliveryDOBankByDO.this);

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
                        Toast.makeText(getApplicationContext(), "Internet Connection lost!" ,Toast.LENGTH_LONG).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String,String> params1 = new HashMap<String,String>();
                params1.put("username", username);
                params1.put("pointCode", pointCode);
                params1.put("flagreq", "delivery_cash_recv_orders");
                return params1;
            }
        };

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(this);
        }
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    @Override
    public void onRefresh() {
        ConnectivityManager cManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cManager.getActiveNetworkInfo();

        SharedPreferences sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString(Config.EMAIL_SHARED_PREF,"Not Available");
        final String pointCode = sharedPreferences.getString(Config.SELECTED_POINTCODE_SHARED_PREF, "ALL");

        list.clear();
        deliveryDOBankByDOAdapter.notifyDataSetChanged();
        if(nInfo!= null && nInfo.isConnected())
        {
            loadCashReceiveData(username,pointCode);
            totalCashCollection.setText("0");
            totalOrdersSelected.setText("0");
            totalCash = 0;
            serialNo = "";
            itemPrimaryIds = "";
            itemOrders = "";
        }
        else{
            Toast.makeText(this,"No Internet Connection",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemClick_view(View view, int position) {
        DeliveryDOBankByDOModel clickedItem = list.get(position);

        String orderIdList = clickedItem.getOrderidList();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Order Ids: "+orderIdList);
        alertDialogBuilder.setNegativeButton("Close",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    @Override
    public void onItemClick_view_image(View view1, int position1) {
        DeliveryDOBankByDOModel clickedItem1 = list.get(position1);
        String serialNo = clickedItem1.getSerialNo();
        Intent intent = new Intent(getApplication(), Bank_DepositeSlip_Image.class);
        intent.putExtra(SERIAL_NO, serialNo);
        startActivity(intent);
    }

    @Override
    public void onItemClick_view_cashCount(View view2, int i) {
        if(((CheckBox)view2).isChecked()){
            if (!list.contains(i)) {
                totalCash = totalCash + Integer.parseInt(String.valueOf(list.get(i).getTotalCashReceive()));
            }else {

            }
        } else {
            if(totalCash != 0){
                totalCash = totalCash - Integer.parseInt(String.valueOf(list.get(i).getTotalCashReceive()));
            } else {
                totalCash = 0;
            }
        }

        totalCashCollection.setText(totalCash+" Taka");
    }
}

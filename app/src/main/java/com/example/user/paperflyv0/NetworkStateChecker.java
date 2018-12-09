package com.example.user.paperflyv0;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class NetworkStateChecker extends BroadcastReceiver {

    //context and database helper object
    private Context context;
    private Database database;
    private BarcodeDbHelper database2;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        database = new Database(context);
        database2 = new BarcodeDbHelper(context);
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        //if there is a network
        if (activeNetwork != null) {
            //if connected to wifi or mobile data plan
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                //getting all the unsynced names
                Cursor cursor = database.getUnsyncedassignment();
                if (cursor.moveToFirst()) {
                    do {
                        //calling the method to save the unsynced name to MySQL
                        saveName(cursor.getInt(7),cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getString(8),cursor.getString(9),cursor.getString(10),cursor.getString(11));
                    } while (cursor.moveToNext());
                }

                //getting all the unsynced barcode
                Cursor cursor1 = database2.getUnsyncedBarcode();
                if (cursor1.moveToFirst()) {
                    do {
                        //calling the method to save the unsynced name to MySQL
                        saveBarcode(cursor1.getInt(0),cursor1.getString(1), cursor1.getString(2),cursor1.getString(3), Boolean.valueOf(cursor1.getString(4)),cursor1.getString(6),cursor1.getString(7));
                    } while (cursor1.moveToNext());
                }
                //getting all the unsynced data
                Cursor cursor2 = database2.getUnsyncedData();
                if (cursor2.moveToFirst()) {
                    do {

                        //calling the method to save the unsynced name to MySQL
                        saveData(cursor2.getInt(0),cursor2.getString(6),cursor2.getString(10),cursor2.getString(11),cursor2.getString(1), cursor2.getString(14), cursor2.getString(9));
                    } while (cursor2.moveToNext());
                }
            }
        }
    }

    /*
     * method taking two arguments
     * name that is to be saved and id of the name from SQLite
     * if the name is successfully sent
     * we will update the status as synced in SQLite
     * */
    private void saveName(final int id, final String executive_name,final String executive_code, final String order_count,final String merchant_code,final String assigned_by,final String created_at,final String m_name,final String contactNumber,final String pick_m_name,final String pick_m_address) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, AssignPickup_Manager.INSERT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                //updating the status in sqlite
                                database.updateAssignStatus(id, AssignPickup_Manager.NAME_SYNCED_WITH_SERVER);

                                //sending the broadcast to refresh the list
                                context.sendBroadcast(new Intent(AssignPickup_Manager.DATA_SAVED_BROADCAST));
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
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("executive_name", executive_name);
                params.put("executive_code", executive_code);
                params.put("order_count", order_count);
                params.put("merchant_code", merchant_code);
                params.put("assigned_by", assigned_by);
                params.put("created_at", created_at);
                params.put("merchant_name", m_name);
                params.put("phone_no", contactNumber);
                params.put("p_m_name",pick_m_name);
                params.put("p_m_address",pick_m_address);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    //Update Sync
   /* private void UpdateSync(final int id,final String merchantcode, final String empcode,final String cou) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://192.168.0.111/new/updateassign.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                //updating the status in sqlite
                                database.updateTheUpdateStatus(id, UpdateAssigns.UPDATE_SYNCED_WITH_SERVER);
                                //sending the broadcast to refresh the list
                                context.sendBroadcast(new Intent(UpdateAssigns.DATA_SAVED_BROADCAST));
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
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("merchant_code", merchantcode);
                params.put("executive_code", empcode);
                params.put("order_count", cou);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }
*/
    //Barcode save to server
    private void saveBarcode(final int id,final String merchant_id, final String sub_merchant_name, final String lastText, final Boolean state, final String updated_by, final String updated_at) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://paperflybd.com/insert_barcode.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                //updating the status in sqlite
                                database2.updateBarcodeStatus(id, ScanningScreen.NAME_SYNCED_WITH_SERVER);
                                //sending the broadcast to refresh the list
                                context.sendBroadcast(new Intent(ScanningScreen.DATA_SAVED_BROADCAST));
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
                }) {
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
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    //Update scan count
    private void saveData(final int id, final String strI, final String updated_by, final String updated_at, final String merchant_id, final String sub_merchant_name, final String match_date) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://paperflybd.com/updateTable.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                //updating the status in sqlite
                                database2.updateDataStatus(id, ScanningScreen.NAME_SYNCED_WITH_SERVER);
                                //sending the broadcast to refresh the list
                                context.sendBroadcast(new Intent(ScanningScreen.DATA_SAVED_BROADCAST));
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
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("scan_count", strI);
                params.put("updated_by", updated_by);
                params.put("updated_at", updated_at);
                params.put("merchant_code", merchant_id);
                params.put("p_m_name", sub_merchant_name);
                params.put("created_at", match_date);
                return params;

            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }
}

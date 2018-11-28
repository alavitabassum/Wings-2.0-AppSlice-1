package com.example.user.paperflyv0;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.user.paperflyv0.AssignPickup_Manager.NAME_NOT_SYNCED_WITH_SERVER;

public class UpdateAssignsAdapter extends RecyclerView.Adapter<UpdateAssignsAdapter.ViewHolder> {

    private List<UpdateAssign_Model> updateAssignModelList;
    private Context context;
    private OnEditTextChanged onEditTextChanged;
    public static final String UPDATE_URL = "http://192.168.0.117/new/updateassign.php";
    public static final String DELETE_URL = "http://192.168.0.117/new/deleteasign.php";


    Database database;
    String merchantcode;
    List<AssignManager_ExecutiveList> executiveLists;

    public interface OnEditTextChanged {
        void onTextChanged(int position, String charSeq);
    }

    public UpdateAssignsAdapter(List<UpdateAssign_Model> updateAssignModelList, Context context,String merchantcode,OnEditTextChanged onEditTextChanged) {
        this.updateAssignModelList = updateAssignModelList;
        this.context = context;
        this.merchantcode = merchantcode;
        this.onEditTextChanged = onEditTextChanged;

    }

    class ViewHolder extends RecyclerView.ViewHolder{

        public AutoCompleteTextView itemExe;
        public TextView itemCount;
        public Button button;
        public Button deletebutton;


        public ViewHolder(View itemView) {
            super(itemView);
            database = new Database(context);
            executiveLists = new ArrayList<>();
            getallexecutives();
            itemExe = (AutoCompleteTextView)itemView.findViewById(R.id.auto_complete);
            itemCount = (TextView)itemView.findViewById(R.id.order_count);
            button = (Button) itemView.findViewById(R.id.update_assigns);
            deletebutton = (Button) itemView.findViewById(R.id.delete_assigns);

            itemExe.setDropDownBackgroundDrawable(new ColorDrawable(context.getResources().getColor(R.color.black)));
            itemExe.setTextColor(itemView.getResources().getColor(R.color.black));
            }
    }

    @Override
    public UpdateAssignsAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.update_assigns_layout, viewGroup, false);
        UpdateAssignsAdapter.ViewHolder viewHolder = new UpdateAssignsAdapter.ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final UpdateAssignsAdapter.ViewHolder viewHolder, final int j) {
        final UpdateAssign_Model updateAssign_model = updateAssignModelList.get(j);
        viewHolder.itemExe.setText(updateAssign_model.getEx_name());
        viewHolder.itemCount.setText(updateAssign_model.getCount());
        viewHolder.deletebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ex = updateAssign_model.getEx_name().toString();
                final String empcode = database.getSelectedEmployeeCode(ex);
                String count = updateAssign_model.getCount().toString();
                String rowid = updateAssign_model.getRowid();
                database.deleteassign(rowid,ex,count);
                assignexecutivedelete(merchantcode,empcode);
                Toast.makeText(context, "Deleted" ,Toast.LENGTH_SHORT).show();
            }
        });

        /*autocomplete*/

        List<String> lables = new ArrayList<String>();

            for (int z = 0; z < executiveLists.size(); z++) {
                lables.add(executiveLists.get(z).getExecutive_name());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                    android.R.layout.simple_list_item_1, lables);
                   adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewHolder.itemExe.setAdapter(adapter);
        viewHolder.itemExe.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try{onEditTextChanged.onTextChanged(j, charSequence.toString());}catch (Exception e){

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                /*final String beforeempname = viewHolder.itemExe.getText().toString();
                final String beforeempcode = database.getSelectedEmployeeCode(beforeempname);*/
                final String cou = viewHolder.itemCount.getText().toString();
                final String empname = editable.toString();
                final String empcode = database.getSelectedEmployeeCode(empname);
                viewHolder.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String rowid = updateAssign_model.getRowid();
                        database.updateassign(rowid,empname,empcode,cou);
                        Toast.makeText(context, "updated" ,Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        viewHolder.itemCount.addTextChangedListener(new TextWatcher() {
            final String empname = viewHolder.itemExe.getText().toString();
            final String empcode = database.getSelectedEmployeeCode(empname);

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try{onEditTextChanged.onTextChanged(j, charSequence.toString());}catch (Exception e){

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

                final String cou = editable.toString();
                viewHolder.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String rowid = updateAssign_model.getRowid();
                        database.updateassign(rowid,empname,empcode,cou);
                        assignexecutiveupdate(merchantcode,empcode,cou);
                        Toast.makeText(context, "updated" ,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        }



    @Override
    public int getItemCount() {
        return updateAssignModelList.size();
    }

    private void assignexecutivedelete(final String merchantcode, final String empcode) {


        StringRequest postRequest = new StringRequest(Request.Method.POST, DELETE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        //   Log.d("Error",error);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("merchant_code", merchantcode);
                params.put("executive_code", empcode);
                return params;
            }

        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(postRequest);

    }

   /* private String getrowid(final String merchantcode, final String empcode) {


        StringRequest postRequest = new StringRequest(Request.Method.POST, "http://192.168.0.111/new/getrowid.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                       try{
                           JSONArray arr = new JSONArray(response);
                           JSONObject jObj = arr.getJSONObject(0);
                           String id = jObj.getString("id");



                       }catch (Exception e)
                       {

                       }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        //   Log.d("Error",error);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("merchant_code", merchantcode);
                params.put("executive_code", empcode);
                return params;
            }

        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(postRequest);
        return id;

    }
*/

    private void assignexecutiveupdate(final String merchantcode, final String empcode,final String cou) {


        StringRequest postRequest = new StringRequest(Request.Method.POST, UPDATE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


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
                Map<String, String> params = new HashMap<String, String>();
                params.put("merchant_code", merchantcode);
                params.put("executive_code", empcode);
                params.put("order_count", cou);
                return params;
            }

        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(postRequest);

    }

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
}

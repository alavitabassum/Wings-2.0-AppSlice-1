package com.example.user.paperflyv0;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class pickuplistForExecutiveAdapter extends RecyclerView.Adapter<pickuplistForExecutiveAdapter.ViewHolder>implements Filterable  {

    private List<PickupList_Model_For_Executive> list;
    private List<PickupList_Model_For_Executive> listFull;

    private int currentPosition =-1;


    private Context context;
    private OnItemClickListener mListner;
    private RecyclerView.OnItemTouchListener touchListener;
    BarcodeDbHelper db;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;

 /*   final CharSequence[] status_options = {"Cancel","Pending"};
    int selection = 1;*/

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listner) {
        this.mListner = listner;
    }

    public void setOnItemTouchListener(RecyclerView.OnItemTouchListener t_listener){
        this.touchListener = t_listener;
    }

    public pickuplistForExecutiveAdapter(List<PickupList_Model_For_Executive> list, Context context) {
        this.list = list;
        this.context = context;
        listFull = new ArrayList<>(list);
    }


    class ViewHolder extends RecyclerView.ViewHolder{

        public TextView item_m_pul;
        public TextView item_p_m_name_pul;
        public TextView item_p_m_add_pul;
        public TextView itme_a_pul;
        public TextView item_scanCount;
        public TextView text_scanCount;
        public TextView item_productName;
        public TextView text_productName;
        public TextView item_pickedCount;
        public TextView text_pickedCount;
        public Button scan_button;
        public TextView item_phnNum;
        public  Button itemStatus;
        public CardView cardview;


        public ViewHolder(final View itemView, int i) {
            super(itemView);
            item_m_pul=itemView.findViewById(R.id.m_name_pul);
            item_p_m_name_pul=itemView.findViewById(R.id.childMerchant_name_pul);
            item_p_m_add_pul=itemView.findViewById(R.id.childMerchant_address_pul);
//            item_add_pul=itemView.findViewById(R.id.m_add_pul);
            itme_a_pul=itemView.findViewById(R.id.a_qty_pul);

            item_scanCount=itemView.findViewById(R.id.scan_qty_pul);
            text_scanCount=itemView.findViewById(R.id.scantxt);
            item_productName=itemView.findViewById(R.id.product_name_pul);
            text_productName=itemView.findViewById(R.id.product_name);
            item_pickedCount=itemView.findViewById(R.id.picked_qty_pul);
            text_pickedCount=itemView.findViewById(R.id.pickedtxt);
            item_phnNum = itemView.findViewById(R.id.m_phn_num);
            scan_button = itemView.findViewById(R.id.btn_scan);
            itemStatus = itemView.findViewById(R.id.btn_status);
            cardview = itemView.findViewById(R.id.card_view_pu_list);


            //underline phoneNumber
            item_phnNum.setPaintFlags(item_phnNum.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            item_phnNum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent callIntent =new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:01781278896"));
                    if (ActivityCompat.checkSelfPermission(v.getContext(),
                            Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) v.getContext(),
                                new String[]{Manifest.permission.CALL_PHONE},
                                MY_PERMISSIONS_REQUEST_CALL_PHONE);
                        return;
                    }
                    v.getContext().startActivity(callIntent);
                }
            });

            scan_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mListner != null) {
                        // Position of the item will be saved in this variable
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListner.onItemClick(itemView, position);
                        }
                    }
                }
            });

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.my_pickups_exe_layout, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v, i);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {

        viewHolder.item_m_pul.setText(list.get(i).getMerchant_name());

        viewHolder.item_p_m_name_pul.setText(list.get(i).getP_m_name());
        viewHolder.item_p_m_add_pul.setText(list.get(i).getP_m_add());
        viewHolder.itme_a_pul.setText(list.get(i).getAssined_qty());
        viewHolder.item_phnNum.setText(list.get(i).getPhone_no());

        String complete_status = list.get(i).getComplete_status();


        if(complete_status.equals("p")) {
            viewHolder.text_scanCount.setText("Scan Count: ");
            viewHolder.item_scanCount.setText(list.get(i).getScan_count());
        } else if ( complete_status.equals("f")) {
            viewHolder.text_productName.setText("Product: ");
            viewHolder.item_productName.setText(list.get(i).getProduct_name());
            viewHolder.text_pickedCount.setText("Picked: ");
            viewHolder.item_pickedCount.setText(list.get(i).getPicked_qty());
        }

        try {
            int count_assigned = Integer.parseInt(list.get(i).getAssined_qty());
            int count_picked = Integer.parseInt(list.get(i).getPicked_qty());
            int count = Integer.parseInt(list.get(i).getScan_count());

            if (count == count_assigned || count > count_assigned && complete_status.equals("p")){
                viewHolder.itemStatus.setText("Complete");
                viewHolder.itemStatus.setBackgroundResource(R.color.green);
                viewHolder.itemStatus.setTextColor(Color.WHITE);
                viewHolder.itemStatus.setEnabled(false);
            } else if (count < count_assigned && complete_status.equals("p")){
                viewHolder.itemStatus.setText("Pending");
                viewHolder.itemStatus.setBackgroundResource(R.color.yellow);
                viewHolder.itemStatus.setTextColor(Color.BLACK);
                viewHolder.itemStatus.setEnabled(true);
            } else if (count_picked ==  count_assigned || count_picked > count_assigned && complete_status.equals("f")){
                viewHolder.itemStatus.setText("Complete");
                viewHolder.itemStatus.setBackgroundResource(R.color.green);
                viewHolder.itemStatus.setTextColor(Color.WHITE);
                viewHolder.itemStatus.setEnabled(false);
            } else if(count_picked < count_assigned && complete_status.equals("f")) {
                viewHolder.itemStatus.setText("Pending");
                viewHolder.itemStatus.setBackgroundResource(R.color.yellow);
                viewHolder.itemStatus.setTextColor(Color.BLACK);
                viewHolder.itemStatus.setEnabled(true);
            }
        } catch(Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Status not changed" +e ,Toast.LENGTH_SHORT).show();
        }

        if(list.get(i).getP_m_name().equals("")) {
            viewHolder.item_p_m_name_pul.setText("No Submerchant");
        }

        if(list.get(i).getP_m_add().equals("")) {
            viewHolder.item_p_m_add_pul.setText("No Address");
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    //search/filter list
    @Override
    public Filter getFilter() {
        return NamesFilter;
    }
    private Filter NamesFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<PickupList_Model_For_Executive> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0){
                filteredList.addAll(listFull);
            }else{
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (PickupList_Model_For_Executive item : listFull){
                    if (item.getMerchant_name().toLowerCase().contains(filterPattern)){
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            list.clear();
            list.addAll((List) results.values);
            notifyDataSetChanged();

        }
    };

}

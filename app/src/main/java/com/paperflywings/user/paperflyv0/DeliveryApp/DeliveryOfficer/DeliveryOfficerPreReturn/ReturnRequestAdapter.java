package com.paperflywings.user.paperflyv0.DeliveryApp.DeliveryOfficer.DeliveryOfficerPreReturn;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.paperflywings.user.paperflyv0.Databases.BarcodeDbHelper;
import com.paperflywings.user.paperflyv0.R;

import java.util.ArrayList;
import java.util.List;

public class ReturnRequestAdapter extends RecyclerView.Adapter<ReturnRequestAdapter.ViewHolder> implements Filterable {
    private List<ReturnRequestModel> listFull;
    private List<ReturnRequestModel> list;
    private int currentPostion = -1;

    private Context context;
    BarcodeDbHelper db;

    public ReturnRequestAdapter(List<ReturnRequestModel> list, Context context) {
        this.list = list;
        this.context = context;
        this.listFull = new ArrayList<>(list);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView item_customerDistrict_without_status;
        public TextView item_barcode_without_status;
        public TextView item_ordId_without_status;
        public TextView item_merOrderRef_without_status;
        public TextView item_merchantName_without_status;
        public TextView item_pickMerchantName_without_status;
        public TextView item_custname_without_status;
        public TextView item_custaddress_without_status;
        public TextView item_custphone_without_status;
        public TextView item_packagePrice_without_status;
        public TextView item_productBrief_without_status;
        public TextView item_deliveryTime_without_status;
        public TextView item_partialreason_without_status;
       // public Button itemStatus_without_status;
        public CardView card_view_without_status;

        public ViewHolder(View itemView, int i) {
            super(itemView);

            item_ordId_without_status=itemView.findViewById(R.id.orderId_without_status);
            item_merOrderRef_without_status=itemView.findViewById(R.id.m_order_ref_without_status);
            item_merchantName_without_status=itemView.findViewById(R.id.m_name_without_status);
            item_partialreason_without_status = itemView.findViewById(R.id.partialReasonText);
            item_pickMerchantName_without_status=itemView.findViewById(R.id.pick_m_name_ret_req);
            item_custname_without_status=itemView.findViewById(R.id.customer_name_without_status);
            item_custaddress_without_status=itemView.findViewById(R.id.customer_Address_without_status);
            item_custphone_without_status=itemView.findViewById(R.id.m_phn_num_without_status);
            item_packagePrice_without_status=itemView.findViewById(R.id.price_without_status);
            item_productBrief_without_status=itemView.findViewById(R.id.package_brief_without_status);
            item_deliveryTime_without_status=itemView.findViewById(R.id.deliverytime);
           // itemStatus_without_status=itemView.findViewById(R.id.btn_status_without_status);
            card_view_without_status=itemView.findViewById(R.id.card_view_delivery_returnr_request_list);

//            item_custphone_without_status.setPaintFlags(item_custphone_without_status.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v  = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.my_return_request,viewGroup,false);
        ViewHolder viewHolder = new ViewHolder(v,i);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.item_ordId_without_status.setText(list.get(i).getOrderid());
        viewHolder.item_merOrderRef_without_status.setText(list.get(i).getMerOrderRef());
        //viewHolder.item_merchantName_without_status.setText(list.get(i).getMerchantName());
        //viewHolder.item_pickMerchantName_without_status.setText("Pick Merchant Name: "+list.get(i).getPickMerchantName());
        viewHolder.item_custname_without_status.setText("Name: "+list.get(i).getCustname());
        viewHolder.item_custaddress_without_status.setText("Address: "+list.get(i).getCustaddress());
        viewHolder.item_custphone_without_status.setText(list.get(i).getCustphone());
        viewHolder.item_packagePrice_without_status.setText(list.get(i).getPackagePrice()+ " Taka");
        viewHolder.item_productBrief_without_status.setText("Product Brief: "+list.get(i).getProductBrief());
        //viewHolder.item_deliveryTime_without_status.setText(list.get(i).getDeliveryTime());

        // viewHolder.item_deliveryTime_without_status.setTextColor(Color.WHITE);

        // String CustomerDistrict = list.get(i).getCustomerDistrict();

        String Merchant_name = list.get(i).getMerchantName();
        String Pick_merchantName = list.get(i).getPickMerchantName();
        String partialReason = list.get(i).getPartialReason();

       // int DeliveryTime = Integer.parseInt(list.get(i).getSlaMiss());

        int DeliveryTime = list.get(i).getSlaMiss();

        if(DeliveryTime<0) {
            viewHolder.item_deliveryTime_without_status.setText(String.valueOf(list.get(i).getSlaMiss()));
            viewHolder.item_deliveryTime_without_status.setBackgroundResource(R.color.red);
            viewHolder.item_deliveryTime_without_status.setTextColor(Color.WHITE);
        }

        else if (DeliveryTime>=0){
            viewHolder.item_deliveryTime_without_status.setText(String.valueOf(list.get(i).getSlaMiss()));
            viewHolder.item_deliveryTime_without_status.setBackgroundResource(R.color.green);
            viewHolder.item_deliveryTime_without_status.setTextColor(Color.WHITE);
        }

        if (Pick_merchantName.isEmpty()) {
            viewHolder.item_merchantName_without_status.setText("Merchant: "+list.get(i).getMerchantName());
        }
        else if(!Pick_merchantName.isEmpty()){
            viewHolder.item_merchantName_without_status.setText("Merchant: "+list.get(i).getPickMerchantName());
        }

        String partial = list.get(i).getPartial();
        String ret = list.get(i).getRet();

       if(partial.equals("Y")){
            viewHolder.item_partialreason_without_status.setText(list.get(i).getPartialReason());
        } else if (partial != "Y"){
            viewHolder.item_partialreason_without_status.setText(list.get(i).getRetReason());
        }

        // if (Pick_merchantName.isEmpty()) {
        //viewHolder.item_merchantName_without_status.setText(list.get(i).getMerchantName());
        //  }
//        else if(!Pick_merchantName.isEmpty()){
//            viewHolder.item_merchantName_without_status.setText(list.get(i).getMerchantName());
//            viewHolder.item_pickMerchantName_without_status.setText("Pick Merchant Name: "+list.get(i).getPickMerchantName());
//        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public Filter getFilter() {
        return NamesFilter;
    }

    private Filter NamesFilter = new Filter() {


        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<ReturnRequestModel>filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0){
                filteredList.addAll(listFull);
            }else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for(ReturnRequestModel item: listFull){
                    if (item.getOrderid().toLowerCase().contains(filterPattern) || item.getMerchantName().toLowerCase().contains(filterPattern) || item.getPickMerchantName().toLowerCase().contains(filterPattern) || item.getCustname().toLowerCase().contains(filterPattern) || item.getCustphone().toLowerCase().contains(filterPattern)){
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

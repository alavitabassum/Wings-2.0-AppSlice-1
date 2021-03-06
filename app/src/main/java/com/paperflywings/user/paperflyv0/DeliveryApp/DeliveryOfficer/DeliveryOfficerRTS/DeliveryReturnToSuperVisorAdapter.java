package com.paperflywings.user.paperflyv0.DeliveryApp.DeliveryOfficer.DeliveryOfficerRTS;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.paperflywings.user.paperflyv0.Databases.BarcodeDbHelper;
import com.paperflywings.user.paperflyv0.R;

import java.util.ArrayList;
import java.util.List;

public class DeliveryReturnToSuperVisorAdapter extends RecyclerView.Adapter<DeliveryReturnToSuperVisorAdapter.ViewHolder> implements Filterable {
    private List<DeliveryReturnToSuperVisorModel> listFull;
    private List<DeliveryReturnToSuperVisorModel> list;
    public static ArrayList<DeliveryReturnToSuperVisorModel> imageModelArrayList1;
    private int currentPostion = -1;
    private Context context;
    private DeliveryReturnToSuperVisorAdapter.OnItemClickListener mListner;
    private RecyclerView.OnItemTouchListener touchListener;
    BarcodeDbHelper db;

    public interface OnItemClickListener {
        void onItemClick_view_details(View view1, int position1);
        void onItemClick_dispute_raise(View view2, int position2);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListner = listener;
    }

    public void setOnItemTouchListener(RecyclerView.OnItemTouchListener t_listener) {
        this.touchListener = t_listener;
    }

    public DeliveryReturnToSuperVisorAdapter(List<DeliveryReturnToSuperVisorModel> list, Context context) {
        this.list = list;
        this.context = context;
        this.listFull = new ArrayList<>(list);
        this.imageModelArrayList1 = new ArrayList<>(list);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView item_ordId_rts;
        public TextView item_retreason_rts;
        public Button item_ret_dispute;
        public CardView card_view_rts;
        protected CheckBox checkBox;

        public ViewHolder(View itemView, int i) {
            super(itemView);

            item_ordId_rts = itemView.findViewById(R.id.orderId_Rts);
            item_retreason_rts = itemView.findViewById(R.id.return_reasons_Rts);
            item_ret_dispute = itemView.findViewById(R.id.disputeBtnReturn);
            card_view_rts = itemView.findViewById(R.id.card_view_delivery_returnr_list);
            checkBox = itemView.findViewById(R.id.cbRT);

            item_ordId_rts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view1) {
                    if(mListner!=null){
                        int position1 = getAdapterPosition();
                        if(position1!=RecyclerView.NO_POSITION){
                            mListner.onItemClick_view_details(view1, position1);
                        }
                    }
                }
            });

            item_ret_dispute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view2) {
                    if(mListner!=null){
                        int position2 = getAdapterPosition();
                        if(position2!=RecyclerView.NO_POSITION){
                            mListner.onItemClick_dispute_raise(view2, position2);
                        }
                    }
                }

            });

        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v  = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.my_delivery_return_to_supervisor,viewGroup,false);
        ViewHolder viewHolder = new ViewHolder(v,i);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        viewHolder.item_ordId_rts.setText(list.get(i).getOrderid());
        viewHolder.item_retreason_rts.setText(list.get(i).getRetReason());
        viewHolder.checkBox.setChecked(imageModelArrayList1.get(i).getSelected());
        viewHolder.checkBox.setTag(i);

        final boolean b = imageModelArrayList1.get(i).getSelected();
        if(b == false){
            viewHolder.checkBox.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
        if (b == true){
            viewHolder.checkBox.setButtonDrawable(R.drawable.ic_check_box_black_24dp);
        }
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
            List<DeliveryReturnToSuperVisorModel>filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0){
                filteredList.addAll(listFull);
            }else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for(DeliveryReturnToSuperVisorModel item: listFull){
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

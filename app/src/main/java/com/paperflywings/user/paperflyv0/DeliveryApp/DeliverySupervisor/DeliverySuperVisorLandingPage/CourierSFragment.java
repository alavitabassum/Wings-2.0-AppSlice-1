package com.paperflywings.user.paperflyv0.DeliveryApp.DeliverySupervisor.DeliverySuperVisorLandingPage;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.paperflywings.user.paperflyv0.DeliveryApp.Courier.CourierSend.CourierResenByScan.DeliveryReturnManagementCourierSend;
import com.paperflywings.user.paperflyv0.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class CourierSFragment extends Fragment {

    private CardView Courier_send,View_details;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //setHasOptionsMenu(true);
        ViewGroup viewGroup = (ViewGroup)inflater.inflate(R.layout.fragment_supervisor_courier,container,false);
        Courier_send = (CardView)viewGroup.findViewById(R.id.courier_send_id);
        View_details = (CardView)viewGroup.findViewById(R.id.view_details_id);

        /*Courier_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.ss,new DeliveryFragment()).commit();
                Intent intent = new Intent(getActivity().getApplicationContext(), DeliveryCourier.class);
                startActivity(intent);
            }
        });*/
        Courier_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getApplicationContext(), DeliveryReturnManagementCourierSend.class);
                startActivity(intent);
            }
        });


        View_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.ss,new DeliveryFragment()).commit();
               /* Intent intent = new Intent(getActivity().getApplicationContext(), DeliveryCourier.class);
                startActivity(intent);*/
            }
        });


        return viewGroup;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       // inflater.inflate(R.menu.menu_status, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       /* if (item.getItemId() == R.id.action_status) {
            Toast.makeText(getActivity(), "Clicked on " + item.getTitle(), Toast.LENGTH_SHORT)
                    .show();
        }*/
        return true;
    }

}

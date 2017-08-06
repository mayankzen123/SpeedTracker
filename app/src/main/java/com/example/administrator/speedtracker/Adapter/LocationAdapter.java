package com.example.administrator.speedtracker.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.example.administrator.speedtracker.Model.LocationModel;
import com.example.administrator.speedtracker.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 8/6/2017.
 */

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    ArrayList<LocationModel> locationModels;
    Context mContext;
    LayoutInflater layoutInflater;
    private int lastPosition = -1;

    public LocationAdapter(Context context, ArrayList<LocationModel> locationModels) {
        mContext = context;
        this.locationModels = locationModels;
        layoutInflater = LayoutInflater.from(context);
    }

    public void addItem(ArrayList<LocationModel> locationModels) {
        this.locationModels = locationModels;
        notifyDataSetChanged();
    }

    @Override
    public LocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_location_status, parent, false);
        LocationViewHolder activityViewHolder = new LocationViewHolder(view);
        return activityViewHolder;
    }

    @Override
    public void onBindViewHolder(LocationViewHolder holder, int position) {
        holder.textDate.setText(locationModels.get(position).getDate());
        holder.textLatitude.setText(locationModels.get(position).getLatitude());
        holder.textLongitude.setText(locationModels.get(position).getLongitude());
        holder.textCurrentDuration.setText(locationModels.get(position).getCurrentDuration());
        holder.textNextDuration.setText(locationModels.get(position).getNextDuration());
        setAnimation(holder.itemView, position);
    }

    @Override
    public void onViewDetachedFromWindow(LocationViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_right);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return locationModels.size();
    }


    public class LocationViewHolder extends RecyclerView.ViewHolder {

        TextView textDate, textLatitude, textLongitude, textCurrentDuration, textNextDuration;

        public LocationViewHolder(View itemView) {
            super(itemView);
            textDate = (TextView) itemView.findViewById(R.id.text_date);
            textLatitude = (TextView) itemView.findViewById(R.id.text_latitude);
            textLongitude = (TextView) itemView.findViewById(R.id.text_longitude);
            textCurrentDuration = (TextView) itemView.findViewById(R.id.text_current_duration);
            textNextDuration = (TextView) itemView.findViewById(R.id.text_next_duration);
        }
    }
}

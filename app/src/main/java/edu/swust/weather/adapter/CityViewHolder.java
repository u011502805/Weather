package edu.swust.weather.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import edu.swust.weather.R;

public class CityViewHolder extends RecyclerView.ViewHolder {
    @Bind(R.id.view_holder_city)
    public LinearLayout item;
    @Bind(R.id.tv_city)
    public TextView tvCity;
    @Bind(R.id.tv_remark)
    public TextView tvRemark;

    public CityViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}

package edu.swust.weather.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import edu.swust.weather.R;
import edu.swust.weather.utils.ACache;
import edu.swust.weather.utils.Extras;

public class ManageCityAdapter extends RecyclerView.Adapter<CityViewHolder> implements View.OnClickListener, View.OnLongClickListener {
    private List<String> mCityList;
    private OnItemClickListener mClickListener;
    private OnItemLongClickListener mLongClickListener;
    private ACache mACache;

    public ManageCityAdapter(List<String> cityList) {
        mCityList = cityList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mLongClickListener = listener;
    }

    @Override
    public CityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_city, parent, false);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        mACache = ACache.get(parent.getContext());
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CityViewHolder holder, int position) {
        holder.item.setTag(mCityList.get(position));
        holder.tvCity.setText(mCityList.get(position));
        String currentCity = mACache.getAsString(Extras.CITY);
        holder.tvRemark.setText(mCityList.get(position).equals(currentCity) ? "当前城市" : "");
    }

    @Override
    public int getItemCount() {
        return mCityList.size();
    }

    @Override
    public void onClick(View v) {
        mClickListener.onItemClick(v, v.getTag());
    }

    @Override
    public boolean onLongClick(View v) {
        mLongClickListener.onItemLongClick(v, v.getTag());
        return true;
    }
}

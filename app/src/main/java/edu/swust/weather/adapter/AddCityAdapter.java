package edu.swust.weather.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import edu.swust.weather.R;
import edu.swust.weather.model.CityListEntity;
import edu.swust.weather.utils.ACache;
import edu.swust.weather.utils.Extras;

public class AddCityAdapter extends RecyclerView.Adapter<CityViewHolder> implements View.OnClickListener {
    private List<CityListEntity.CityInfoEntity> mCityList = new ArrayList<>();
    private Type mType;
    private OnItemClickListener mListener;
    private List<String> mAddedCityList;

    public void setData(List<CityListEntity.CityInfoEntity> data, Type type) {
        mCityList = data;
        mType = type;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public CityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_city, parent, false);
        view.setOnClickListener(this);
        ACache cache = ACache.get(parent.getContext());
        mAddedCityList = (List<String>) cache.getAsObject(Extras.CITY_LIST);
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CityViewHolder holder, int position) {
        holder.item.setTag(mCityList.get(position));
        switch (mType) {
            case PROVINCE:
                holder.tvCity.setText(mCityList.get(position).province);
                break;
            case CITY:
                holder.tvCity.setText(mCityList.get(position).city);
                break;
            case AREA:
                holder.tvCity.setText(mCityList.get(position).area);
                holder.tvRemark.setText(mAddedCityList.contains(mCityList.get(position).area) ? "已添加" : "");
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mCityList.size();
    }

    @Override
    public void onClick(View v) {
        mListener.onItemClick(v, v.getTag());
    }

    public enum Type {
        PROVINCE,
        CITY,
        AREA
    }
}

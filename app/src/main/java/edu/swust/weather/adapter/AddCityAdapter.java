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

/**
 * 添加城市适配器（列表管理器）
 * RecyclerView是一个强大的滑动组件，可以实现瀑布流 http://blog.csdn.net/lmj623565791/article/details/45059587
 * 通过设置它提供的不同LayoutManager，ItemDecoration , ItemAnimator实现令人瞠目的效果
 * 同ListView需要一个adapter一样，RecyclerView也需要Adapter才能正常使用
 *  */
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

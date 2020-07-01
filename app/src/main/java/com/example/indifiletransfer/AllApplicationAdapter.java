package com.example.indifiletransfer;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.zip.Inflater;

public class AllApplicationAdapter extends RecyclerView.Adapter<AllApplicationAdapter.AllApplicationAdapterViewHolder> {

    List<ApplicationInfo> list;
    Context context;
    PackageManager packageManager;

    public AllApplicationAdapter(List<ApplicationInfo> list, Context context, PackageManager packageManager) {
        this.list = list;
        this.context = context;
        this.packageManager = packageManager;
    }

    @NonNull
    @Override
    public AllApplicationAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.my_view, parent, false);
        return new AllApplicationAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllApplicationAdapterViewHolder holder, int position) {
       ApplicationInfo applicationInfo = list.get(position);
       holder.imageView.setImageDrawable(getIcon(applicationInfo));
       holder.textView.setText(applicationInfo.packageName);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class AllApplicationAdapterViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView textView;
        public AllApplicationAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.textView);
        }
    }

    Drawable getIcon(ApplicationInfo info){
        return  packageManager.getApplicationIcon(info);
    }
}

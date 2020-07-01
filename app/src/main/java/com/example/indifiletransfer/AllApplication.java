package com.example.indifiletransfer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import java.util.List;

public class AllApplication extends AppCompatActivity {
    private static final String TAG = AllApplication.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_application);

        Environment.getExternalStorageDirectory();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        PackageManager packageManager = getApplicationContext().getPackageManager();
        List<ApplicationInfo> list = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        Log.d(TAG, "onCreate: " + new Gson().toJson(list.get(0)));

        AllApplicationAdapter allApplicationAdapter = new AllApplicationAdapter(list, this, packageManager);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(allApplicationAdapter);

    }
}
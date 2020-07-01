package com.example.indifiletransfer;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Button accept, reject, button, advertising, discovery, wifiStartDiscover;

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    private static final String[] REQUIRED_PERMISSIONS =
            new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

    IntentFilter intentFilter = new IntentFilter();

    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;

    private List<WifiP2pDevice> p2pDevices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        setOnClickListener();

        accept.setVisibility(View.INVISIBLE);
        reject.setVisibility(View.INVISIBLE);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.d(TAG, "onChannelDisconnected: ");
            }
        });

    }

    public void initViews() {
        button = findViewById(R.id.allApplication);
        advertising = findViewById(R.id.advertising);
        discovery = findViewById(R.id.discovery);
        accept = findViewById(R.id.accept);
        reject = findViewById(R.id.reject);
    }

    public void setOnClickListener() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AllApplication.class);
                startActivity(intent);
            }
        });

        advertising.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAdvertising();
            }
        });

        discovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDiscovery();
            }
        });

        wifiStartDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDiscoverPeers(wifiP2pManager, channel);
            }
        });
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    //activity.setIsWifiP2pEnabled(true);
                    Log.d(TAG, "onReceive: Please enable");
                } else {
                    Log.d(TAG, "onReceive: fails to enable");
                    // activity.setIsWifiP2pEnabled(false);
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

                // The peer list has changed! We should probably do something about
                // that.
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT < 23) {
                        ActivityCompat.requestPermissions(
                                (Activity)getApplicationContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_REQUIRED_PERMISSIONS);
                    } else {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_REQUIRED_PERMISSIONS);
                    }
                }else {
                    wifiP2pManager.requestPeers(channel, peerListListener);
                }

            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

                // Connection state changed! We should probably do something about
                // that.

            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
//                DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
//                        .findFragmentById(R.id.frag_list);
//                fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
//                        WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));

            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    void startDiscoverPeers(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT < 23) {
                ActivityCompat.requestPermissions(
                        this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_REQUIRED_PERMISSIONS);
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_REQUIRED_PERMISSIONS);
            }
        } else {
            wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "onSuccess: wifi discover peers success");
                }

                @Override
                public void onFailure(int reason) {

                }
            });
        }
    }

    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peers) {
            Log.d(TAG, "onPeersAvailable: called");
            p2pDevices.clear();
            p2pDevices.addAll(peers.getDeviceList());
            Log.d(TAG, "onPeersAvailable: " + new Gson().toJson(p2pDevices));
        }
    };

    public static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!hasPermissions(this, getRequiredPermissions())) {
            if (Build.VERSION.SDK_INT < 23) {
                ActivityCompat.requestPermissions(
                        this, getRequiredPermissions(), REQUEST_CODE_REQUIRED_PERMISSIONS);
            } else {
                requestPermissions(getRequiredPermissions(), REQUEST_CODE_REQUIRED_PERMISSIONS);
            }
        }
    }

    protected String[] getRequiredPermissions() {
        return REQUIRED_PERMISSIONS;
    }

    void startAdvertising() {
        AdvertisingOptions advertisingOptions = new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build();
        ApplicationInfo applicationInfo = getApplicationInfo();
        Nearby.getConnectionsClient(getApplicationContext())
                .startAdvertising("Device A", applicationInfo.packageName, connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: button start advertising");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(String endpointId, ConnectionInfo info) {

            accept.setVisibility(View.VISIBLE);
            reject.setVisibility(View.VISIBLE);

            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Nearby.getConnectionsClient(getApplicationContext())
                            .acceptConnection(endpointId, payloadCallback);
                }
            });

            reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Nearby.getConnectionsClient(getApplicationContext()).rejectConnection(endpointId);
                }
            });

        }

        @Override
        public void onConnectionResult(@NonNull String s, @NonNull ConnectionResolution connectionResolution) {
            switch (connectionResolution.getStatus().getStatusCode()) {
                case ConnectionsStatusCodes.STATUS_OK:
                    // We're connected! Can now start sending and receiving data.
                    Log.d(TAG, "onConnectionResult: success");
                    break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                    // The connection was rejected by one or both sides.
                    Log.d(TAG, "onConnectionResult: fail");
                    break;
                default:
                    // The connection was broken before it was accepted.
                    break;
            }
        }

        @Override
        public void onDisconnected(@NonNull String s) {

        }
    };

    void startDiscovery() {
        ApplicationInfo applicationInfo = getApplicationInfo();
        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build();
        Nearby.getConnectionsClient(getApplicationContext())
                .startDiscovery(applicationInfo.packageName, endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: we are discovering");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: we are not discovering..");
                    }
                });
    }


    EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(@NonNull String s, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
            Log.d(TAG, "onEndpointFound: " + s);
            Log.d(TAG, "onEndpointFound: " + discoveredEndpointInfo.getEndpointName());
            Log.d(TAG, "onEndpointFound: " + discoveredEndpointInfo.getServiceId());
            requestConnection(s);
        }

        @Override
        public void onEndpointLost(@NonNull String s) {
            Log.d(TAG, "onEndpointLost: A previously discovered endpoint has gone away." + s);
        }
    };

    void stopAdvertising() {
        Nearby.getConnectionsClient(getApplicationContext())
                .stopAdvertising();
    }

    void stopDiscovery() {
        Nearby.getConnectionsClient(getApplicationContext())
                .stopDiscovery();
    }

    void requestConnection(String endpoint) {
        Nearby.getConnectionsClient(getApplicationContext())
                .requestConnection("Device A", endpoint, connectionLifecycleCallback)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // We successfully requested a connection. Now both sides
                        // must accept before the connection is established.
                        Log.d(TAG, "onSuccess: We successfully requested a connection. Now both sides");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {

        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

        }
    };
}
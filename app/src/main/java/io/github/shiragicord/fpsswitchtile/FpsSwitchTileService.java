package io.github.shiragicord.fpsswitchtile;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import java.security.Permission;

import io.github.shiragicord.fpsswitchservice.IFpsSwitchSettingService;

public class FpsSwitchTileService extends TileService {
    private static final String TAG = FpsSwitchTileService.class.getSimpleName();
    private static final String SETTING_SECURE_USER_REFRESH_RATE = "user_refresh_rate";

    private IFpsSwitchSettingService mFpsSwitchSettingService;
    private final ServiceConnection fpsSwitchSettingServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mFpsSwitchSettingService = IFpsSwitchSettingService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mFpsSwitchSettingService = null;
        }
    };
    @Override
    public void onStartListening() {
        super.onStartListening();
        setTileStateRefreshRate(getQsTile());
        Log.d(TAG, "onStartListening 1");

        bindFpsSwitchSettingService();
    }

    private void bindFpsSwitchSettingService() {
        var intent = new Intent("io.github.shiragicord.fpsswitchservice.FpsSwitchSettingService");
        intent.setPackage("io.github.shiragicord.fpsswitchservice");
        var bool = bindService(intent, fpsSwitchSettingServiceConnection, Service.BIND_AUTO_CREATE);
        Log.d(TAG, "bindService " + String.valueOf(bool));
    }

    private void unbindFpsSwitchSettingService() {
        unbindService(fpsSwitchSettingServiceConnection);
    }

    @Override
    public void onStopListening() {
        super.onStopListening();

        unbindFpsSwitchSettingService();
    }

    @Override
    public void onClick() {
        super.onClick();
        setRefreshRate(getQsTile().getState() == Tile.STATE_INACTIVE);
        setTileStateRefreshRate(getQsTile());
    }

    private void setTileStateRefreshRate(Tile tile) {
        try {
            if (isRefreshRate120()) {
                tile.setState(Tile.STATE_ACTIVE);
                var icon = Icon.createWithResource(this, R.drawable.ic_120);
                tile.setIcon(icon);
            } else {
                tile.setState(Tile.STATE_INACTIVE);
                var icon = Icon.createWithResource(this, R.drawable.ic_60);
                tile.setIcon(icon);
            }
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Cannot find refreshRate setting.");
            tile.setState(Tile.STATE_UNAVAILABLE);
        }

        tile.updateTile();
    }

    private boolean isRefreshRate120() throws Settings.SettingNotFoundException {
        var refreshRate = Settings.Secure.getInt(getContentResolver(), SETTING_SECURE_USER_REFRESH_RATE);
        return refreshRate == 120;
    }

    private void setRefreshRate(boolean is120) {
        try {
            if (mFpsSwitchSettingService != null) {
                mFpsSwitchSettingService.setRefreshRate(is120);
            } else {
                Log.e(TAG, "mFpsSwitchSettingService is null");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException", e);
        }
    }
}
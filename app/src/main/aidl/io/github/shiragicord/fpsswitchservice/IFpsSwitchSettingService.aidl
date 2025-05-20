// IFpsSwitchSettingService.aidl
package io.github.shiragicord.fpsswitchservice;

interface IFpsSwitchSettingService {
    boolean isRefreshRate120();
    void setRefreshRate(boolean is120);
}
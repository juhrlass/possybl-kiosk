package de.ecube.kioskweb;

import de.ecube.kioskweb.service.AdbService;
import de.ecube.kioskweb.service.DeviceStateService;
import de.ecube.kioskweb.service.DownloadService;
import de.ecube.kioskweb.service.DpmService;
import de.ecube.kioskweb.service.GestureService;
import de.ecube.kioskweb.service.InstallApkService;
import de.ecube.kioskweb.service.ShellCommandService;
import de.ecube.kioskweb.service.UniquePseudoIDService;
import de.ecube.kioskweb.service.WifiService;

public class ServiceLocator {

    private static ServiceLocator instance = null;

    private final WifiService wifiService = new WifiService();
    private final ShellCommandService shellCommandService = new ShellCommandService();
    private final DpmService dpmService = new DpmService();
    private final DeviceStateService deviceStateService = new DeviceStateService();
    private final UniquePseudoIDService uniquePseudoIDService = new UniquePseudoIDService();
    private final InstallApkService installApkService = new InstallApkService();
    private final DownloadService downloadService = new DownloadService();
    private final GestureService gestureService = new GestureService();
    private final AdbService adbService = new AdbService();

    private ServiceLocator() {
    }

    public static ServiceLocator getInstance() {
        if (instance == null) {
            synchronized (ServiceLocator.class) {
                instance = new ServiceLocator();
            }
        }
        return instance;
    }

    public WifiService getWifiService() {
        return wifiService;
    }

    public DpmService getDpmService() {
        return dpmService;
    }

    public DeviceStateService getDeviceStateService() {
        return deviceStateService;
    }

    public UniquePseudoIDService getUniquePseudoIDService() {
        return uniquePseudoIDService;
    }

    public InstallApkService getInstallApkService() {
        return installApkService;
    }

    public DownloadService getDownloadService() {
        return downloadService;
    }

    public GestureService getGestureService() {
        return gestureService;
    }

    public ShellCommandService getShellCommandService() {
        return shellCommandService;
    }

    public AdbService getAdbService() {
        return adbService;
    }
}


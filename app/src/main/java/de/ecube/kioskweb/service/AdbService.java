package de.ecube.kioskweb.service;

import java.io.IOException;

import de.ecube.kioskweb.ServiceLocator;

public final class AdbService {

    public void setAdbTCPService(boolean checked) throws IOException, InterruptedException {
        if (checked) {
            ServiceLocator.getInstance().getShellCommandService().executeShellCommand("setprop service.adb.tcp.port 5555");
        } else {
            ServiceLocator.getInstance().getShellCommandService().executeShellCommand("setprop service.adb.tcp.port -1");
        }
        ServiceLocator.getInstance().getShellCommandService().executeShellCommand("setprop ctl.restart adbd");
    }

    public String getAdbPortSetting() throws IOException, InterruptedException {
        return ServiceLocator.getInstance().getShellCommandService().executeShellCommand("getprop service.adb.tcp.port").replace("n", "");
    }
}

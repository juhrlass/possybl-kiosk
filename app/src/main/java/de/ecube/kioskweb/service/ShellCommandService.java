package de.ecube.kioskweb.service;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public final class ShellCommandService {

    String TAG = "ShellCommandService";

    public String executeShellCommand(final String command) throws InterruptedException, IOException {
        final StringBuilder output = new StringBuilder();
        final Process process = Runtime.getRuntime().exec(command.split(" "));
        process.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line = "";
        while ((line = reader.readLine()) != null) {
            output.append(line);
            output.append("n");
        }
        final String result = output.toString();
        Log.d(TAG, "Result of command " + command + ": " + result);
        return result;
    }


}

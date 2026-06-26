package com.ayush.ems.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class VPNChecker {

    public static boolean vpnchecker() {
        try {
            // Command to run (e.g., "netsh interface show interface")
            String command = "netsh interface show interface";

            // Create ProcessBuilder to execute the command
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            builder.redirectErrorStream(true); // Redirect error stream to input stream
            
            // Start the process
            Process process = builder.start();

            // Read the output of the process
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Output each line of the command output
                    System.out.println(line);
                    if(line.contains("CatoNetworks") && line.contains("Connected"))
                    {
                    	System.out.println("VPN is connected");
                    	return true;
                    }
//                    if(line.contains("CatoNetworks") && line.contains("Disconnected"))
//                    {
//                    	System.out.println("VPN is not connected");
//                    	return false;
//                    }
//                    else
//                    {
//                    	System.out.println("VPN is not connected");
//                    }
                }
            }

            // Wait for the process to finish
            int exitCode = process.waitFor();
            System.out.println("Process exited with code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
		return false;
    }
}

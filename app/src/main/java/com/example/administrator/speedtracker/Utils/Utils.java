package com.example.administrator.speedtracker.Utils;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Administrator on 8/4/2017.
 */

public class Utils {
    static String folderName = "SpeedTracker";
    static String fileName = "SpeedTracker.txt";
    static File speedUpdates;
    static FileWriter fileWriter;
    static BufferedWriter bufferedWriter = null;

    public static void writeToFile(String details) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), folderName);
            if (!root.exists()) {
                root.mkdirs();
            }
            speedUpdates = new File(root.getAbsolutePath(), fileName);
            fileWriter = new FileWriter(speedUpdates, true);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(details + "\n");
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package org.hear2read.telugu;

/**
 * Created by saikrishnalticmu on 7/6/17.
 */

import android.content.Context;
import android.content.res.AssetManager;
import android.view.View;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Utility {

    public static boolean pathExists(String pathname){
        File tempFile = new File(pathname);
        if ((!tempFile.exists()) ){
            return false;
        }
        return true;
    }

    public static  ArrayList<String>readLines(String filename) throws IOException {
        ArrayList<String> strLines = new ArrayList<String>();
        FileInputStream fstream = new FileInputStream(filename);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in),1024);
        String strLine;
        while ((strLine = br.readLine()) != null)   {
            strLines.add(strLine);
        }
        in.close();
        return strLines;
    }

    public static AssetManager getMyAssets(Context context)
    {
        return context.getResources().getAssets();
    }

    public static AssetManager getMyAssets(View view)
    {
        return view.getContext().getResources().getAssets();
    }

}



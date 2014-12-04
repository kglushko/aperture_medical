package com.westhealth.aperture.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by Kirill on 9/30/2014.
 */
public class FileIOTasks  {

    public void writeData ( String data, String filename, Context ctx, boolean append ) {
        try {
            Long milli = System.currentTimeMillis();
            if(append){
                filename = filename + "." + milli.toString();
            }
            FileOutputStream fOut = ctx.openFileOutput ( filename, ctx.MODE_PRIVATE ) ;
            OutputStreamWriter osw = new OutputStreamWriter ( fOut ) ;
            osw.write ( data ) ;
            osw.flush ( ) ;
            osw.close ( ) ;
        } catch ( Exception e ) {
            e.printStackTrace ( ) ;
        }
    }

    public String readSavedData ( String filename, Context ctx ) {
        StringBuffer datax = new StringBuffer("");
        try {
            FileInputStream fIn = ctx.openFileInput ( filename ) ;
            InputStreamReader isr = new InputStreamReader ( fIn ) ;
            BufferedReader buffreader = new BufferedReader ( isr ) ;

            String readString = buffreader.readLine ( ) ;
            while ( readString != null ) {
                datax.append(readString);
                readString = buffreader.readLine ( ) ;
            }

            isr.close ( ) ;
        } catch ( IOException ioe ) {
            ioe.printStackTrace ( ) ;
        }
        return datax.toString() ;
    }
}

package com.westhealth.aperture.nfcutils;

import android.content.Context;
import android.widget.Toast;

import com.westhealth.aperture.sync.OverviewActivity;
import com.westhealth.aperture.util.FileIOTasks;

import java.util.ArrayList;

/**
 * Created by Kirill on 11/10/2014.
 */
public class NfcDataParseTask {

    private ArrayList<Character> heartrate;
    private ArrayList<Character> transit;
    private ArrayList<Character> hydration;
    private ArrayList<Character> temperature;
    private ArrayList<Character> timestamp;
    private ArrayList<Character> daystamp;
    private ArrayList<Character> battery;

    private ArrayList<Double> parsedTimeStamp;

    private String[] fileList;
    private ArrayList<String> filesToDelete;
    private Context context;

    public NfcDataParseTask(Context c, String[] files) {
        context = c;
        fileList = files;
        parseFiles();
        parsedTimeStamp = parseTimestamp();
    }

    /* Getters */
    public ArrayList<Double> getParsedTimeStamp() {
        return parsedTimeStamp;
    }

    public Double parseCurrentHeartRate() {
        Double[] data = merge(heartrate, 4);

        for(int i = 1; i < data.length; i++) {
            if(data[i] <= 200 || data[i] >= 1200 ) {
                data[i] = data[i - 1];
            }
        }

        return 60/((data[data.length - 1]) / 1000);

    }

    public Double parseCurrentTransit() {
        Double[] data = merge(transit, 4);

        for(int i = 1; i < data.length; i++) {
            if(data[i] <= 50 || data[i] >= 200 ) {
                data[i] = data[i - 1];
            }
        }

        return data[data.length - 1];
    }

    public Double parseCurrentHydration() {
        Double[] data = merge(hydration, 4);

        /* Remember to add formulas */

        return data[data.length - 1];
    }

    public Double parseCurrentTemperature() {
        Double[] data = merge(temperature, 4);

/*        for(int i = 1; i < data.length; i++) {
            if(data[i] <= 50 || data[i] >= 200 ) {
                data[i] = data[i - 1];
            }
        }*/

        return (0.0505 * data[data.length - 1]) + 64.7;
    }

    public ArrayList<Double> parseHeartRate() {
        ArrayList<Double> parsedData = new ArrayList<Double>();

        Double[] data = merge(heartrate, 4);

        for(int i = 1; i < data.length; i++) {
            if(data[i] <= 200 || data[i] >= 1200 ) {
                data[i] = data[i - 1];
            }
        }

        for(Double d : data){
            parsedData.add(60/(d/1000));
        }

        return parsedData;
    }

    public ArrayList<Double> parseTransit() {
        ArrayList<Double> parsedData = new ArrayList<Double>();

        Double[] data = merge(transit, 4);

        for(int i = 1; i < data.length; i++) {
            if(data[i] <= 50 || data[i] >= 200 ) {
                data[i] = data[i - 1];
            }
        }

        for(Double d : data){
            parsedData.add(d);
        }

        return parsedData;
    }

    public ArrayList<Double> parseHydration() {
        ArrayList<Double> parsedData = new ArrayList<Double>();

        Double[] data = merge(hydration, 4);

        for(Double d : data){
            parsedData.add((d - 416) * (31/3));
        }

        return parsedData;
    }

    public ArrayList<Double> parseTemperature() {
        ArrayList<Double> parsedData = new ArrayList<Double>();

        Double[] data = merge(temperature, 4);

/*        for(int i = 1; i < data.length; i++) {
            if(data[i] <= 200 || data[i] >= 1200 ) {
                data[i] = data[i - 1];
            }
        }*/


        for(Double d : data){
            parsedData.add((0.0505 * d) + 64.7);
        }

        return parsedData;
    }

    private ArrayList<Double> parseTimestamp() {
        ArrayList<Double> parsedData = new ArrayList<Double>();

        long startOfRealtime = Long.parseLong(fileList[0].substring(fileList[0].length() - 13, fileList[0].length()));

        Double[] timedata = merge(timestamp, 2);
        Double[] daydata = merge(daystamp, 2);

        ArrayList<Double> hold = new ArrayList<Double>();
        ArrayList<Integer> dups = new ArrayList<Integer>();

        int mod = 0;

        for(int i = 0; i < daydata.length; i++) {
            if(i % 92 == 0 && i > 0) {
                mod++;
            }
            hold.add(timedata[i] + (mod * 92));
        }

        for(int j = 0; j < hold.size(); j++) {
            for(int k = 0; k < hold.size(); k++) {
                if (hold.get(j).equals(hold.get(k)) && (j != k)) {
                    dups.add(k);
                }
            }
        }

        for(int r = dups.size() - 1; r >= 0; r--) {
            heartrate.remove(dups.get(r));
            temperature.remove(dups.get(r));
            hydration.remove(dups.get(r));
            transit.remove(dups.get(r));
            battery.remove(dups.get(r));
            hold.remove(dups.get(r));
        }

        for(Double d : hold) {
            parsedData.add((d * 900000) + startOfRealtime);
        }

        return parsedData;
    }

    public ArrayList<Double> parseBattery() {
        ArrayList<Double> parsedData = new ArrayList<Double>();

        Double[] data = merge(battery, 2);

        for(Double d : data){
            parsedData.add(d);
        }

        return parsedData;
    }

    private void parseFiles() {

        FileIOTasks FIO = new FileIOTasks();

        filesToDelete = new ArrayList<String>();

        heartrate = new ArrayList<Character>();
        transit = new ArrayList<Character>();
        hydration = new ArrayList<Character>();
        temperature = new ArrayList<Character>();
        timestamp = new ArrayList<Character>();
        daystamp = new ArrayList<Character>();
        battery = new ArrayList<Character>();

        ArrayList<char[]> fileData = new ArrayList<char[]>();

        for (String s : fileList) {
            String raw = FIO.readSavedData(s, context).substring(1);
            if(raw.length() < 2210) {
                Toast.makeText(context, "Error: NFC Scan incomplete, please Re-Scan",
                        Toast.LENGTH_LONG).show();
                context.deleteFile(s);
                break;
            }
            String cln = "";
            for(int i = 0; i < 2322; i = i + 258)
                cln += (raw.substring(1 + i,257 + i));
            fileData.add(cln.substring(0,2207).toCharArray());
        }

        int matches = 0, i, j;

        for(i = 0; i < fileData.size() - 1; i++)
        {
            for(j = 0; j < fileData.get(i).length; j++) {
                if(fileData.get(i)[j] == fileData.get(i + 1)[j])
                {
                    matches++;
                }
            }
            if(matches == fileData.get(i).length) {
                fileData.remove(i);
                filesToDelete.add(fileList[i]);
            }
        }

        /*
        * Data alignment:
        *
        * BPM    | BPM   | TRANSIT TIME   | TRANSIT TIME
        * TEMP   | TEMP  | HYDRO 		   | HYDRO
        * TIME   | DAY   | BATT           | 0xFF
        *
        */

        int fileNum = 0;

        ArrayList<Character> daystamp_temp = new ArrayList<Character>();

        for(char[] c : fileData) {
            for(i = 0; i < c.length; i += 24) {
                heartrate.add(c[i + 0]);    // Byte 1
                heartrate.add(c[i + 1]);
                heartrate.add(c[i + 2]);    // Byte 2
                heartrate.add(c[i + 3]);

                transit.add(c[i + 4]);      // Byte 3
                transit.add(c[i + 5]);
                transit.add(c[i + 6]);      // Byte 4
                transit.add(c[i + 7]);

                hydration.add(c[i + 8]);  // Byte 5
                hydration.add(c[i + 9]);
                hydration.add(c[i + 10]); // Byte 6
                hydration.add(c[i + 11]);

                temperature.add(c[i + 12]);   // Byte 7
                temperature.add(c[i + 13]);
                temperature.add(c[i + 14]);   // Byte 8
                temperature.add(c[i + 15]);

                timestamp.add(c[i + 16]);   //  Byte 9
                timestamp.add(c[i + 17]);

                daystamp.add(c[i + 18]);    // Byte 10
                daystamp.add(c[i + 19]);

                battery.add(c[i + 20]);     // Byte 11
                battery.add(c[i + 21]);

                /* Byte 12 is currently free */
            }
        }
    }

    private Double[] merge(ArrayList<Character> data, int len) {

        StringBuilder sb = new StringBuilder();
        Double[] du = new Double[data.size() / len];

        if(len == 4) {
            for (int j = 0; j < data.size(); j += 4) {
                sb.append(data.get(j + 0));
                sb.append(data.get(j + 1));
                sb.append(data.get(j + 2));
                sb.append(data.get(j + 3));
                du[(j/4)] = (double)Integer.parseInt(sb.toString(),16);
                sb = new StringBuilder();
            }
        }
        else if(len == 2) {
            for (int j = 0; j < data.size(); j += 2) {
                sb.append(data.get(j + 0));
                sb.append(data.get(j + 1));
                du[(j/2)] = (double)Integer.parseInt(sb.toString(),16);
                sb = new StringBuilder();
            }
        }
        else {
            return null;
        }
        return du;
    }

    public long getMaxDate() {
        return parsedTimeStamp.get(parsedTimeStamp.size() - 1).longValue();
    }

    public long getMinDate() {
        return Long.parseLong(fileList[0].substring(fileList[0].length() - 13, fileList[0].length()));
    }
}

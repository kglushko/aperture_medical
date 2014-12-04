package com.westhealth.aperture.nfcutils;

import android.content.Context;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

import com.westhealth.aperture.util.FileIOTasks;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by Kirill on 9/30/2014.
 */
public class NFCReaderTask {

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private String NdefReadText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

        byte[] payload = record.getPayload();

        // Get the Text Encoding
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

        // Get the Language Code
        int languageCodeLength = payload[0] & 0063;

        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
        // e.g. "en"

        // Get the Text
        return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
    }

    private String NfcVReadText(ArrayList<byte[]> message) {
        StringBuilder nibbles = new StringBuilder();
        for (byte[] words : message) {
                nibbles.append(bytesToHex(words));
        }
        return nibbles.toString();
    }


    public int saveNdefData(NdefMessage msgs[], Context context, boolean append) {
        FileIOTasks FIO = new FileIOTasks();
        try {
            for (NdefMessage m : msgs) {
                NdefRecord[] rec = m.getRecords();
                for (NdefRecord r : rec) {
                    String data = NdefReadText(r);
                    FIO.writeData(data, "ndef_data.t", context, append);
                }
            }
        } catch (Exception exp) {
            return 0;
        }
        return 1;
    }

    public int saveNfcVData(ArrayList<byte[]> nfcVdata, Context context, boolean append) {
        FileIOTasks FIO = new FileIOTasks();
        try {
            String data = NfcVReadText(nfcVdata);
            FIO.writeData(data, "nfcv_data.t", context, append);
        } catch (Exception exp) {
            return 0;
        }
        return 1;
    }

}

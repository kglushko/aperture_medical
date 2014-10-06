package com.westhealth.aperture.nfc;

import android.content.Context;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

import java.io.UnsupportedEncodingException;

/**
 * Created by Kirill on 9/30/2014.
 */
public class NdefReaderTask {

    public String readText(NdefRecord record) throws UnsupportedEncodingException {
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

    public int saveData (NdefMessage msgs[], Context context, boolean append) {
        FileIOTasks FIO = new FileIOTasks();
        try {
            for (NdefMessage m : msgs) {
                NdefRecord[] rec = m.getRecords();
                for (NdefRecord r : rec) {
                        String data = readText(r);
                        FIO.writeData(data, "data.t", context, append);
                }
            }
        } catch (Exception exp) {
            return 0;
        }
        return 1;
    }
}

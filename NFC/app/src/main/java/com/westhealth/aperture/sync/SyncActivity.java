package com.westhealth.aperture.sync;

import com.westhealth.aperture.STMlibs.DataDevice;
import com.westhealth.aperture.STMlibs.Helper;
import com.westhealth.aperture.nfcutils.NFCReaderTask;
import com.westhealth.aperture.util.SystemUiHider;
import com.westhealth.aperture.STMlibs.NFCCommand;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;

import java.util.ArrayList;
import android.view.View;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.util.Timer;
import java.util.TimerTask;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class SyncActivity extends Activity {

    /**
     * A handle to NFC data
     */
    private NfcAdapter mNfcAdapter;

    NFCReaderTask mNRT = new NFCReaderTask();

    /**
     * Main text in center window
     */
    private TextSwitcher mTextView;

    /**
     * Main button on home screen
     */
    private Button mMainButton;

    private ArrayList<byte[]> nfcV_buffer;

    private Timer mTimer;

    private String[] waitText = {"READY\nTO\nSYNC",
                                 "PLEASE\nHOLD PHONE\nTO DEVICE",
                                 "NFC\nIS\nDISABLED"};

    boolean isOne = false;

    boolean isNFCon = false;

    private View decorView;

    private DataDevice ma = (DataDevice)getApplication();

    private byte[] SystemInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            OverviewActivity.overview.finish();
            mNfcAdapter.disableReaderMode(this);
        }
        catch (Exception e) {
            Log.d("AM_SYNC","Attempted to close non-existent activity:: " + e.getMessage());
        }

        decorView = getWindow().getDecorView();

        setContentView(R.layout.activity_sync);

        mMainButton = (Button) findViewById(R.id.main_button);

        // Initialize the textSwitcher
        initTextSwitch();

        flipText();

        // Check and Bind NFC radio
        initNFC();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        // Check and Bind NFC radio
        setupForegroundDispatch(this, mNfcAdapter);
        initNFC();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    /**
     * Initializes the TextSwitcher and Animations
     */
    private void initTextSwitch() {
        mTextView = (TextSwitcher) findViewById(R.id.fullscreen_content);

        mTextView.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                TextView myText = new TextView(SyncActivity.this);
                myText.setHeight(4000);
                myText.setWidth(2000);
                myText.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
                myText.setTextSize(50);
                myText.setTextColor(Color.argb(255, 0, 78, 88));
                return myText;
            }
        });

        // Declare the in and out animations and initialize them
        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);

        // set the animation type of textSwitcher
        mTextView.setInAnimation(in);
        mTextView.setOutAnimation(out);
    }

    /**
     * For that awesome dynamic look
     */
    private void flipText() {
        mTimer = new Timer();
        mNfcAdapter=NfcAdapter.getDefaultAdapter(this);

        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextView.setText((isNFCon ? (isOne ? waitText[1] : waitText[0]) : waitText[2]));
                        isOne = !isOne;
                    }
                });
            }
        },0, 3000);
    }

    /**
     * This checks if NFC is enabled and binds the App to the radio
     */
    private void initNFC() {
        mNfcAdapter=NfcAdapter.getDefaultAdapter(this);
        if(mNfcAdapter==null) {
            isNFCon = false;
            // Stop here, we definitely need NFC
            mTextView.setText("NFC IS NOT SUPPORTED BY YOUR DEVICE");
            mMainButton.setText("QUIT");
            mMainButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    finish();
                }
            });
        } else {
/*            if (!mNfcAdapter.isEnabled()) {
                isNFCon = false;
                // Tell the user we're in bad shape
                mMainButton.setVisibility(View.VISIBLE);
                mMainButton.setText("ENABLE NFC?");
                mMainButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                    }
                });
            } else {*/
                isNFCon = true;
                mMainButton.setText("CONTINUE W/O SYNC");
                mMainButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if(fileList().length == 0) {
                            Toast.makeText(getApplicationContext(),
                                    "Error: No historical records", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            startActivity(new Intent(SyncActivity.this, OverviewActivity.class));
                        }
                    }
                });
                discoverTag(getIntent());
            //}
        }
    }

    /**
     *  Actually handle tag discovery here
     */
    public void discoverTag(Intent intent) {
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {

            DataDevice dataDevice = (DataDevice)getApplication();
            Tag currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            SystemInfo = NFCCommand.SendGetSystemInfoCommandCustom(currentTag,dataDevice);

            DecodeGetSystemInfoResponse(SystemInfo);

            ma.setCurrentTag(currentTag);

            Log.d("Action", "ACTION_TAG_DISCOVERED");
            Log.d("Tag", currentTag.toString());

            nfcV_buffer = new ArrayList<byte[]>();

            /* Address space of out card, M24LR-16e {0x0000 - 0x01FF} */

            byte[][] addresses = new byte[][]{
                    {0x00,0x00},{0x00,0x20},{0x00,0x40},{0x00,0x60},
                    {0x00, (byte) 0x80},{0x00, (byte) 0xA0},
                    {0x00, (byte) 0xC0},{0x00, (byte) 0xE0},
                    {0x01,0x00}/*,{0x01,0x20},{0x01,0x40},{0x01,0x60},
                    {0x01, (byte) 0x80},{0x01, (byte) 0xA0},
                    {0x01, (byte) 0xC0},{0x01, (byte) 0xE0}*/
            };

            try {
                for(byte[] address : addresses) {
                    nfcV_buffer.add(NFCCommand.SendReadMultipleBlockCommand(currentTag, address,
                                                                            (byte) 0x1F, ma));
                }
                if(mNRT.saveNfcVData(nfcV_buffer, getApplicationContext(), true) == 1) {
                    Log.d("NFCv", "Data saved successfully");
                }
                else {
                    throw new Exception("FailedToSave_nfcVdata");
                }
            }
            catch (Exception e) {
                Log.e("NFCv", e.getMessage());
            }

            /* Logging, mostly for debug */

            String[] techList = currentTag.getTechList();
            String searchedTech = NfcV.class.getName();
            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    Log.d("Tech", tech);
                    break;
                }
            }
            startActivity(new Intent(this, OverviewActivity.class));
        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }

    /**
     *
     * @param activity	The {@link Activity} requesting foreground dispatch.
     * @param adapter	The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    /**
     *
     * @param adapter	The {@link NfcAdapter} used for the foreground dispatch
     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    public boolean DecodeGetSystemInfoResponse (byte[] GetSystemInfoResponse)
    {
        //if the tag has returned a good response
        if(GetSystemInfoResponse[0] == (byte) 0x00 && GetSystemInfoResponse.length >= 12)
        {
            ma = (DataDevice)getApplication();
            String uidToString = "";
            byte[] uid = new byte[8];
            // change uid format from byteArray to a String
            for (int i = 1; i <= 8; i++)
            {
                uid[i - 1] = GetSystemInfoResponse[10 - i];
                uidToString += Helper.ConvertHexByteToString(uid[i - 1]);
            }

            //***** TECHNO ******
            ma.setUid(uidToString);
            if(uid[0] == (byte) 0xE0)
                ma.setTechno("ISO 15693");
            else if (uid[0] == (byte) 0xD0)
                ma.setTechno("ISO 14443");
            else
                ma.setTechno("Unknown techno");

            //***** MANUFACTURER ****
            if(uid[1]== (byte) 0x02)
                ma.setManufacturer("STMicroelectronics");
            else if(uid[1]== (byte) 0x04)
                ma.setManufacturer("NXP");
            else if(uid[1]== (byte) 0x07)
                ma.setManufacturer("Texas Instrument");
            else
                ma.setManufacturer("Unknown manufacturer");

            //**** PRODUCT NAME *****
            if(uid[2] >= (byte) 0x04 && uid[2] <= (byte) 0x07)
            {
                ma.setProductName("LRI512");
                ma.setMultipleReadSupported(false);
                ma.setMemoryExceed2048bytesSize(false);
            }
            else if(uid[2] >= (byte) 0x14 && uid[2] <= (byte) 0x17)
            {
                ma.setProductName("LRI64");
                ma.setMultipleReadSupported(false);
                ma.setMemoryExceed2048bytesSize(false);
            }
            else if(uid[2] >= (byte) 0x20 && uid[2] <= (byte) 0x23)
            {
                ma.setProductName("LRI2K");
                ma.setMultipleReadSupported(true);
                ma.setMemoryExceed2048bytesSize(false);
            }
            else if(uid[2] >= (byte) 0x28 && uid[2] <= (byte) 0x2B)
            {
                ma.setProductName("LRIS2K");
                ma.setMultipleReadSupported(false);
                ma.setMemoryExceed2048bytesSize(false);
            }
            else if(uid[2] >= (byte) 0x2C && uid[2] <= (byte) 0x2F)
            {
                ma.setProductName("M24LR64");
                ma.setMultipleReadSupported(true);
                ma.setMemoryExceed2048bytesSize(true);
            }
            else if(uid[2] >= (byte) 0x40 && uid[2] <= (byte) 0x43)
            {
                ma.setProductName("LRI1K");
                ma.setMultipleReadSupported(true);
                ma.setMemoryExceed2048bytesSize(false);
            }
            else if(uid[2] >= (byte) 0x44 && uid[2] <= (byte) 0x47)
            {
                ma.setProductName("LRIS64K");
                ma.setMultipleReadSupported(true);
                ma.setMemoryExceed2048bytesSize(true);
            }
            else if(uid[2] >= (byte) 0x48 && uid[2] <= (byte) 0x4B)
            {
                ma.setProductName("M24LR01E");
                ma.setMultipleReadSupported(true);
                ma.setMemoryExceed2048bytesSize(false);
            }
            else if(uid[2] >= (byte) 0x4C && uid[2] <= (byte) 0x4F)
            {
                ma.setProductName("M24LR16E");
                ma.setMultipleReadSupported(true);
                ma.setMemoryExceed2048bytesSize(true);
                if(ma.isBasedOnTwoBytesAddress() == false)
                    return false;
            }
            else if(uid[2] >= (byte) 0x50 && uid[2] <= (byte) 0x53)
            {
                ma.setProductName("M24LR02E");
                ma.setMultipleReadSupported(true);
                ma.setMemoryExceed2048bytesSize(false);
            }
            else if(uid[2] >= (byte) 0x54 && uid[2] <= (byte) 0x57)
            {
                ma.setProductName("M24LR32E");
                ma.setMultipleReadSupported(true);
                ma.setMemoryExceed2048bytesSize(true);
                if(ma.isBasedOnTwoBytesAddress() == false)
                    return false;
            }
            else if(uid[2] >= (byte) 0x58 && uid[2] <= (byte) 0x5B)
            {
                ma.setProductName("M24LR04E");
                ma.setMultipleReadSupported(true);
                ma.setMemoryExceed2048bytesSize(true);
            }
            else if(uid[2] >= (byte) 0x5C && uid[2] <= (byte) 0x5F)
            {
                ma.setProductName("M24LR64E");
                ma.setMultipleReadSupported(true);
                ma.setMemoryExceed2048bytesSize(true);
                if(ma.isBasedOnTwoBytesAddress() == false)
                    return false;
            }
            else if(uid[2] >= (byte) 0x60 && uid[2] <= (byte) 0x63)
            {
                ma.setProductName("M24LR08E");
                ma.setMultipleReadSupported(true);
                ma.setMemoryExceed2048bytesSize(true);
            }
            else if(uid[2] >= (byte) 0x64 && uid[2] <= (byte) 0x67)
            {
                ma.setProductName("M24LR128E");
                ma.setMultipleReadSupported(true);
                ma.setMemoryExceed2048bytesSize(true);
                if(ma.isBasedOnTwoBytesAddress() == false)
                    return false;
            }
            else if(uid[2] >= (byte) 0x6C && uid[2] <= (byte) 0x6F)
            {
                ma.setProductName("M24LR256E");
                ma.setMultipleReadSupported(true);
                ma.setMemoryExceed2048bytesSize(true);
                if(ma.isBasedOnTwoBytesAddress() == false)
                    return false;
            }
            else if(uid[2] >= (byte) 0xF8 && uid[2] <= (byte) 0xFB)
            {
                ma.setProductName("detected product");
                ma.setBasedOnTwoBytesAddress(true);
                ma.setMultipleReadSupported(true);
                ma.setMemoryExceed2048bytesSize(true);
            }
            else
            {
                ma.setProductName("Unknown product");
                ma.setBasedOnTwoBytesAddress(false);
                ma.setMultipleReadSupported(false);
                ma.setMemoryExceed2048bytesSize(false);
            }

            //*** DSFID ***
            ma.setDsfid(Helper.ConvertHexByteToString(GetSystemInfoResponse[10]));

            //*** AFI ***
            ma.setAfi(Helper.ConvertHexByteToString(GetSystemInfoResponse[11]));

            //*** MEMORY SIZE ***
            if(ma.isBasedOnTwoBytesAddress())
            {
                String temp = new String();
                temp += Helper.ConvertHexByteToString(GetSystemInfoResponse[13]);
                temp += Helper.ConvertHexByteToString(GetSystemInfoResponse[12]);
                ma.setMemorySize(temp);
            }
            else
                ma.setMemorySize(Helper.ConvertHexByteToString(GetSystemInfoResponse[12]));

            //*** BLOCK SIZE ***
            if(ma.isBasedOnTwoBytesAddress())
                ma.setBlockSize(Helper.ConvertHexByteToString(GetSystemInfoResponse[14]));
            else
                ma.setBlockSize(Helper.ConvertHexByteToString(GetSystemInfoResponse[13]));

            //*** IC REFERENCE ***
            if(ma.isBasedOnTwoBytesAddress())
                ma.setIcReference(Helper.ConvertHexByteToString(GetSystemInfoResponse[15]));
            else
                ma.setIcReference(Helper.ConvertHexByteToString(GetSystemInfoResponse[14]));

            return true;
        }

        //if the tag has returned an error code
        else
            return false;
    }

}






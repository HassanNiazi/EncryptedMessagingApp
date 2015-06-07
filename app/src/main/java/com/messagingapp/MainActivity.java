package com.messagingapp;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    private static EditText numberField;
    private static EditText messageField;
    private static EditText secretKeyField;
    private static EditText lengthField;
    private static ArrayList<Message> list;
    private static MessageArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        numberField = (EditText) findViewById(R.id.numberField);
        messageField = (EditText) findViewById(R.id.messageField);
        secretKeyField = (EditText) findViewById(R.id.secretKeyField);
        lengthField = (EditText) findViewById(R.id.LengthField);
        final ListView listview = (ListView) findViewById(R.id.messageList);
        list = new ArrayList<>();
        adapter = new MessageArrayAdapter(this, list);
        listview.setAdapter(adapter);

        ReceivedSMS receivedSMS = new ReceivedSMS(this);
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(receivedSMS, filter);

        secretKeyField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                lengthField.setText(String.valueOf(secretKeyField.getText().toString().length()));

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void sendMessage(View view) {
        String number = numberField.getText().toString();
        String message = messageField.getText().toString();
        byte[] returnArray = new byte[0];
        try {
            String SecretKey = secretKeyField.getText().toString();
            Cipher c = Cipher.getInstance("AES");

            Key key = new SecretKeySpec(SecretKey.getBytes(), "AES");

            try {
                c.init(Cipher.ENCRYPT_MODE, key);
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }

            try {
                returnArray = c.doFinal(message.getBytes());
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

       // message = returnArray.toString();
        message = byte2hex(returnArray);
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(
                number, null, message, null, null);
        Toast.makeText(getBaseContext(), "Message Sent", Toast.LENGTH_SHORT).show();
        addToMessages("Me", message);
        messageField.setText("");
    }
    public static String byte2hex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            if (stmp.length() == 1)
                hs += ("0" + stmp);
            else
                hs += stmp;
        }
        return hs.toUpperCase();
    }
    public static byte[] hex2byte(byte[] b) {
        if ((b.length % 2) != 0)
            throw new IllegalArgumentException("hello");

        byte[] b2 = new byte[b.length / 2];

        for (int n = 0; n < b.length; n += 2) {
            String item = new String(b, n, 2);
            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        return b2;
    }

    public void addToMessages(String sender, String message) {
        byte[] returnArray = new byte[0];
        byte[] msg = hex2byte(message.getBytes());
        try {

            String SecretKey = secretKeyField.getText().toString();
            Cipher c = Cipher.getInstance("AES");

            Key key = new SecretKeySpec(SecretKey.getBytes(), "AES");

            try {
                c.init(Cipher.DECRYPT_MODE, key);
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }

            try {
                returnArray = c.doFinal(msg);
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        message = new String(returnArray);
        list.add(new Message(sender, message));
        adapter.notifyDataSetChanged();
    }

    public String getNumber() {
        return numberField.getText().toString();
    }

    public class MessageArrayAdapter extends TwoLineArrayAdapter<Message> {

        public MessageArrayAdapter(Context context, ArrayList<Message> Messages) {
            super(context, Messages);
        }

        @Override
        public String lineOneText(Message message) {
            return message.getMessage();
        }

        @Override
        public String lineTwoText(Message message) {
            return message.getSender();
        }
    }

    public class Message {
        private final String sender;
        private final String message;

        public Message(String sender, String message) {

            this.sender = sender;
            this.message = message;
        }

        public String getSender() {
            return sender;
        }

        public String getMessage() {
            return message;
        }
    }

    public abstract class TwoLineArrayAdapter<T> extends ArrayAdapter<T> {
        private int mListItemLayoutResId;

        public TwoLineArrayAdapter(Context context, ArrayList<T> ts) {
            this(context, android.R.layout.two_line_list_item, ts);
        }

        public TwoLineArrayAdapter(
                Context context,
                int listItemLayoutResourceId,
                ArrayList<T> ts) {
            super(context, listItemLayoutResourceId, ts);
            mListItemLayoutResId = listItemLayoutResourceId;
        }

        @Override
        public android.view.View getView(
                int position,
                View convertView,
                ViewGroup parent) {


            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View listItemView = convertView;
            if (null == convertView) {
                listItemView = inflater.inflate(
                        mListItemLayoutResId,
                        parent,
                        false);
            }

            TextView lineOneView = (TextView) listItemView.findViewById(
                    android.R.id.text1);
            TextView lineTwoView = (TextView) listItemView.findViewById(
                    android.R.id.text2);

            T t = getItem(position);
            lineOneView.setText(lineOneText(t));
            lineTwoView.setText(lineTwoText(t));

            lineOneView.setTextColor(Color.GREEN);
            lineTwoView.setTextColor(Color.GREEN);


            return listItemView;
        }

        public abstract String lineOneText(T t);

        public abstract String lineTwoText(T t);
    }
}

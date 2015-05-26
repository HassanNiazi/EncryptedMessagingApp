package com.messagingapp;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
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

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    private static EditText numberField;
    private static EditText messageField;
    private static ArrayList<Message> list;
    private static MessageArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        numberField = (EditText) findViewById(R.id.numberField);
        messageField = (EditText) findViewById(R.id.messageField);
        final ListView listview = (ListView) findViewById(R.id.messageList);
        list = new ArrayList<>();
        adapter = new MessageArrayAdapter(this, list);
        listview.setAdapter(adapter);

        ReceivedSMS receivedSMS = new ReceivedSMS(this);
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(receivedSMS, filter);
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
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(
                number, null, message, null, null);
        Toast.makeText(getBaseContext(), "Message Sent", Toast.LENGTH_SHORT).show();
        addToMessages("Me", message);
        messageField.setText("");
    }

    public void addToMessages(String sender, String message) {
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

            return listItemView;
        }

        public abstract String lineOneText(T t);

        public abstract String lineTwoText(T t);
    }
}

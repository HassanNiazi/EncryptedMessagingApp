package com.messagingapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;

public class ReceivedSMS extends BroadcastReceiver {

    private MainActivity mainActivity;

    public ReceivedSMS(MainActivity mainActivity){

        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        for (Object pdu : pdus) {

            SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
            String sender = sms.getOriginatingAddress();
            String message = sms.getMessageBody();

            if (PhoneNumberUtils.compare(sender, mainActivity.getNumber())) {
                mainActivity.addToMessages(sender, message);
            }
        }
    }
}


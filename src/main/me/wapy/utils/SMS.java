package me.wapy.utils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

/**
 * Created by Antonio Zaitoun on 01/07/2018.
 */
public final class SMS {

    public static final SMS shared = new SMS();

    private SMS(){
        String sid = Config.main.get("twilio_sid").getAsString();
        String token = Config.main.get("twilio_token").getAsString();
        Twilio.init(sid,token);
    }

    public void send(String sms,String phone) {
         Message.creator(new PhoneNumber(phone),
                new PhoneNumber(""),
                sms).createAsync();
    }
}

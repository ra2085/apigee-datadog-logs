package com.google.apigee;


import org.junit.Test;

import java.io.BufferedWriter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DatadogHandlerTests {

    @Test
    public void testOne(){

        DatadogHandler handler = new DatadogHandler();
        try{
            for(int i = 0; i < 10; i++) {
                Map.Entry<Long, BufferedWriter> socket = handler.socketPool.borrowObject();
                socket.getValue().write("57e873ac5bc61a25a9dad68fb942e2db hello from apigee 5!");
                socket.getValue().newLine();
                socket.getValue().flush();
                try{socket.getValue().close();} catch (Exception ex){}
            }
            TimeUnit.SECONDS.sleep(60);
            for(int i = 0; i < 10; i++) {
                Map.Entry<Long, BufferedWriter> socket = handler.socketPool.borrowObject();
                socket.getValue().write("57e873ac5bc61a25a9dad68fb942e2db hello from apigee 6!");
                socket.getValue().newLine();
                socket.getValue().flush();
                try{socket.getValue().close();} catch (Exception ex){}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

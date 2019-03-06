package com.google.apigee;

import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.MessageContext;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.Map;


public class DatadogHandler implements Execution {

    protected static ObjectPool<Map.Entry<Long, BufferedWriter>> socketPool;

    static {

        socketPool = new ObjectPool<Map.Entry<Long, BufferedWriter>>(20, 20, 500L) {
            @Override
            protected Map.Entry<Long, BufferedWriter> createObject() throws  Exception{
                Socket clientSocket = new Socket("intake.logs.datadoghq.com", 10514);
                BufferedWriter outToServer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                Long expiration = System.currentTimeMillis() + 18000;
                Map.Entry<Long, BufferedWriter> entry = new AbstractMap.SimpleEntry<Long, BufferedWriter>(expiration, outToServer);
                return entry;
            }

            @Override
            protected boolean validateObject(Map.Entry<Long, BufferedWriter> object) {
                if(System.currentTimeMillis() > object.getKey()){
                    return false;
                }
                return true;
            }
        };

    }

	public ExecutionResult execute(MessageContext messageContext, ExecutionContext executionContext) {

        try{
            Map.Entry<Long, BufferedWriter> socket = socketPool.borrowObject();
            socket.getValue().write("57e873ac5bc61a25a9dad68fb942e2db hello from apigee 2!");
            socket.getValue().newLine();
            socket.getValue().flush();
            try{socket.getValue().close();} catch (Exception ex){}
            return ExecutionResult.SUCCESS;
		} catch (Exception e) {
			messageContext.setVariable("exp", e.getMessage());
			return ExecutionResult.SUCCESS;
		}
	}
}
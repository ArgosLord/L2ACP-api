package com.elfocrash.l2acp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.stream.Collectors;

import com.elfocrash.l2acp.crypto.AesCrypto;
import com.elfocrash.l2acp.requests.L2ACPRequest;
import com.elfocrash.l2acp.tasks.DatamineTask;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.sf.l2j.commons.concurrent.ThreadPool;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.sf.l2j.gameserver.Shutdown;

public class L2ACPServer {
	
	final String apiKey = "elfocrash";
	public Shutdown serverShutdown = null;
	
	public static void main(String[] args) throws Exception
	{
		L2ACPServer.getInstance();		
	}
	
	public L2ACPServer()
	{
		HttpServer server;
		try {
			server = HttpServer.create(new InetSocketAddress(8000), 0);
	        server.createContext("/api", new RequestHandler());
	        server.setExecutor(null); // creates a default executor
	        server.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ThreadPool.scheduleAtFixedRate(new DatamineTask(), 120000, 600000);
	}
	
	class RequestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) {
        	try {
            String requestBody = read(t.getRequestBody());
            requestBody = AesCrypto.decryptRequest(requestBody);
            JsonElement jelement = new JsonParser().parse(requestBody);
            JsonObject  jobject = jelement.getAsJsonObject();
            	         
            String key = jobject.get("ApiKey").getAsString();
            if(key.equals(key))
            {
            	int requestId = Integer.parseInt(jobject.get("RequestId").getAsString());
            	L2ACPRequest request = L2ACPRequests.REQUESTS[requestId].newRequest(jobject);
            	
                Gson gson = new Gson();
                String jsonInString = gson.toJson(request.getResponse());
                String jsonResponse = jsonInString.toString();
                jsonResponse = AesCrypto.encryptRequest(jsonResponse);
                t.sendResponseHeaders(200, jsonResponse.length());
                OutputStream os = t.getResponseBody();
                os.write(jsonResponse.getBytes());
                os.close();
            }
            
        	} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
    
    public static String read(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }

    
	 
	public static final L2ACPServer getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final L2ACPServer _instance = new L2ACPServer();
	}

}

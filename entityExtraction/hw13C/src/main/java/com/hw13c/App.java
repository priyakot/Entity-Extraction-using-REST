package com.hw13c;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import mx.bigdata.jcalais.CalaisClient;
import mx.bigdata.jcalais.CalaisObject;
import mx.bigdata.jcalais.CalaisResponse;
import mx.bigdata.jcalais.rest.CalaisRestClient;


public class App 
{
 /**   public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        CalaisClient client = new CalaisRestClient("44vkyby2f4b6xcvphtum3xzc");
        try {
			CalaisResponse response = client.analyze("Prosecutors at the trial of former Liberian President Charles Taylor " 
			       + " hope the testimony of supermodel Naomi Campbell " 
			       + " will link Taylor to the trade in illegal conflict diamonds, "
			       + " which they say he used to fund a bloody civil war in Sierra Leone.");
			for (CalaisObject entity : response.getEntities()) {
			      System.out.println(entity.getField("_type") + ":" 
			                         + entity.getField("name"));
			    }
			for (CalaisObject topic : response.getTopics()) {
			      System.out.println(topic.getField("categoryName"));
			    }
			for (CalaisObject tags : response.getSocialTags()){
			      System.out.println(tags.getField("_typeGroup") + ":" 
			                         + tags.getField("name"));
			    }
			excutePost("http://www.bbc.com/news/health-30254697","");
        
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
    
    public static String excutePost(String targetURL, String urlParameters) {
        URL url;
        HttpURLConnection connection = null;  
        try {
          //Create connection
          url = new URL(targetURL);
           
          connection = (HttpURLConnection)url.openConnection();
          connection.addRequestProperty("User-Agent", 
      	        "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
          connection.setRequestMethod("POST");
          connection.setRequestProperty("Content-Type", 
               "application/x-www-form-urlencoded");

          connection.setRequestProperty("Content-Length", "" + 
                   Integer.toString(urlParameters.getBytes().length));
          connection.setRequestProperty("Content-Language", "en-US");  

          connection.setUseCaches (false);
          connection.setDoInput(true);
          connection.setDoOutput(true);

          //Send request
          DataOutputStream wr = new DataOutputStream (
                      connection.getOutputStream ());
        //  wr.writeBytes (urlParameters);
          wr.flush ();
          wr.close ();

          //Get Response    
          InputStream is = connection.getInputStream();
          BufferedReader rd = new BufferedReader(new InputStreamReader(is));
          String line;
          StringBuffer response = new StringBuffer(); 
          while((line = rd.readLine()) != null) {
            response.append(line);
            System.out.println("\n"+line);
            response.append('\r');
          }
          rd.close();
          return response.toString();

        } catch (Exception e) {

          e.printStackTrace();
          return null;

        } finally {

          if(connection != null) {
            connection.disconnect(); 
          }
        }
    } **/
}

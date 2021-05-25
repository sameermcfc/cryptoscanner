
import com.sun.tools.javah.Util;
import org.json.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class coinhuntscrapper {

    public static JSONArray parseData(URL url) throws IOException{
        //Check if connection is valid
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        int responseCode = conn.getResponseCode();
        if(responseCode != 200){
            throw new RuntimeException("HttpResponseCode: "+ responseCode);
        }
        else{
            // Scan data into JSONArray
            Scanner sc = new Scanner(url.openStream());
            String coin_details = sc.nextLine();
            JSONObject obj = new JSONObject(coin_details);
            JSONArray arr = obj.getJSONArray("res");
            sc.close();
            return arr;


        }
    }
    public static JSONArray orderByLaunch (JSONArray all_coins){
        List<JSONObject> jsonList = new ArrayList<JSONObject>();
        for (int i=0; i<all_coins.length(); i++){
            jsonList.add(all_coins.getJSONObject(i));
        }
        // Obtain the launchDate for each coin and create an array which orders by this value by soonest
        JSONArray sortedJSONArray = new JSONArray();
        Collections.sort(jsonList, (o1, o2) -> {
            String valA = new String();
            String valB = new String();
            try {
                valA = o1.getString("launchDate");
                valB = o2.getString("launchDate");
            } catch (JSONException e) {
                System.out.println("JSON error!");
            }
            return valA.compareTo(valB);
        });

        for (int i = 0; i < all_coins.length(); i++){
            sortedJSONArray.put(jsonList.get(i));
        }

        return sortedJSONArray;
    }
    public static void main(String[] args) throws IOException {
        JSONArray all_coins = parseData(new URL("https://api.cnhnt.cc/public/getAllCoinsApproved"));
        JSONArray sortedByLaunch = orderByLaunch(all_coins);
        for (int i=0; i<sortedByLaunch.length(); i++){
           long epoch = Long.parseLong(sortedByLaunch.getJSONObject(i).getString("launchDate"));
           long current = Instant.now().toEpochMilli();
           if(current <= epoch) {
               LocalDate ld = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).toLocalDate();
               System.out.println(ld + " " + sortedByLaunch.getJSONObject(i).getString("name") + " " + sortedByLaunch.getJSONObject(i).getLong("votesCount"));
           }
        }

        }
    }



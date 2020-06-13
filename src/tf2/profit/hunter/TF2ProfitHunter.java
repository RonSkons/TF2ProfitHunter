package tf2.profit.hunter;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.regex.*;
import java.util.Map.Entry;
import java.util.*;
import java.net.http.*;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.*;
import java.nio.charset.StandardCharsets;
import javax.json.*;

public class TF2ProfitHunter {
    String APIKey, stnItemCategory;
    Price budget, minProfit;
    
    public TF2ProfitHunter(String key, String cat, Price min, Price max){
        APIKey = key;
        stnItemCategory = "tf2-"+cat.toLowerCase();
        minProfit = min;
        budget = max;
    }
    public String search() {        
        String output;//This string will store the formatted results of the search
        HashMap<String, Price> potentials = new HashMap(); //This will store potentially profitable items - with a key/value of name/Price
        int currentPageNumber = 1;

        mainloop:
        while(true){//Keep looping until budget is exceeded.
            String urlString = "https://stntrading.eu/backend/itemOverviewAjax?page="+currentPageNumber+"&category="+stnItemCategory+"&sort=2"; //Grab item data from the STNTrading backend
            String contents = get(urlString);
            if(contents.length() <= 25){//If the length is 25 characters, the result is {"html":"","itemCount":0} which means that the page is empty.
                break;//In this case, the highest-value item in this category is worth less than our budget, and we sohuld stop searching.
            }
            
            Pattern namePattern = Pattern.compile("item-name&quot;&gt;(.*?)&lt;"); //Pull data from the HTML code using some regular expressions
            Pattern pricePattern = Pattern.compile("(?:Available|Starting) at (.*?)&lt;");
            Pattern stockPattern = Pattern.compile("item-stock (.*?)&quot;");
            
            Matcher names = namePattern.matcher(contents);
            Matcher prices = pricePattern.matcher(contents);
            Matcher stocks = stockPattern.matcher(contents);
            
            while(names.find()){//Loop through all matches
                stocks.find();
                prices.find();
                
                String stock = stocks.group(1);
                if(stock.equals("in-stock")){//If it's out of stock, ignore it
                    String name = names.group(1);
                    Price cost = new Price(prices.group(1));
                    if(cost.isGreaterThan(budget)){//If the cost of an item exceeds our maximum budget, stop searching!
                        break mainloop;
                    }else{
                        //Make sure that the item can be bought - not just an overpriced killstreak
                        potentials.put(name, cost);
                    }
                }
            }
            currentPageNumber++;//We're done with this page. Move on to the next one.
        }
        
        output = "Found "+potentials.size()+" potentially profitable items that cost less than "+budget;
        System.out.print("Found "+potentials.size()+" potentially profitable items that cost less than "+budget);
        //Now that we have a list of all of our potentially profitable items, cross-reference them with Backpack.tf to see which ones pay off.
        Set<Entry<String, Price>> itemSet = potentials.entrySet();//Convert our item HashMap into a set that we can iterate over
        for (Map.Entry<String, Price> item: itemSet) {//Iterate through the items, in no particular order (Potential TODO: Iterate by ascending price?)
            String name = item.getKey();
            Price price = item.getValue();
            
            Price profit = isProfitable(name, price);
            if(!profit.isZero() && !minProfit.isGreaterThan(profit) && isPurchaseable(name)){//If there is a profit, and it is more than minProfit, and the item can be purchased, display it
                output += "\n"+name+" | Profit: "+profit;
                System.out.print("\n"+name+" | Profit: "+profit);
            }
        }
        
        System.out.println("\nDONE!");//Degub code
        output += "\nDONE!";
        return output;
    }
    
    private String get(String uri){//Paraphrased from https://openjdk.java.net/groups/net/httpclient/recipes.html
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(uri))
          .build();
        HttpResponse<String> response;
        try {
            response = client.send(request, BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e.getCause());
        }
    }
    private Price isProfitable(String itemName, Price sellOrder){//Returns the amount of profit if the exchange is profitable, and a Price(0,0) object if not.
        String url = buildQuery(itemName);
        String data = get(url);//Contains a string of raw json
        
        JsonReader reader = Json.createReader(new StringReader(data));
        JsonArray buyOrders = reader.readObject().getJsonObject("buy").getJsonArray("listings");
        
        for (int i = 0; i < buyOrders.size(); i++){ //Iterate through all buy orders until a suitable one is found
            JsonObject order = buyOrders.getJsonObject(i);
            if(order.getJsonObject("item").getString("name").equals(itemName) && order.containsKey("automatic")){//If it is a direct name match and an automatic listing, continue
                JsonObject currencies = order.getJsonObject("currencies");//This JsonObject stores the amount of metal/keys that the buyer will pay
                
                //Parse "currencies" to a Price object
                Price buyOrder;
                if(currencies.containsKey("keys")){
                    if(currencies.containsKey("metal")){//Keys and metal
                        buyOrder = new Price(currencies.getInt("keys"), currencies.getJsonNumber("metal").doubleValue());
                    }else{//Keys, no metal
                        buyOrder = new Price(currencies.getInt("keys"), 0);
                    }
                }else{//Metal, no keys
                    buyOrder = new Price(0, currencies.getJsonNumber("metal").doubleValue());
                }
                
                if(buyOrder.isGreaterThan(sellOrder)){
                    //Success! Profit!
                    return buyOrder.getDifference(sellOrder);
                }else{
                    return new Price(0,0);//The best valid buy order is not profitable. Stop searching - it doesn't get better than this.
                }
            }
        }
        return new Price(0,0); //No valid buy orders found. Not profitable.
    }
    
    private String buildQuery(String itemName){//Take an item name and convert it to a BP.tf API call URL
        String url = "https://backpack.tf/api/classifieds/search/v1?key="+this.APIKey+"&offers=1&tradable=1&item="+urlEncode(itemName);//Base URL
        if(itemName.contains("Non-Craftable")){
            url += "&craftable=-1";
        }else{
            url += "&craftable=1";
        }
        
        if(itemName.contains("Australium")){
            url += "&australium=1";
        }else{
            url += "&australium=-1";
        }
        
        int quality = 6;//Unique quality. The default value.
        if(itemName.contains("Strange ") && !itemName.contains("Part:")){//If the item is Strange, NOT a Strange Part
            quality = 11;
        }else if(itemName.contains("Vintage ")){
            quality = 3;
        }else if(itemName.contains("Genuine ")){
            quality = 1;
        }
        url += "&quality="+quality;
        return url;
    }
    
    private String urlEncode(String str){
        try{
            String url = URLEncoder.encode(str, StandardCharsets.UTF_8.toString());
            return url;
        }catch(UnsupportedEncodingException e){
            throw new RuntimeException(e.getCause());
        }
    }
    
    private boolean isPurchaseable(String itemName){//Determines if the specified item can be purchased from STN - returns true if it can, false if it can't.
        String buyPage = get("https://stntrading.eu/item/tf2/"+urlEncode(itemName));
        return buyPage.matches("(?is).*"+stnItemCategory.substring(4, stnItemCategory.length()-1)+"s? in stock.*"); //If the page contains "[Category name](s) in stock" (as opposed to "Killstreak(s) in stock") the item can be purchased.
    }
}

package tf2.profit.hunter;


//This class allows the easy comparison of TF2 prices, which may have both an amount in keys and an amount in refined metal
public class Price {
    static double keyCost = 54.33;//This variable stores the amount of refined metal that it costs to purchase a key. The default value is a rough estimate, and should be set by the user.
    double refined;
    int keys;
    
    Price(int key, double ref){//price foo = new price(2, 12.66) will create a new price, worth 2 keys and 12.66 refined metal
        refined = ref;
        keys = key;
        toMixed();//Convert to mixed prices
    }
    
    Price(String rawPrice){//Parse numerical values from a price string in the format "x keys, n ref", "1 key, n ref", "x keys", or "n ref"
        if(rawPrice.endsWith("ref")){//Price includes refined metal
            if(rawPrice.contains("key")){//Keys and ref
                refined = Double.parseDouble(rawPrice.substring(rawPrice.indexOf(",")+2, rawPrice.indexOf("r"))); //Begin 2 characters after the comma folowing "key?s", and continue until "ref"
                keys = Integer.parseInt(rawPrice.substring(0, rawPrice.indexOf(" "))); //Begin at 0, and capture characters up to (and not including) the space after the key price
            }else{//Ref, but no keys
                keys = 0;
                refined = Double.parseDouble(rawPrice.substring(0, rawPrice.indexOf("r")));
            }
        }else{//No refined - all characters before " keys" must therefore be the price in keys
            refined = 0;
            keys = Integer.parseInt(rawPrice.substring(0, rawPrice.indexOf(" "))); //Begin at 0, and capture characters up to (and not including) the space after the key price
        }
        toMixed();//Convert to mixed prices
    }
    
    public boolean isGreaterThan(Price compare){//Returns true if this price is worth more than the provided price and false if it is worth less
        toRef();//Convert all Prices to refined metal for easy comparison
        compare.toRef();
        return refined > compare.refined; //More keys means a higher value. 
        //However, if both prices have the same key value, then it is up to the refined metal to determine the higher-valued price.
    }
    
    public boolean isZero(){//Returns true if the Price is worth 0 keys and 0 refined metal. Returns false otherwise.
        return refined == 0 && keys == 0;
    }
    public Price getDifference(Price compare){//Find the absolute difference between two prices (to two decimal places), and return it as a Price
        toRef();//Convert all Prices to refined metal for easy comparison
        compare.toRef();
        
        double difference = roundDouble(refined-compare.refined);
        Price output = new Price(0, difference);
        output.toMixed();//Convert difference back to keys and ref
        return output;
    }
    
    private void toRef(){//Used internally for price comparisons. Converts an amount of keys and refined to pure refined metal.
        refined += keyCost*keys;
        keys = 0;
    }
    
    private void toMixed(){//Used internally for price comparisons. Converts as much metal as possible to keys.
        if(refined >= keyCost){//There's no point converting ref into keys if there is not enough metal to purchase a key.
            keys += (int)Math.floor(refined/keyCost);//The maximum amount of purchaseable keys
            refined = roundDouble(refined%keyCost);
        }
    }
    
    private double roundDouble(double value){//Round value to 2 decimal places
        return Math.round(value*100.0)/100.0;
    }
    
    @Override
    public String toString(){
        toMixed();//Mixed prices are much neater to present
        String plural = (keys != 1) ? "keys":"key"; //We don't want "1 keys"!
        if(keys == 0){
            return refined+" refined"; //Return the String representation of the price, sans keys
        }else if(refined == 0){
            return keys+" "+plural; //Return the String representation of the price, sans refined
        }else{
            return keys+" "+plural+", "+refined+" refined"; //Return the String representation of the price, with both keys and refined
        }
    }
}

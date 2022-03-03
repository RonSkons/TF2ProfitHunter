<em>Not maintained, probably doesn't work at the moment.</em>

# TF2ProfitHunter
The lower end of the Team Fortress 2 trading economy is dependant on hordes of trading bots. However, prices list of these bots are not always in line with eachother. Thus, arbitrage is possible and profitable trading opportunities are consistently present.

This program scans a TF2 trading website, [STNTrading.eu](https://stntrading.eu/), and looks for items that can be purchased for a lower price than the buy orders on another trading website, [backpack.tf](https://backpack.tf/). Once the program finds a match, simply purchase the item for the first site, and sell it to a bot on the other.

Note: Uses [javax.json](https://docs.oracle.com/javaee/7/api/javax/json/package-summary.html) in order to parse responses from the BP.tf API. I've uploaded the JAR file of the library, but it must be manually added to the classpath in your preferred IDE in order to recompile the source code.
## Guide
This tool requires an up-to-date java installation. To open it, download and run TF2ProfitHunter.jar.
Fill in all the necessary fields:
- Under *Maximum Price*, enter the amount of currencies that you have at your disposal. The scraper will ignore items that are more expensive than your budget.
- Under *Minimum Profit*, enter the minimum required profit required for a trade to be returned. Trades with a profit lower than this will be ignored.
- Under *BP.tf API Key*, enter your [backpack.tf API key](https://backpack.tf/developer). Elevated access is required.
- Under *Key Price*, enter the current price with which you can buy a key. Generally, I found the best prices [here](https://backpack.tf/stats/Unique/Mann%20Co.%20Supply%20Crate%20Key/Tradable/Craftable).
Finally, hit *Begin Search*. The program will begin comparing prices across the two trading sites according to your criteria.
Currently, inputs are not well validated. Just make sure to only enter numbers where numbers whould be entered, and that your API key is correct.
After a few minutes (or more, depending on how high your budget is), the search should be complete. Any profitable trades will be displayed in the textarea at the bottom of the window.
Then, just buy from STN, sell to BP, and *voila*! You just made some easy profit!

*If you've enjoyed using this program and would like to support me, feel free to [send me some items](https://steamcommunity.com/tradeoffer/new/?partner=312046080&token=EBWiXMVe). Constructive criticism and suggestions are also always welcome.*

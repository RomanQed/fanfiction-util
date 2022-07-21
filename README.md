# fanfiction-util

A simple utility to help the translator with fanfiction.net

# How To
1) Open the required chapter on the website fanfiction.net
2) Save the page (ctrl+s or right-click->save as)
3) Open futil.jar
4) Click "parse" and select the file containing the downloaded web page
5) PROFIT

# How To Use Built-in Translator
## If you will use the built-in implementation
1) Go to https://rapidapi.com/microsoft-azure-org-microsoft-cognitive-services/api/microsoft-translator-text/
2) Subscribe to api
3) Get value of "X-RapidAPI-Key" header
4) Create config file config.json at the .jar file directory
5) Fill it this way

```json
{
    "translate-token": "Your value of X-RapidAPI-Key header"
}
```
## If you want to use your implementation
1) 

* refactor YoutubeVideo to support twitch vods: URL is "https://www.twitch.tv/videos/\<video id>" (video id = item_id in the json)
* investigate if AbstractVideoListener has any Youtube specific stuff, if so consider moving this to a youtube specific super class for the RSS an API listener
* implement twitch listener as a child class of AbstractVideoListener
* investigate if the processing process supports twitch
* twitch descriptions would be description_html in the json, convert this from html to markdown
* investigate need for a format for twitch descriptions

Requests:
get collection data from twitch, format and put in a file  
`curl -H 'Accept: application/vnd.twitchtv.v5+json' -H 'Client-ID: <Client-ID>' -X GET 'https://api.twitch.tv/kraken/collections/<Collection ID>/items' | sudo json_pp > /tmp/twitchCollection.json  && atom /tmp/twitchCollection.json`

Collection ID: WRX7ycPa5RSsgA  
[Client ID](https://dev.twitch.tv/dashboard)  
[API info](https://dev.twitch.tv/docs/v5/reference/collections#get-collection)  
oauth shouldn't be needed for this (woot woot \o/)
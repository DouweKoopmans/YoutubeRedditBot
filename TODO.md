* ~~refactor YoutubeVideo to support twitch vods: URL is "https://www.twitch.tv/videos/\<video id>" (video id = item_id in the json)~~
* ~~investigate if AbstractVideoListener has any Youtube specific stuff, if so consider moving this to a youtube specific super class for the RSS an API listener~~
* ~~implement twitch listener for collections as a child class of AbstractVideoListener~~
* implement twitch listener for all videos of a twtich channel as a child class of AbstractVideoListener
* add support for twitch in the config
    * ~~collections~~
    * all videos
* ~~investigate if the processing process supports twitch~~
    * add tests
* twitch descriptions would be description_html in the json, convert this from html to markdown -> testing
* investigate need for a format for twitch descriptions
* ~~check if the history manager is compatible with twitch~~
* replace Youtube (and possibly twitch) links found in the description which are present in our history -> testing

Requests:
get collection data from twitch, format and put in a file  
`curl -H 'Accept: application/vnd.twitchtv.v5+json' -H 'Client-ID: <Client-ID>' -X GET 'https://api.twitch.tv/kraken/collections/<Collection ID>/items'`

`curl -H 'Accept: application/vnd.twitchtv.v5+json' -H 'Client-ID: <Client-ID>' -X GET 'https://api.twitch.tv/kraken/channels/<channelId>/videos'`

Collection ID: WRX7ycPa5RSsgA  
N3 channelId = 29660771

[Client ID](https://dev.twitch.tv/dashboard)  
[Colletion API info](https://dev.twitch.tv/docs/v5/reference/collections#get-collection)  
[Video API info](https://dev.twitch.tv/docs/v5/reference/channels#get-channel-videos)

oauth shouldn't be needed for this (woot woot \o/)
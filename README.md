<a href='https://play.google.com/store/apps/details?id=com.dmitriymorozov.findfork&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png'/></a>

Android Level-up courses final work

Description of application:<br>
A map of restaurants around the location.

Technical requirements:
1. The user should be able to enter the address or zip code of their location.
2. The user should be able to automatically determine their position using GPS or any available means and use it as a base of the search.
3. The application should display restaurants around the specified location using markers: a) on the map, b) in list mode.
4. In the list mode, the following information should be displayed: the name of the restaurant and the distance to the specified address or user's location. Also, restaurants should be sorted in the list by distance to the address or userr's location.
5. Either in the map mode or in the list mode, an item click action should be followed by the screen (or dialog) displayed. In the dialog the restaurant name, contact phone number, address and price rating ($, $$, etc.) should be displayed.
6. In the window that opens, the user should be able to specify the restaurant's rating and enter his nickname. The evaluation of each next user wipes out the rating of the previous one.
7. When scrolling the list, the search area should expand. Also, the user should be able to edit the search area using zoomIn / zoomOut in map mode.
8. In the map mode, the restaurant with the highest rating should be marked with a different color marker. If there are several top-ranked restaurants, then they all should have such a marker.


The application must support caching and offline operations based on stored data.
The application must support the screen rotation.


Additional materials.

As a restaurant base, you can use the Foursquare API:<br>
https://developer.foursquare.com/docs/venues/search
https://developer.foursquare.com/overview/venues
https://developer.foursquare.com/docs/venues/categories

Also, the following links can be useful:<br>
http://stackoverflow.com/questions/9064385/ploting-foursquare-venues-on-google-map
http://stackoverflow.com/questions/3478840/google-maps-api-drop-a-new-pin

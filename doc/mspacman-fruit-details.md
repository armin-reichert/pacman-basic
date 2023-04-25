You're exactly correct. Each map has one entrance path and one exit path for each tunnel opening present. 
This means there are four entrance paths on maps 1, 2, and 4, but only two on map 3 (there are only 2 tunnel openings).

The exact fruit mechanics are as follows: After 64 dots are consumed, the game spawns the first fruit of the level. 
After 176 dots are consumed, the game attempts to spawn the second fruit of the level. 
If the first fruit is still present in the level when (or eaten very shortly before) the 176th dot is consumed, 
the second fruit will not spawn. Dying while a fruit is on screen causes it to immediately disappear and never return.
The type of fruit is determined by the level count - levels 1-7 will always have two cherries, two strawberries, etc. 
until two bananas on level 7. 
On level 8 and beyond, the fruit type is randomly selected using the weights in the following table:

Apple|Strawberry|Orange|Pretzel|Apple|Pear|Banana
-----|----------|------|-------|-----|----|------
5/32 |5/32      |5/32	 |5/32   |4/32 |4/32|4/32

(Regrettably, the RNG "function" in Ms. Pac-Man is a poor excuse for true randomness and there is no attempt made to balance fruit spawns. But that's not important.)

Fruits may spawn from any of the tunnels on the map. The tunnel they spawn in determines the path they will take 
to "the loop", which is the path that runs around the ghost house. They always arrive in such a way that they end up 
going clockwise around the ghost house, making one full lap before choosing their exit path. Finally, they exit the loop
 and follow the hardcoded path until they leave the map through the tunnels. The fruit may leave the loop at the same 
 exact tile it came in at, but usually the fruit will end up making more than one lap, as it has to keep going around 
 clockwise until it reaches its path. It is impossible for the fruit to make two full laps as it will always leave the 
 loop at the earliest possible moment for its path. Additionally, this system means that the amount of time the fruit 
 stays on the map is variable and depends on the length of the paths and what combination of paths is chosen.

As an example, here is a (poorly drawn) map of the first map's paths. I can't remember the paths for maps 2, 3, and 4, 
and I don't have the game on me to test at the moment, but if you pop open MAME and use save states you should be able 
to get the rest of the paths pretty quickly.

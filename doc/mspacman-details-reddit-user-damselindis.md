(Original URL: https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/)

Hey! Glad to hear you liked the info. I have been playing Ms. Pac for years and learned the ins and outs of the game mostly through testing and trial and error
but occasionally do dip into the assembly. 

I don't know if I would ever have figured it out alone but the Pac-Man dossier did most of the heavy lifting, all I had to figure out was what was different 
between the two games. Speaking of that, here is a comprehensive list of all differences between the two:

- Scatter/chase timings
- Ghost behavior during scatter mode
- Fruits
- Maps/Boards
- The killscreen

With the exception of these 5 bullets, everything else is exactly the same, unless I'm forgetting something. Let's dive into each in-depth.

### Scatter/chase timings (:heavy_check_mark:)
This is one of the most important changes Ms. Pac-Man brings. This is the scatter/chase timing table for Ms. Pac-Man:

Mode      | Levels 1-4 | Levels 5+
----      | ---------- | ---------
Scatter	  | 7	         | 5
Chase	    | 20         | 20
Scatter	  | 1/60	     | 1/60
Chase	    | 1037	     | 1037
Scatter	  | 1/60	     | 1/60
Chase	    | 1037	     | 1037
Scatter	  | 1/60	     | 1/60
Chase	    | indefinite| indefinite

Note: 1037 seconds is 17 minutes and 17 seconds.

While it may not seem much, this is gamechanging. Essentially, the scatter/chase cycle is eliminated entirely. The ghosts begin the game in scatter for 7/5 seconds and switch to chase for 20 seconds. Then, they enter scatter mode for just a single frame, making them turn around, and immediately switch back to chase for nearly 20 entire minutes. The table might as well look like this (excluding the single reversal at 27/25 seconds):

Mode      | Levels 1-4 | Levels 5+
----      | ---------- | ---------
Scatter	  | 7	         | 5
Chase	    | indefinite |indefinite

### Ghost behavior during scatter mode (:heavy_check_mark:)
During scatter mode, Blinky and Pinky only will move completely randomly. Inky and Sue/Clyde will still path towards their respective corners. Note that due to the scatter/chase changes this only matters during the first few seconds of a level.

### Fruits (:heavy_check_mark:)
I've already given my fruits rundown, but there is one thing I forgot to clarify. On level 8 and beyond, each fruit is randomly selected when it is spawned. So level 8 could give you a cherry and a banana, then level 9 could give you a pretzel and an orange, etc. The fruit indicator in the bottom right stays the same as it is in level 7 for the rest of the entire game.

### Maps/Boards (:heavy_check_mark:)
Ms. Pac, as you are likely already aware, comes with 4 sets of maps instead of Pac's 1. Map 1 appears for 2 levels, map 2 for 3, then map 3 and 4 alternate every 4 levels until the end of the game. Speaking of the end of the game:

### The killscreen (*Out of scope*)
When you reach level 128, the routine that adds the "slow ghost" bit to the tunnels detects this as maze -1, as the programmers used the wrong flag comparison. While not quite an integer overflow, the cause of the Pac-Man split screen, it has the same effect, and from then on every time a level is loaded the game begins writing this bit to memory addresses it was never intended to modify. From level 128-133, this has mostly harmless effects. When loading level 134 and onward, completely coincidentally, the program counter begins jumping to unpredictable locations and begins writing bits to entirely random memory addresses. On any level from 134-141, the game can flip the screen upside down, flip only the maze upside down, make the maze walls invisible, introduce some other graphical glitches, or worst of all, reset. Finally, loading level 142 causes so much damage to the program memory that the game unavoidably crashes.

That's it for all the changes. I tried your implementation and was blown away, this is one of the most accurate Pac recreations I have ever played. You have done a phenomenal job and your attention to detail is impeccable - I initially thought it was a MAME wrapper until a bit of gameplay. How did you even notice that the Ms. Pac marquee is bugged and doesn't show the lights going around the left edge?

That being said, I did notice some small issues and have a bit of feedback:

- First and foremost, Ms. Pac feels much too slow - I don't think her raw speed is too low, but it feels like she doesn't corner very well. In Pac and Ms. Pac, buffering a direction around a corner causes them to physically cut that corner, shaving a few pixels off their route. In the arcade version, on level 10, Ms. Pac is able to easily outpace Elroy 2 Blinky, helped by the especially twisty map 4. On your implementation, Blinky doesn't just keep up with Ms. Pac, he outruns her. If you can't out-run the ghosts, and you can't out-corner the ghosts, what options are there to win? I didn't play up to the "slow boards", but I'm not sure they would be possible. (:white_check_mark: Added some cornering speedup for Pac-Man)

- Collision detection seems wonky. I'm sure you are familiar with the 'passthrough glitch' mentioned in the Dossier, and while I understand it is a real mechanic in the game, it happens far too often in your implementation. I have almost never had a frightened ghost pass through me in the arcade game, but it happened to me twice in the same board in just half an hour of play. (:white_check_mark: Rearranged code but not sure if issue is completely fixed)

- When pellets get shorter, I think under 3 seconds? The ghosts flash for the entirety of the duration. Your implementation has no flashing at all, and the effect ends when the ghosts are still solid blue. (:heavy_check_mark: Fixed)

- On map 3, ghosts are never allowed to go down immediately after leaving the tunnels. They must go right or left depending on which tunnel exit they appear from. However, there is nothing preventing them from going down at those intersections if not leaving a tunnel. (*Not yet done*)

- This might have been intentional, but eating all ghosts on a map awards 24,000 points instead of 12,000. This means I ended level 1 with 26,600 points instead of the usual 14,600. (*I cannot see in the code how this may happen, must reproduce this somehow*)

I hope you continue to work on and find great success with your game. It looks and plays wonderful, and I love the 3D. Let me know if you need any more information or elaboration.

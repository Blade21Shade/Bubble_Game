# Bubble Game
A bubble matching game, like Candy Crush

As of writing this, the game isn't working as should, players clicking bubbles doesn't work properly, but the basics are in place.

Overall this game has been quite successful at achieving my end goal of making a bubble matching game.

To play: Click bubbles that are next to each other that when switched will cause 3 or more bubbles of the same color to be in a horizontal or vertical line.

## Takeaways
There were two big takeaways for this project: Separate code into smaller functions and test code before writing everything

### Separate Code into Smaller Functions
When I first made the game I wrote a large chunk of it as a few big functions. While this was convenience at first, it made troubleshooting difficult and the same code being used multiple times became a pain to change each one. This is something that had been hounded into us since we started learning Java, but I think I had just been too confident and since this project was less "hand holdy" then school projects I ended up regretting it. But I was able to get functionality split up as necessary, and it is nicely separated out now.

### Test Code Before Writing Everything
Similar to above, I started this project like a school project where most of it I could write out in one big chunk before testing and it would work. However, when stuff started going wrong not doing any testing during the process ended up a large problem. One problem in particular is that bubbles were falling sideways rather than down, and it took me a while to figure out my initial allocation and drawing of bubbles was coded sideways, a problem that could've been easily found out if I had just tested the code when I first wrote it. I was able to eventually fix almost everything (as of writing I still have an issue with manually clicking bubbles), but I have certainly learned that I need to test stuff as I write it, not just assume it is all going to work.

## How the Game Works
The main driver of the game is a variable called "moving"; when bubbles are moving, the player isn't allowed to click bubbles. As bubbles are matched they are marked for destruction, and a few functions work to destroy and move current bubbles while also creating new bubbles. Once everything is done moving the program "unlocks", allowing the user to interact once more. They can click bubbles to try and match, and if the move would create a valid match it occurs and the moving process begins again.

## Possible Future Work
Make it obvious as to which bubble the user currently has clicked; right now I just trust the user remembers.

Have some sort of scoring system; right now it is just a "ooh, I see movement" game. This would require learning how textboxes work with libGDX, which I assume is pretty simple but I haven't done it yet so maybe I'm wrong.

# Death Stats

A client side mod which tracks deaths over time and provides graphs and additional statistics.

All rights reserved.

![alt text](images/deathstats_logo_1024.png)

## Change Log

 * 1.2.0 - Remove shade and use jarjar.   Use IGuiOverlay correctly to stop drawing the hud too many times.
 * 1.1.0 - Simple death stats hud which tracks deaths and can be reset. I.e. it can be used to track deaths per hour, per day, per stream etc.
 * 1.0.0 - first version with a graph, no hud and using mixins

## TODO
 * Store each death over time and graph it (only for current deaths counter)
 * Add fabric support

## Issues
 * The counter only increments after the user respawns not on the actual death.  This is because the client event for respawn is being used and there isnt one for died.
 * Worked around https://github.com/iChun/ContentCreatorIntegration-IssuesAndDocumentation/issues/89
   * https://github.com/MinecraftForge/MinecraftForge/pull/7754 

## Guide

DeathStats tracks users deaths and provides an on-screen counter.  

The values are stored in a file in the users home directory.   
E.g. `/home/mnkybrdr/minecraft_deaths.dat` or `C:\Users\mnkybrdr\minecraft_deaths.dat`

## In Game Display

![alt text](images/stage1.png)

Deaths are shown in white when less than 50% of max

![alt text](images/stage2.png)

Deaths are shown in yellow when between 50% - 75%

![alt text](images/stage3.png)

Deaths are shown in orange when between 50% - 75%

![alt text](images/stage4.png)

Deaths are shown in red when between 90% - 100%

![alt text](images/stage5.png)

New high score is shown when you are setting the highscore.

## Commands

For the commands run : 
```/deathstats help```

```
DeathStats by mnkybrdr

/deathstats set current <value> - set current value
/deathstats set max <value> - set max value
/deathstats set visible <true|value> - hides or shows the overlay
/deathstats get current - get current value
/deathstats get max - get max value
/deathstats get highscore - has the highscore hit
/deathstats debug - shows debug information
/deathstats sound - plays high score sound
/deathstats reset - sets max and current to 0
```

## Named mobs and colors

https://www.gamergeeks.net/apps/minecraft/mob-generator/summon-zombie

```
/summon zombie ~ ~ ~ {CustomName:'[{"text":"Zoidberg","color":"dark_red","bold":true}]'}
```

## Build

```bash
java17
./gradlew genIntellijRuns
./gradlew build
```


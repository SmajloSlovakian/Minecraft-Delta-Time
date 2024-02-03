# Minecraft Deltatime
Minecraft mod making datapacks able to take control of some vanilla features

# Features
this mod adds commands to the game, which uncover some features to datapacks
the root command is /deltatime
- tickdelta -> previous tick duration in microseconds
- tickstoadd -> number to add to a counter, which you don't want to be affected by lags (more info below on how to use it effectively)
- setitemcooldown <targets (players)> <item> <duration (in ticks)> -> makes an item of that kind not be usable by the player and displays a cooldown animation on them (just like after the use of enderpearls)
- setability <targets (players)> <player ability> <value: Boolean|Float> -> basically changes the NBT value of abilities of the player. For actual effects read below
- setproperty <targets (players)> <property> -> basically changes the NBT value of the property on the player. For a list of available properties, read below


# How to use /deltatime tickdelta
example:
```
execute store result score tickstoadd objective run deltatime tickstoadd
scoreboard players operation untilactionprevious objective = untilaction objective
scoreboard players operation untilaction objective -= tickstoadd objective
execute if score untilaction objective matches ..0 unless score untilactionp objective matches ..0 run function my:action
```
In this example, we are trying to run "function my:action" after "untilaction objective" ticks run. If we want to make it independant of server performance, we need to add a larger number of ticks to the counter, if the server lags. This means that there is a possibility that the counter jumps over the 0. In order to prevent the action from not executing, we need to have another variable, which tells us the tickcount before the tickstoadd get added (subtracted). Then we need to check if the previous count is higher than 0 and the current count is exactly or less than 0.

(note that if you set the counter to a lower/higher (according to the direction of counting) value while the counter is counting and you won't also change the previous count, it will execute everything in between the previous and current value)

# Player abilities
- flying
  - boolean value
  - true: makes the player fly regardless of anything. If the player is low to the ground, the player may instantly switch back to not flying
  - false: makes the player stop flying regardless of anything. If the player is in spectator mode, the player may instantly switch back to flying.
- instabuild
  - boolean value
  - true:
    - Survival/adventure mode: player can pick block like in creative mode (doesn't apply to adventure mode) but the item is actually desynced from server and so the player can't place it. If the player already has an item however, the item doesn't deplete if it's used (this doesn't apply to totems of undying, spawn eggs and may also not apply to some other items)
  - false:
    - Creative mode: player can't pick block and depletes some items just like in survival (food depletes, buckets work like in survival and tools may break - blocks don't deplete when placed)
- invulnerable
  - boolean value
  - true:
    - works as expected - player can't be hit and doesn't take damage other than out_of_world and generic_kill
- mayBuild
  - boolean value
  - true:
    - Survival/creative mode: default value for these modes
    - Spectator mode: no effect
    - Adventure mode: makes the player able to place minecarts, place music discs into jukeboxes (they immediately pop out and the music remains playing even after breaking the jukebox), take music discs out of jukeboxes, put books into lecterns, change the duration of repeaters by two levels, put out and light up campfires and maybe more
  - false:
    - Adventure/spectator mode: default value for these modes
    - Creative/survival mode: makes the player's placed blocks immediately disappear and if instabuild is false, blocks will also deplete but only client-side. Makes players not able to place armor stands*, place minecarts*, put discs into jukeboxes, change the state of daylight sensors, put and take out books from a lectern, change comparators**, repeaters** and redstone** states, put out* and light up** * a campfire
      - \* consumes the item client-side
      - ** changes are visible client-side for a short while
- mayfly
  - boolean value
  - true: makes the player able to change their flying state when double-pressing the jump key (except spectator mode). The player is also immune to fall damage
  - false: disables the ability to change flying state when double-pressing the jump key
- flySpeed
  - float value
  - changes the speed of the player when flying (this value doesn't change when changing gamemodes, getting killed or reconnecting)
- walkSpeed
  - float value
  - changes the fov of the player (if they have fov effects turned on) and changes the walking/sprinting speed of the player when the player reconnects (this value doesn't change when changing gamemodes, getting killed or reconnecting)

(note that these values change after reconnect or changing gamemodes (except those that don't))

# Player properties
- health
  - float value
  - it is automatically clamped to the valid values
- foodExhaustionLevel
  - float value
- foodSaturationLevel
  - float value
- foodLevel
  - integer value



# Additional notes
- tickdelta subcommand also includes the time waiting for next tick
- tickstoadd subcommand also accounts for fluctuating tickdelta as it isn't always perfect 50 ms by adding its value to another variable every tick and subtracting the time per tick from the variable until it is lower than it and counting how many times it subtracted it - the subtraction count is the return value

## Please make an issue report if you find any undocumented unusual behaviour when changing the values, so I can document them.

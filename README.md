# WorldQuest

WorldQuest RPG is a single-player role-playing game set in a fantasy world.

![Screenshot](screenshot1.png "Screenshot")

## Features

### Game World

* 2 Maps
* Stairs
* Doors that open and shut
* 2 Enemy Types
* Shop that sells items
* Talking to NPCs
* Obstruct-able field of view

### Skills

* Woodcutting
* Fire-making
* Melee Combat
* Digging
* Mining
* Smelting
* Experience

### Items

* Armour
* Weapons

### Quests

* Ability to start quests
* Quest-state locked dialog
* Quest completion dialog
* Kill-X quest
* Multi-step quest support

## UX

* Tabbed interface for stats
* Map-click actions
* WASD controls
* Game messages for feedback

## Roadmap

Goals for version 1.0 (Public Release)

* Hunger, food, armour and healing
* Smithing
* A decent-size game world with rivers, bridges, forests and dungeons
* Doors that require keys
* Fetch quests
* Quest rewards
* Quest requirements
* Persistent NPC death & health
* At least one multi-step quest
* Selling items
* Hit splats
* AI with different combat strategies
* Crafting
* Ranged weapons
* Magecraft
* Prospecting
* Right-click actions
* Less text, more item graphics in UI

Goals for version 2.0

* Auto-generated, persistent dungeons
* Auto-generated, persistent surface landscape
* Deeper conversational AI
* Enemy respawn mechanics

Other aims:

* Binary data format instead of text

## Files

### `scenario`

Contains the scenarios provided with the game, each in it's own folder.

### `scenario/default`

Contains the "default" (and currently only) scenario data.

### `map000.dat`

Provides data for the starting map (see Map File Structure)

### `saves`

Contains the saved games.

### `saves/save`

Contains a single save (currently only one save game is possible).
A save comprises a copy of the scenario data (with updates based on the player's gameplay) plus a player file.

## File Structure

### Map File

Currently a new-line String based format, rather than binary to make it easier to edit. 
In the future there will be a converter to a more efficient binary format.

#### Map Tile Data:

First line, the number of lines of map data to follow (expected to be 40)
Subsequent lines, one line for each row of tiles. Each row is a comma separated list of tile types

#### NPC Data

First line, the number of NPCs to follow
Subsequent lines, one line for each NPC

#### Game Object Data

First line, the number of Game Objects to follow
Subsequent lines, one line for each Game Object

#### Room Data

First line, the number of Rooms to follow
Subsequent lines, one line for each Room

#### Adjacent Map Data

One line for each cardinal direction (N,E,S,W) providing maps for travel.
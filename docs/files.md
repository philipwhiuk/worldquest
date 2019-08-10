# Files

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

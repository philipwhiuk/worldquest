package com.whiuk.philip.worldquest;

import org.json.simple.JSONObject;

import static com.whiuk.philip.worldquest.JsonUtils.intFromObj;

public interface MovementStrategy {
    static MovementStrategy parseStrategy(JSONObject strategyData) {
        switch((String) strategyData.get("type")) {
            case "Zonal": return new ZonalMovementStrategy(
                    intFromObj(strategyData.get("x")),
                    intFromObj(strategyData.get("y")),
                    intFromObj(strategyData.get("width")),
                    intFromObj(strategyData.get("height")));
            case "Random": return new RandomMovementStrategy();
            case "Fixed": return new FixedMovementStrategy();
            default: throw new IllegalArgumentException("Unknown strategy:" + strategyData.get("type"));
        }
    }

    Direction nextDirection(NPC npc);

    String asString();
}

class FixedMovementStrategy implements MovementStrategy {
    @Override
    public Direction nextDirection(NPC npc) {
        return null;
    }

    @Override
    public String asString() {
        return "Fixed";
    }
}

class ZonalMovementStrategy implements MovementStrategy {
    private int x, y, width, height;

    ZonalMovementStrategy(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public Direction nextDirection(NPC npc) {
        int random = RandomSource.getRandom().nextInt(4);
        Direction toUse = null;
        switch(random) {
            case 0: if (npc.y > y) { toUse = Direction.NORTH; } else { toUse = Direction.SOUTH; } break;
            case 1: if (npc.y < y+height) { toUse = Direction.SOUTH; } else { toUse = Direction.NORTH; } break;
            case 2: if (npc.x > x) { toUse = Direction.WEST; } else { toUse = Direction.EAST; } break;
            case 3: if (npc.x < x+width) { toUse = Direction.EAST; } else { toUse = Direction.WEST; } break;
        }
        return toUse;
    }

    @Override
    public String asString() {
        return "Zonal,"+x+","+y+","+width+","+height;
    }
}

class RandomMovementStrategy implements MovementStrategy {

    @Override
    public Direction nextDirection(NPC npc) {
        switch(RandomSource.getRandom().nextInt(4)) {
            case 0: return Direction.NORTH;
            case 1: return Direction.SOUTH;
            case 2: return Direction.EAST;
            case 3: return Direction.WEST;
        }
        return null;
    }

    @Override
    public String asString() {
        return "Random";
    }
}
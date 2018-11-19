package com.whiuk.philip.worldquest;

public interface MovementStrategy {
    static MovementStrategy parseStrategy(String data) {
        String movementStrategyData[] = data.split(",");
        switch(movementStrategyData[0]) {
            case "Zonal": return new ZonalMovementStrategy(
                    Integer.parseInt(movementStrategyData[1]),
                    Integer.parseInt(movementStrategyData[2]),
                    Integer.parseInt(movementStrategyData[3]),
                    Integer.parseInt(movementStrategyData[4]));
            case "Random": return new RandomMovementStrategy();
            case "Fixed": return new FixedMovementStrategy();
        }
        throw new IllegalArgumentException("Unknown strategy:" + movementStrategyData[0]);
    }

    Direction nextDirection(NPC npc);
}

class FixedMovementStrategy implements MovementStrategy {
    @Override
    public Direction nextDirection(NPC npc) {
        return null;
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
        System.out.println(toUse);
        return toUse;
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
}
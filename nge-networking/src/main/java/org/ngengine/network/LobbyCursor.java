package org.ngengine.network;

import java.time.Instant;
import java.util.List;

public class LobbyCursor {
    public enum Direction {
        NEWER,
        OLDER
    }

    private final Instant until;
    private final List<Lobby> lobbies;
    private final Instant since;
    private Direction direction = Direction.OLDER;

    public LobbyCursor(Direction direction, Instant until, Instant since, List<Lobby> lobbies) {
        this.until = until;
        this.lobbies = lobbies;
        this.since = since;
    }

    public Instant until() {
        return until;
    }

    public Instant since() {
        return since;
    }

    public List<Lobby> get() {
        return lobbies;
    }    

    public boolean hasMore() {
        return lobbies.size() > 0;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Direction direction() {
        return direction;
    }

    @Override
    public String toString() {
        return "LobbyCursor{" + "direction=" + direction + ", until=" + until + ", since=" + since + '}';
    }   



    
}

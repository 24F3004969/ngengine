package org.ngengine.config;

import java.util.List;

public class Relays {
    
    public static RelayList nostr = new RelayList(
        "nostr",
        "relays.nostr.json",
        List.of(
            "wss://relay.ngengine.org",
            "wss://relay2.ngengine.org"
        )
    );

    public static RelayList blossom = new RelayList(
        "blossom",
        "relays.blossom.json",
        List.of(
            "wss://relay.ngengine.org",
            "wss://relay2.ngengine.org"
        )
    );

}

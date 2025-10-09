package org.ngengine.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ngengine.platform.NGEUtils;

public class RelayList {
    private static final Logger log = Logger.getLogger(RelayList.class.getName());
    private final List<String> defaultFallback;
    private final String unit;
    private final NGEAppSettings settings;

    public RelayList(NGEAppSettings settings, String unit,  List<String> defaultFallback) {
        this.settings = settings;
        this.unit = unit;
        this.defaultFallback = defaultFallback;
    }

 
    public void set(String group, List<String> relays){
        Map<Object,Object> raw = (Map<Object,Object>) settings.get("relays");
        if(raw==null){
            raw = new HashMap<>();
            settings.put("relays", raw);
        }
        Map<Object,Object> unitMap = (Map<Object,Object>) raw.get(this.unit);
        if(unitMap==null){
            unitMap = new HashMap<>();
            raw.put(this.unit, unitMap);
        }
        unitMap.put(group, relays);
        settings.checkRestartRequired("relatys");
    }


    private Map<String,List<String>> get(){        
        try{
            Map<Object,Object> raw = settings.get("relays");
            if(raw==null) return Map.of();
            raw = (Map<Object,Object>) raw.get(this.unit);               
            if(raw==null) return Map.of();
            Map<String,List<String>> parsed = new HashMap<>();
            for(Object e : raw.entrySet()){
                String key = NGEUtils.safeString(((Entry)e).getKey());
                List<String> vals = NGEUtils.safeStringList(((Entry)e).getValue());
                parsed.put(key, vals); 
            }
            return Collections.unmodifiableMap(parsed);        
        }catch(Exception ex){
            log.log(Level.WARNING, "Failed to parse "+this.unit+" relay config", ex);
        }
        return Map.of();
    }


    public List<String> get(String group){
        Map<String,List<String>> map = get();
        if(map != null){
            List<String> relays = map.get(group);
            if(relays==null)  relays = map.get("default");
            if(relays != null && !relays.isEmpty()){
                return relays;
            }
        }
        return defaultFallback;
    }
    
   
 
}

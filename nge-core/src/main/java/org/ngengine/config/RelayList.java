package org.ngengine.config;

import java.io.BufferedInputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ngengine.platform.NGEPlatform;
import org.ngengine.platform.NGEUtils;
import org.ngengine.platform.transport.NGEHttpResponse;

import com.jme3.export.binary.ByteUtils;
import com.jme3.util.res.Resources;

public class RelayList {
    private static final Logger log = Logger.getLogger(RelayList.class.getName());
    private final String configPath;
    private final List<String> defaultFallback;
    private final String unit;
    private volatile Map<String, List<String>> list;

    public RelayList(String unit, String configPath, List<String> defaultFallback) {
        this.unit = unit;
        this.defaultFallback = defaultFallback;
        this.configPath = configPath;
    }

    @SuppressWarnings("unchecked")
    private Map<String,List<String>> parseConfig(String json){
        try{
            Map<Object,Object> raw = NGEPlatform.get().fromJSON(json, Map.class);
            if(raw != null){
                Map<String,List<String>> parsed = new HashMap<>();
                for(Entry<Object, Object> e : raw.entrySet()){
                    String key = NGEUtils.safeString(e.getKey());
                    List<String> vals = NGEUtils.safeStringList(e.getValue());
                    parsed.put(key, vals); 
                }
                return parsed;
            }
        }catch(Exception ex){
            log.log(Level.WARNING, "Failed to parse "+this.unit+" relay config", ex);
        }
        return null;
    }

    private Map<String,List<String>> readConfig(String path){
        try{
            if(path.startsWith("http://") || path.startsWith("https://")){
                NGEHttpResponse resp  = NGEPlatform.get().httpRequest("GET", NGEUtils.safeURI(path).toString(), null, null, null).await();
                if(!resp.status) throw new Exception("Failed to fetch "+this.unit+" relay config from "+path);
                String content = new String(resp.body, StandardCharsets.UTF_8);
                return parseConfig(content);
            } else{
                URL url = Resources.getResource(path);
                if(url == null) throw new Exception("Failed to find "+this.unit+" relay config from "+path);
                byte data[] = ByteUtils.readFully(new BufferedInputStream(url.openStream()));
                String content = new String(data, StandardCharsets.UTF_8);
                return parseConfig(content);
            }
        }catch(Exception ex){
            log.log(Level.WARNING, "Failed to read "+this.unit+" relay config from "+path, ex);
        }
        return null;
    }



    private Map<String,List<String>> get(){
        if(this.list != null ) return this.list;
        String overrideContent = System.getProperty("ngengine.relays."+this.unit+".config");
        if(overrideContent != null && !overrideContent.isBlank()){
            Map<String,List<String>> config = parseConfig(overrideContent);
            if(config != null){
                return this.list = config;
            }

        }

        String overridePath = System.getProperty("ngengine.relays."+this.unit);
        if(overridePath != null && !overridePath.isBlank()){
            Map<String,List<String>> config = readConfig(overridePath);
            if(config != null){
                return  this.list = config;
            }
        }

        if(configPath != null && !configPath.isBlank()){
            Map<String,List<String>> config = readConfig(configPath);
            if(config != null){
                return  this.list = config;
            }
        }

        String defaultPath = "relays."+this.unit+".json";
        Map<String,List<String>> config = readConfig(defaultPath);
        if(config != null){
            return  this.list = config;
        }

        return null;       
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

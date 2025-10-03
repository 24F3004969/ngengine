 
package org.ngengine.web.filesystem;

import com.jme3.asset.*;
import com.jme3.util.res.Resources;

import java.net.URL;
import java.util.logging.Logger;
 
public class WebLocator implements AssetLocator {

    private static final Logger logger = Logger.getLogger(WebLocator.class.getName());
    private String root = "";

    public WebLocator(){
    }

    @Override
    public void setRootPath(String rootPath) {
        this.root = rootPath;
        if (root.equals("/"))
            root = "";
        else if (root.length() > 1){
            if (root.startsWith("/")){
                root = root.substring(1);
            }
            if (!root.endsWith("/"))
                root += "/";
        }
    }
    
    @Override
    public AssetInfo locate(AssetManager manager, AssetKey key) {
        URL url;

        String name = key.getName();
        if (name.startsWith("/"))
            name = name.substring(1);

        name = root + name;

        url = Resources.getResource( name);     
        

        
        if (url == null){
            return null;
        }
        WebAssetInfo info = new WebAssetInfo(manager, key, url, null);         
        return info;
          
    }
}

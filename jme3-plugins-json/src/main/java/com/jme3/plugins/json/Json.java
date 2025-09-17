/*
 * Copyright (c) 2009-2023 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.plugins.json;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A json parser factory that allows you to set the parser to use.
 * 
 * @author Riccardo Balbo
 */
public class Json {
    /**
     * The property name to set the parser to use.
     * Should be set automatically by the JmeSystemDelegate.
     * Note: changing this property after the first call to Json.create() will have no effect.
     */
    public static final String PROPERTY_JSON_PARSER_IMPLEMENTATION = "com.jme3.JsonParserImplementation";
    private static final Logger LOGGER = Logger.getLogger(Json.class.getName());
    private static final String DEFAULT_JSON_PARSER_IMPLEMENTATION = "com.jme3.plugins.gson.GsonParser";

    private static JsonParser parser;

    /**
     * Set the parser to use.
     * 
     * @param parser the json parser to use
     */
    public static void setParser(JsonParser parser) {
        Json.parser = parser;
    }

    /**
     * @deprecated Use {@link #setParser(JsonParser)} instead.
     */
    @Deprecated
    public static void setParser(Class<? extends JsonParser> clazz) {
        try {
            setParser(clazz.getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate JsonParser class " + clazz.getName(), e);
        }
    }


    @SuppressWarnings("unchecked")
    private static Class<? extends JsonParser> findJsonParser(String className) {
        Class<?> clazz = null;

        try {
            clazz = Class.forName(className);
        } catch (final Throwable e) {
            LOGGER.log(Level.WARNING, "Unable to access {0}", className);
        }

        if (clazz != null && !JsonParser.class.isAssignableFrom(clazz)) {
            LOGGER.log(Level.WARNING, "{0} does not implement {1}", new Object[] { className, JsonParser.class.getName() });
            clazz = null;
        }

        return (Class<? extends JsonParser>) clazz;
    }

    /**
     * @deprecated Use {@link #get()} instead.
     * This method is deprecated and redirected to {@link #get()}
     * that returns a single shared instance of JsonParser.
     */
    @Deprecated
    public static JsonParser create() {
        return get();
    }
  
    /**
     * Create a new JsonParser instance.
     * 
     * @return a new JsonParser instance
     */

    public static JsonParser get() {
        if (Json.parser == null) {
            Class<? extends JsonParser> clazz = null;
            String userDefinedImpl = System.getProperty(PROPERTY_JSON_PARSER_IMPLEMENTATION, null);
            if (userDefinedImpl != null) {
                LOGGER.log(Level.FINE, "Loading user defined JsonParser implementation {0}", userDefinedImpl);
                clazz = findJsonParser(userDefinedImpl);
            }
            if (Json.parser == null) {
                LOGGER.log(Level.FINE, "No usable user defined JsonParser implementation found, using default implementation {0}", DEFAULT_JSON_PARSER_IMPLEMENTATION);
                clazz = findJsonParser(DEFAULT_JSON_PARSER_IMPLEMENTATION);
            }
            if (clazz == null) {
                throw new RuntimeException("No JsonParser implementation found");
            }
            try {
                Json.parser = clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Could not instantiate JsonParser class " + clazz.getName(), e);
            }
        }
        return Json.parser;
      

      
    }
}

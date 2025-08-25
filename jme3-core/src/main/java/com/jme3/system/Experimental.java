package com.jme3.system;
import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.CLASS) 
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
public @interface Experimental {
}
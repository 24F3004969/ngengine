package org.ngengine.web.patches;

import org.teavm.classlib.impl.reflection.Flags;
import org.teavm.platform.PlatformClass;

public abstract class ClassPatch {
    public abstract PlatformClass getPlatformClass();

    public boolean isAnnotation() {
        PlatformClass platformClass = getPlatformClass();
        return (platformClass.getMetadata().getFlags() & Flags.ANNOTATION) != 0;
    }
}
    
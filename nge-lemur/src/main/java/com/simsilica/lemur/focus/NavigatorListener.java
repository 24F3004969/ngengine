package com.simsilica.lemur.focus;

import com.jme3.scene.Spatial;

public interface NavigatorListener {

    default boolean beforeNavigatorScroll(ScrollDirection dir, double delta) {
        return true;
    }

    default boolean afterNavigatorScroll(ScrollDirection dir, double delta) {
        return true;
    }

    default boolean beforeNavigatorAction(boolean pressed) {
        return true;
    }

    default boolean afterNavigatorAction(boolean pressed) {
        return true;
    }

    default boolean beforeNavigatorNavigate(TraversalDirection dir) {
        return true;
    }

    default boolean afterNavigatorNavigate(TraversalDirection dir) {
        return true;
    }

    default boolean beforeNavigatorFocus(Spatial newFocus) {
        return true;
    }

    default boolean afterNavigatorFocus(Spatial newFocus) {
        return true;
    }

    default boolean beforeNavigatorNavigateTo(TraversalDirection dir, Spatial from, Spatial to) {
        return true;
    }

}

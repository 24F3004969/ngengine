package com.simsilica.lemur.focus;

public enum TraversalDirection { 
    /**
     *  For containers that provide row/column navigation, this
     *  represents the previous row in a similar column as defined
     *  by the layout/navigation implementation.  In many layouts,
     *  this will be synonymous with TraversalDirection.Previous.
     *  It is up to the specific focus layout how wrapping is handled.
     */
    Up,
     
    /**
     *  For containers that provide row/column navigation, this
     *  represents the next row in a similar column as defined
     *  by the layout/navigation implementation.  In many layouts,
     *  this will be synonymous with TraversalDirection.Next.
     *  It is up to the specific focus layout how wrapping is handled.
     */
    Down, 
     
    /**
     *  For containers that provide row/column navigation, this
     *  represents the previous column in a similar row as defined
     *  by the layout/navigation implementation.  In many layouts,
     *  this will be synonymous with TraversalDirection.Previous.
     *  It is up to the specific focus layout how wrapping is handled.
     */
    Left, 
     
    /**
     *  For containers that provide row/column navigation, this
     *  represents the next column in a similar row as defined
     *  by the layout/navigation implementation.  In many layouts,
     *  this will be synonymous with TraversalDirection.Next.
     *  It is up to the specific focus layout how wrapping is handled.
     */
    Right, 
     
    /**
     *  Represents the next logical navigation step in a focus
     *  traversal workflow.  In general, using TraversalDirection.Next should
     *  be able to take you through all focusable elements in a container, from
     *  TraversalDirection.PageHome to TraversalDirection.PageEnd.  It is also
     *  typical that in a root-level container, Next should wrap to PageHome when 
     *  reaching the end.  It is not required. 
     */
    Next, 
     
    /**
     *  Represents the previous logical navigation step in a focus
     *  traversal workflow.  In general, using TraversalDirection.Previous should
     *  be able to take you through all focusable elements in a container, from
     *  TraversalDirection.PageEnd to TraversalDirection.PageHome.  It is also
     *  typical that in a root-level container, Previous should wrap to PageEnd when 
     *  on the first/home element.  It is not required. 
     */
    Previous
    
}
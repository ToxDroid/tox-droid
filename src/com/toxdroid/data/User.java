
package com.toxdroid.data;

import com.toxdroid.ui.UserCardFragment;

import im.tox.jtoxcore.ToxUserStatus;

/**
 * An interface representing a person on the Tox network. Implementators of this interface can be displayed on {@link UserCardFragment}.
 * 
 * 
 */
public interface User {
    /**
     * Gets the person's username.
     * @return the username
     */
    public String getName();
    
    /**
     * Gets the person's status message
     * @return the status message
     */
    public String getStatusMessage();
    
    /**
     * Gets the person's ToxUserStatus
     * @return the person's ToxUserStatus
     */
    public ToxUserStatus getStatus();
    
    /**
     * Gets the person's online status
     * @return the online status
     */
    public boolean isOnline();
}

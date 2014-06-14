
package com.toxdroid.tox;

import im.tox.jtoxcore.FriendExistsException;
import im.tox.jtoxcore.ToxUserStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;

import android.os.Handler;
import android.os.Looper;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.toxdroid.data.Contact;
import com.toxdroid.util.Util;

/**
 * A Tox friend list.
 * 
 * 
 */
public class FriendList extends Observable implements im.tox.jtoxcore.FriendList<Contact> {
    /**
     * The main friend list collection. Access to this field is thread-safe.
     */
    private List<Contact> friends = Collections.synchronizedList(new ArrayList<Contact>());
    
    /**
     * A copy of the friend list, with some differences. Firstly, updates to this collection are always on the UI thread. Secondly, any
     * observers of this class will be notified immediately after it is updated. This collection is intended for use with an adapter.
     */
    private List<Contact> friendsUi = Collections.synchronizedList(new ArrayList<Contact>());
    
    /**
     * A handler which runs on the UI thread.
     */
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    
    @Override
    public Contact addFriend(int fn) throws FriendExistsException {
        if (getByFriendNumber(fn) != null)
            throw new FriendExistsException(fn);
        
        final Contact friend = new Contact(fn);
        friends.add(getInsertIndex(friend), friend);
        
        // Update the UI list and notify observers
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                friendsUi.add(friend);
                notifyObservers();
            }
        });
        return friend;
    }
    
    @Override
    public Contact addFriendIfNotExists(int fn) {
        try {
            Contact out = getByFriendNumber(fn);
            return out == null ? addFriend(fn) : out;
        } catch (FriendExistsException e) {
            throw new AssertionError();
        }
    }
    
    @Override
    public void removeFriend(int fn) {
        final Contact target = getByFriendNumber(fn);
        friends.remove(target);
        
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                friendsUi.remove(target);
                notifyObservers();
            }
        });
    }
    
    /**
     * Removes all elements from the list.
     */
    public void clear() {
        friends.clear();
        
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                friendsUi.clear();
                notifyObservers();
            }
        });
    }
    
    private int getInsertIndex(Contact friend) {
        synchronized (friends) {
            return 1 + Collections.binarySearch(friends, friend, new Comparator<Contact>() {
                @Override
                public int compare(Contact lhs, Contact rhs) {
                    return Util.compare(lhs.getFriendnumber(), rhs.getFriendnumber());
                }
            });
        }
    }
    
    /**
     * Check if the given address is already added as a friend.
     * @param address the full Tox ID (including nospam / checksum)
     * @return true if added
     */
    public boolean exists(String toxId) {
        final String publicKey = ToxCore.getPublicKey(toxId);
        
        synchronized (friends) {
            Contact existing = Iterables.find(friends, new Predicate<Contact>() {
                @Override
                public boolean apply(Contact friend) {
                    return ToxCore.getPublicKey(friend.getId()).equals(publicKey);
                }
            }, null);
            
            return existing != null;
        }
    }
    
    @Override
    public List<Contact> all() {
        return Collections.unmodifiableList(friends);
    }
    
    public List<Contact> allUi() {
        return Collections.unmodifiableList(friendsUi);
    }
    
    @Override
    public Contact getByFriendNumber(final int fn) {
        synchronized (friends) {
            return Iterables.find(friends, new Predicate<Contact>() {
                @Override
                public boolean apply(Contact friend) {
                    return friend.getFriendnumber() == fn;
                }
            }, null);
        }
    }
    
    @Override
    public Contact getById(final String id) {
        synchronized (friends) {
            return Iterables.find(friends, new Predicate<Contact>() {
                @Override
                public boolean apply(Contact friend) {
                    return friend.getId().equals(id);
                }
            }, null);
        }
    }
    
    public List<Contact> getByOnlineStatus(final boolean online) {
        synchronized (friends) {
            return Lists.newArrayList(Iterables.filter(friends, new Predicate<Contact>() {
                @Override
                public boolean apply(Contact friend) {
                    return friend.isOnline() == online;
                }
            }));
        }
    }
    
    @Override
    public List<Contact> getByName(final String name, final boolean ignorecase) {
        synchronized (friends) {
            return Lists.newArrayList(Iterables.filter(friends, new Predicate<Contact>() {
                @Override
                public boolean apply(Contact friend) {
                    String compare = friend.getName();
                    if (ignorecase)
                        compare.toLowerCase();
                    
                    return compare.equals(ignorecase ? name.toLowerCase() : name);
                }
            }));
        }
    }
    
    @Override
    public List<Contact> searchFriend(final String partial) {
        synchronized (friends) {
            return Lists.newArrayList(Iterables.filter(friends, new Predicate<Contact>() {
                @Override
                public boolean apply(Contact friend) {
                    return friend.getName().contains(partial);
                }
            }));
        }
    }
    
    @Override
    public List<Contact> getByStatus(final ToxUserStatus status) {
        synchronized (friends) {
            return Lists.newArrayList(Iterables.filter(friends, new Predicate<Contact>() {
                @Override
                public boolean apply(Contact friend) {
                    return friend.getStatus().equals(status);
                }
            }));
        }
    }
    
    @Override
    public List<Contact> getOnlineFriends() {
        return getByOnlineStatus(true);
    }
    
    @Override
    public List<Contact> getOfflineFriends() {
        return getByOnlineStatus(false);
    }
}

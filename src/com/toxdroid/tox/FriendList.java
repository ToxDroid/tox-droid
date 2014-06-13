
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
import com.toxdroid.util.Util;

/**
 * A Tox friend list.
 * 
 * 
 */
public class FriendList extends Observable implements im.tox.jtoxcore.FriendList<ToxFriend> {
    /**
     * The main friend list collection. Access to this field is thread-safe.
     */
    private List<ToxFriend> friends = Collections.synchronizedList(new ArrayList<ToxFriend>());
    
    /**
     * A copy of the friend list, with some differences. Firstly, updates to this collection are always on the UI thread. Secondly, any
     * observers of this class will be notified immediately after it is updated. This collection is intended for use with an adapter.
     */
    private List<ToxFriend> friendsUi = Collections.synchronizedList(new ArrayList<ToxFriend>());
    
    /**
     * A handler which runs on the UI thread.
     */
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    
    @Override
    public ToxFriend addFriend(int fn) throws FriendExistsException {
        if (getByFriendNumber(fn) != null)
            throw new FriendExistsException(fn);
        
        final ToxFriend friend = new ToxFriend(fn);
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
    public ToxFriend addFriendIfNotExists(int fn) {
        try {
            ToxFriend out = getByFriendNumber(fn);
            return out == null ? addFriend(fn) : out;
        } catch (FriendExistsException e) {
            throw new AssertionError();
        }
    }
    
    @Override
    public void removeFriend(int fn) {
        final ToxFriend target = getByFriendNumber(fn);
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
    
    private int getInsertIndex(ToxFriend friend) {
        synchronized (friends) {
            return 1 + Collections.binarySearch(friends, friend, new Comparator<ToxFriend>() {
                @Override
                public int compare(ToxFriend lhs, ToxFriend rhs) {
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
            ToxFriend existing = Iterables.find(friends, new Predicate<ToxFriend>() {
                @Override
                public boolean apply(ToxFriend friend) {
                    return ToxCore.getPublicKey(friend.getId()).equals(publicKey);
                }
            }, null);
            
            return existing != null;
        }
    }
    
    @Override
    public List<ToxFriend> all() {
        return Collections.unmodifiableList(friends);
    }
    
    public List<ToxFriend> allUi() {
        return Collections.unmodifiableList(friendsUi);
    }
    
    @Override
    public ToxFriend getByFriendNumber(final int fn) {
        synchronized (friends) {
            return Iterables.find(friends, new Predicate<ToxFriend>() {
                @Override
                public boolean apply(ToxFriend friend) {
                    return friend.getFriendnumber() == fn;
                }
            }, null);
        }
    }
    
    @Override
    public ToxFriend getById(final String id) {
        synchronized (friends) {
            return Iterables.find(friends, new Predicate<ToxFriend>() {
                @Override
                public boolean apply(ToxFriend friend) {
                    return friend.getId().equals(id);
                }
            }, null);
        }
    }
    
    public List<ToxFriend> getByOnlineStatus(final boolean online) {
        synchronized (friends) {
            return Lists.newArrayList(Iterables.filter(friends, new Predicate<ToxFriend>() {
                @Override
                public boolean apply(ToxFriend friend) {
                    return friend.isOnline() == online;
                }
            }));
        }
    }
    
    @Override
    public List<ToxFriend> getByName(final String name, final boolean ignorecase) {
        synchronized (friends) {
            return Lists.newArrayList(Iterables.filter(friends, new Predicate<ToxFriend>() {
                @Override
                public boolean apply(ToxFriend friend) {
                    String compare = friend.getName();
                    if (ignorecase)
                        compare.toLowerCase();
                    
                    return compare.equals(ignorecase ? name.toLowerCase() : name);
                }
            }));
        }
    }
    
    @Override
    public List<ToxFriend> searchFriend(final String partial) {
        synchronized (friends) {
            return Lists.newArrayList(Iterables.filter(friends, new Predicate<ToxFriend>() {
                @Override
                public boolean apply(ToxFriend friend) {
                    return friend.getName().contains(partial);
                }
            }));
        }
    }
    
    @Override
    public List<ToxFriend> getByStatus(final ToxUserStatus status) {
        synchronized (friends) {
            return Lists.newArrayList(Iterables.filter(friends, new Predicate<ToxFriend>() {
                @Override
                public boolean apply(ToxFriend friend) {
                    return friend.getStatus().equals(status);
                }
            }));
        }
    }
    
    @Override
    public List<ToxFriend> getOnlineFriends() {
        return getByOnlineStatus(true);
    }
    
    @Override
    public List<ToxFriend> getOfflineFriends() {
        return getByOnlineStatus(false);
    }
}

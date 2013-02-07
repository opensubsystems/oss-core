/*
 * Copyright (C) 2003 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
 * 
 * This file is part of OpenSubsystems.
 *
 * OpenSubsystems is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package org.opensubsystems.core.util;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.opensubsystems.core.data.DataObject;

/**
 * This class encapsulate context in which certain method call execution is 
 * made. This class establishes that every call is made in the context  of a user 
 * and a domain where the user belongs. 
 * 
 * This class is independent from the security implementation, therefore
 * it doesn't refer to any specific security package. Overridden class actually
 * implement concept of users and domains as proposed in this class. 
 *  
 * @author bastafidli
 */
public class CallContext extends OSSObject
{
   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Lock used in synchronized sections.
    */
   private static final String IMPL_LOCK = "IMPL_LOCK";

   // Cached values ////////////////////////////////////////////////////////////
  
   /**
    * Reference to the instance actually in use.
    */
   private static CallContext s_defaultInstance;

   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Each thread calling this class will store here information about current 
    * user. Since we support switching of user identities, this variable 
    * contains stack where new user identity is pushed when it's context is 
    * established and from which it is popped, when the context is reset. This 
    * has an implication and that is when new thread is created and it inherits 
    * the parent's value, it in fact inherits the stack and therefore the parent 
    * and child would share the same stack. This is not desirable so the child 
    * thread has to create it's own stack and copy the content of the parent 
    * stack into it so that from that point these two stacks can be manipulated 
    * separately. What is the questions is if all identities on the stack should 
    * be copied or only the current one. For now we copy all of them.
    */
   private ThreadLocal<Stack<Principal>> m_currentUser; 

   /**
    * Each thread calling this class will store here information about current 
    * session. The same discussion about stack as for m_currentUser applies to 
    * this class.
    */
   private ThreadLocal<Stack<String>> m_currentSession; 

   /**
    * Each thread calling this class will store here error or information 
    * messages produced during call, which will can be later displayed to the 
    * user. 
    */
   private ThreadLocal<Messages> m_messages; 

   /**
    * Generic cache where anybody can cache anything for the context under a key. 
    */
   private ThreadLocal<Map<String, Map>> m_cache; 

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Default constructor.
    */
   public CallContext(
   )
   {
      // Make it inheritable so that SWT background loaders can act on behalf 
      // of user who is using  
      m_currentUser = new InheritableThreadLocal<Stack<Principal>>()
      {
         protected Stack<Principal> childValue(
               Stack<Principal> parentValue
         )
         {
            Stack<Principal> childStack = null;
                                
            if ((parentValue != null) && (!parentValue.isEmpty()))
            {
               // See discussion above why we need to copy this
               childStack = new Stack<Principal>();
               childStack.addAll(parentValue);   
            }
                                
            return childStack;
         }   
      };
      m_currentSession = new InheritableThreadLocal<Stack<String>>()
      {
         protected Stack<String> childValue(
               Stack<String> parentValue
         )
         {
            Stack<String> childStack = null;
                                
            if ((parentValue != null) && (!parentValue.isEmpty()))
            {
               // See discussion above why we need to copy this
               childStack = new Stack<String>();
               childStack.addAll(parentValue);   
            }
                                
            return childStack;
         }   
      };
      // Error messages are not inheritable so that the thread don't write
      // is own error message to parent. If it would be inheritable and the
      // child value is not overridden as above, every error messages produced
      // by child thread would be visible by parent
      m_messages = new ThreadLocal<Messages>();
      m_cache = new ThreadLocal<Map<String, Map>>();
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * Get the default instance.
    *
    * @return CallContext
    */
   public static CallContext getInstance(
   )
   {
      if (s_defaultInstance == null)
      {
         // Only if the default instance wasn't set by other means create a new 
         // one Synchronize just for the creation
         synchronized (IMPL_LOCK)
         {
            if (s_defaultInstance == null)
            {
               setInstance(new CallContext());
            }
         }   
      }
      
      return s_defaultInstance;
   }
   
   /**
    * Set the default instance. This instance will be returned by getInstance 
    * method until it is changed.
    *
    * @param defaultInstance - new default instance
    * @see #getInstance
    */
   public static void setInstance(
      CallContext defaultInstance
   )
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         
         assert defaultInstance != null : "Default instance cannot be null";
      }   
      
      synchronized (IMPL_LOCK)
      {
         s_defaultInstance = defaultInstance;
      }   
   }
      
   /**
    * Get identification of current user.
    * 
    * @return Principal - current user identification or null if not known
    */
   public Principal getCurrentUser(
   )
   {
      Stack<Principal> userStack = m_currentUser.get();

      if ((userStack == null) || (userStack.isEmpty()))
      {
         return null;
      }
      
      return userStack.peek(); 
   }

   /**
    * Get identification of current session, which is jus a unique string which
    * can identify the each individual session established between client and
    * application. 
    * 
    * @return String - current session identification or null if not known
    */
   public String getCurrentSession(
   )
   {
      Stack<String> sessionStack = m_currentSession.get();

      if ((sessionStack == null) || (sessionStack.isEmpty()))
      {
         return null;
      }
      
      return sessionStack.peek(); 
   }

   /**
    * Get current user ID.
    * 
    * @return long - current user ID
    */
   public long getCurrentUserId(
   )
   {
      return DataObject.NEW_ID;
   }

   /**
    * Set who is the current user and session for this call context.
    * Values are stored in stack.
    * 
    * @param user - information about user who is making the call.
    * @param session - information about session in whic user is making the call.
    */
   public void setCurrentUserAndSession(
      Principal user,
      String    session
   )
   {
      Stack<Principal> userStack = m_currentUser.get();
      if (userStack == null)
      {
         m_currentUser.set(new Stack<Principal>());
         userStack = m_currentUser.get();
      }
      userStack.push(user);

      Stack<String> sessionStack = m_currentSession.get();
      if (sessionStack == null)
      {
         m_currentSession.set(new Stack<String>());
         sessionStack = m_currentSession.get();
      }
      sessionStack.push(session);
   }

   /**
    * Removes who is the current user session for this call context from stack.
    * Whatever was the current user and session before will be restored. 
    */
   public void resetCurrentUserAndSession(
   )
   {
      Stack<Principal> userStack = m_currentUser.get();
      if ((userStack != null) &&  (!userStack.isEmpty()))
      {
         userStack.pop();
      }
      Stack<String> sessionStack = m_currentSession.get();
      if ((sessionStack != null) &&  (!sessionStack.isEmpty()))
      {
         sessionStack.pop();
      }
   }

   /**
    * Get current domain ID, which can be used to limit current user or session
    * to access only data in this domain. Domain is just partition to which data
    * might be allocated.
    * 
    * @return long - current domain ID
    */
   public long getCurrentDomainId(
   )
   {
      return DataObject.NEW_ID;
   }

   /**
    * Get messages to display to user for current call.
    * 
    * @return Messages - error or information messages to display to user 
    *                    related to current call, never null
    */
   public Messages getMessages(
   )
   {
      Messages msgs = null;
      
      msgs = m_messages.get();
      
      if (msgs == null)
      {   
         msgs = new Messages();
         setMessages(msgs);
      }
      
      return msgs;
   }

   /**
    * Set the error or information message object to collect messages to display 
    * to user for current call
    * 
    * @param msgs - messages for current call
    */
   protected void setMessages(
      Messages msgs
   )
   {
      m_messages.set(msgs);
   }

   /**
    * Removes all messages for current call. After this call, there won't be 
    * any error or information messages to display to the user.
    */
   public void resetMessages(
   )
   {
      m_messages.set(null);
   }

   /**
    * Get custom cache for given key.
    * 
    * @param  strCacheKey - key for which to get cache 
    * @return Map - cache registered under given key. This will always return 
    *                Map and never null.
    */
   public Map getCache(
      String strCacheKey
   )
   {
      Map<String, Map> cache;
      Map              customcache;
      
      cache = m_cache.get();
      
      if (cache == null)
      {   
         cache = new HashMap<String, Map>();
         setCache(cache);
      }
      customcache = cache.get(strCacheKey);
      if (customcache == null)
      {
         customcache = new HashMap();
         cache.put(strCacheKey, customcache);
      }
      
      return customcache;
   }

   /**
    * Set custom cache for this call context
    * 
    * @param cache - cache for current call context
    */
   protected void setCache(
      Map<String, Map> cache
   )
   {
      m_cache.set(cache);
   }

   /**
    * Removes all cached items for current custom cache.
    */
   public void resetCache(
   )
   {
      m_cache.set(null);
   }

   /**
    * Removes context (all information) for current call from stack and restore
    * the previous user session.
    */
   public void reset(
   )
   {
      resetCurrentUserAndSession();
      resetMessages();
      resetCache();
   }
}

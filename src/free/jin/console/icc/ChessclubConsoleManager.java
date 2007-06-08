/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2007 Alexander Maryanovsky.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package free.jin.console.icc;

import free.jin.Game;
import free.jin.ServerUser;
import free.jin.console.Console;
import free.jin.console.ConsoleDesignation;
import free.jin.console.ConsoleManager;
import free.jin.console.ics.ChannelChatConsoleDesignation;
import free.jin.console.ics.ShoutChatConsoleDesignation;
import free.jin.event.ChatEvent;
import free.jin.ui.PreferencesPanel;


/**
 * An extension of the default ConsoleManager for the chessclub.com server.
 */

public class ChessclubConsoleManager extends ConsoleManager{
  
  
  
  /**
   * Returns an ICC-specific system console designation.
   */
  
  protected ConsoleDesignation createSystemConsoleDesignation(){
    return new ChessclubSystemConsoleDesignation(getConn(), getEncoding());
  }
  
  
  
  /**
   * Returns an ICC-specific help console designation.
   */
  
  protected ConsoleDesignation createHelpConsoleDesignation(boolean isCloseable){
    return new ChessclubHelpConsoleDesignation(getConn(), getEncoding(), isCloseable);
  }
  
  
  
  /**
   * Returns an ICC-specific general chat console designation.
   */
  
  protected ConsoleDesignation createGeneralChatConsoleDesignation(boolean isCloseable){
    if (getUser().getServer().getId().equals("wcl"))
      return new ChannelChatConsoleDesignation(getConn(), 250, getEncoding(), isCloseable);
    else
      return new ShoutChatConsoleDesignation(getConn(), getEncoding(), isCloseable);
  }
  
  
  
  /**
   * Creates an ICC-specific personal chat console designation.
   */
  
  protected ConsoleDesignation createPersonalChatConsoleDesignation(ServerUser user, boolean isCloseable){
    return new ChessclubPersonalChatConsoleDesignation(getConn(), user, getEncoding(), isCloseable);
  }
  
  
  
  /**
   * Creates an ICC-specific game chat console designation.
   */
  
  protected ConsoleDesignation createGameConsoleDesignation(Game game){
    return new ChessclubGameConsoleDesignation(getConn(), game, getEncoding());
  }
  
  
  
  /**
   * Creates a <code>ChessclubConsole</code> with the specified designation.
   */

  protected Console createConsole(ConsoleDesignation designation){
    return new ChessclubConsole(this, designation);
  }
  
  
  
  /**
   * Return a <code>PreferencesPanel</code> for changing the console
   * manager's settings.
   */

  public PreferencesPanel getPreferencesUI(){
    return new ChessclubConsolePrefsPanel(this);
  }
  
  
  
  /**
   * Returns a string that should be displayed for the given ChatEvent when the
   * ChatEvent contains a qtell.
   */

  public static String parseQTell(ChatEvent evt){
    String message = evt.getMessage();
    int index;
    while ((index = message.indexOf("\\n")) != -1)
      message = message.substring(0, index) + "\n:" + message.substring(index + 2);
    while ((index = message.indexOf("\\h")) != -1)
      message = message.substring(0, index) + message.substring(index + 2);
    while ((index = message.indexOf("\\H")) != -1)
      message = message.substring(0, index) + message.substring(index + 2);
    while ((index = message.indexOf("\\b")) != -1)
      message = message.substring(0, index) + message.substring(index + 2);
    return ":" + message;
  }
  
  
  
  /**
   * Returns a string that should be displayed for the given ChatEvent when the
   * ChatEvent contains a channel qtell.
   */

  public static String parseChannelQTell(ChatEvent evt){
    String message = evt.getMessage();
    Object forum = evt.getForum();
    int index;
    while ((index = message.indexOf("\\n")) != -1)
      message = message.substring(0, index) + "\n" + forum + ">" + message.substring(index + 2);
    while ((index = message.indexOf("\\h")) != -1)
      message = message.substring(0, index) + message.substring(index + 2);
    while ((index = message.indexOf("\\H")) != -1)
      message = message.substring(0, index) + message.substring(index + 2);
    while ((index = message.indexOf("\\b")) != -1)
      message = message.substring(0, index) + message.substring(index + 2);
    return forum + ">" + message;
  }
  
  
  
}

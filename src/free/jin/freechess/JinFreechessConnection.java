/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
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

package free.jin.freechess;

import free.jin.*;
import free.jin.event.*;
import free.chess.*;
import free.freechess.*;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.SwingUtilities;
import free.jin.event.JinListenerManager;
import free.chess.variants.BothSidesCastlingVariant;
import free.chess.variants.NoCastlingVariant;
import free.chess.variants.fischerrandom.FischerRandom;
import free.chess.variants.suicide.Suicide;
import free.chess.variants.atomic.Atomic;
import free.util.Pair;


/**
 * An implementation of the JinConnection interface for the freechess.org
 * server.
 */

public class JinFreechessConnection extends FreechessConnection implements JinConnection,
    SeekJinConnection, PGNJinConnection{



  /**
   * Our listener manager.
   */

  private final FreechessJinListenerManager listenerManager = new FreechessJinListenerManager(this);





  /**
   * Creates a new JinFreechessConnection with the specified hostname, port,
   * requested username and password.
   */

  public JinFreechessConnection(String hostname, int port, String username, String password){
    super(hostname, port, username, password);

    setInterface(Jin.getInterfaceName());
    setStyle(12);

    setIvarState(Ivar.GAMEINFO, true);
    setIvarState(Ivar.SHOWOWNSEEK, true);
    setIvarState(Ivar.MOVECASE, true);
    setIvarState(Ivar.LOCK, true);
  }




  /**
   * Returns a Player object corresponding to the specified string. If the
   * string is "W", returns <code>Player.WHITE</code>. If it's "B", returns
   * <code>Player.BLACK</code>. Otherwise, throws an IllegalArgumentException.
   */

  public static Player playerForString(String s){
    if (s.equals("B"))
      return Player.BLACK_PLAYER;
    else if (s.equals("W"))
      return Player.WHITE_PLAYER;
    else
      throw new IllegalArgumentException("Bad player string: "+s);
  }





  /**
   * Returns our JinListenerManager.
   */

  public JinListenerManager getJinListenerManager(){
    return getFreechessJinListenerManager();
  }




  /**
   * Returns out JinListenerManager as a reference to FreechessJinListenerManager.
   */

  public FreechessJinListenerManager getFreechessJinListenerManager(){
    return listenerManager;
  }




  /**
   * Overrides createSocket() to fire a ConnectionEvent specifying that the connection
   * was established when super.createSocket() returns (in the Event dispatching
   * thread of course).
   */

  protected java.net.Socket createSocket(String hostname, int port) throws IOException{
    java.net.Socket sock = new free.freechess.timeseal.TimesealingSocket(hostname, port); // Comment this to disable timesealing
//    java.net.Socket sock = new java.net.Socket(hostname, port); // Comment this to enable timesealing

    execRunnable(new Runnable(){

      public void run(){
        listenerManager.fireConnectionEvent(new ConnectionEvent(JinFreechessConnection.this, ConnectionEvent.ESTABLISHED));
      }

    });

    return sock;
  }






  /**
   * Performs various on-login tasks. Also notifies all interested
   * ConnectionListeners that we've successfully logged in.
   */

  public void onLogin(){
    super.onLogin();

    sendCommand("$set bell 0");
    filterLine("Bell off.");

    listenerManager.fireConnectionEvent(new ConnectionEvent(this, ConnectionEvent.LOGGED_IN));
  }




  /**
   * Overrides processDisconnection() to fire a ConnectionEvent specifying that
   * the connection was lost.
   */

  protected void processDisconnection(){
    listenerManager.fireConnectionEvent(new ConnectionEvent(this, ConnectionEvent.LOST));
  }




  /**
   * Notifies any interested PlainTextListener of the received line of otherwise
   * unidentified text.
   */

  protected void processLine(String line){
    listenerManager.firePlainTextEvent(new PlainTextEvent(this, line));
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected boolean processPersonalTell(String username, String titles, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "tell", username,
      (titles == null ? "" : titles), -1, message, null));

    return true;
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected boolean processSayTell(String username, String titles, int gameNumber, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "say", username,
      (titles == null ? "" : titles), -1, message, new Integer(gameNumber)));

    return true;
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected boolean processPTell(String username, String titles, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "ptell", username,
      (titles == null ? "" : titles), -1, message, null));

    return true;
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected boolean processChannelTell(String username, String titles, int channelNumber, 
      String message){

    listenerManager.fireChatEvent(new ChatEvent(this, "channel-tell", username,
      (titles == null ? "" : titles), -1, message, new Integer(channelNumber)));

    return true;
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected boolean processKibitz(String username, String titles, int rating, int gameNumber,
      String message){

    if (titles == null)
      titles = "";

    listenerManager.fireChatEvent(
      new ChatEvent(this, "kibitz", username, titles, rating, message, new Integer(gameNumber)));

    return true;
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected boolean processWhisper(String username, String titles, int rating, int gameNumber,
      String message){
    if (titles == null)
      titles = "";

    listenerManager.fireChatEvent(
      new ChatEvent(this, "whisper", username, titles, rating, message, new Integer(gameNumber)));

    return true;
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected boolean processQTell(String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "qtell", null, null, -1, message, null));

    return true;
  }





  /**
   * Fires an appropriate ChatEvent.
   */

  protected boolean processShout(String username, String titles, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "shout", username,
      (titles == null ? "" : titles), -1, message, null));

    return true;
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected boolean processIShout(String username, String titles, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "ishout", username,
      (titles == null ? "" : titles), -1, message, null));

    return true;
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected boolean processTShout(String username, String titles, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "tshout", username, 
      (titles == null ? "" : titles), -1, message, null));

    return true;
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected boolean processCShout(String username, String titles, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "cshout", username, 
      (titles == null ? "" : titles), -1, message, null));

    return true;
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected boolean processAnnouncement(String username, String message){
    listenerManager.fireChatEvent(
      new ChatEvent(this, "announcement", username, "", -1, message, null));

    return true;
  }




  /**
   * Returns the wild variant corresponding to the given server wild variant 
   * name/category name, or <code>null</code> if that category is not supported. 
   */

  private static WildVariant getVariant(String categoryName){
    if (categoryName.equalsIgnoreCase("lightning") ||
        categoryName.equalsIgnoreCase("blitz") ||
        categoryName.equalsIgnoreCase("standard") ||
        categoryName.equalsIgnoreCase("untimed"))
      return Chess.getInstance();

    
    if (categoryName.startsWith("wild/")){
      String wildId = categoryName.substring("wild/".length());
      if (wildId.equals("0") || wildId.equals("1"))
        return new BothSidesCastlingVariant(Chess.INITIAL_POSITION_FEN, categoryName);
      else if (wildId.equals("2") || wildId.equals("3"))
        return new NoCastlingVariant(Chess.INITIAL_POSITION_FEN, categoryName);
      else if (wildId.equals("5") || wildId.equals("8") || wildId.equals("8a"))
        return new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, categoryName);
      else if (wildId.equals("fr"))
        return FischerRandom.getInstance();
    }
    else if (categoryName.equals("suicide"))
      return Suicide.getInstance();
    else if (categoryName.equals("losers"))
      return new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, categoryName);
    else if (categoryName.equals("atomic"))
      return Atomic.getInstance();

    // This means it's a fake variant we're using because the server hasn't told us the real one.
    else if (categoryName.equals("fake-variant"))
      return Chess.getInstance();

    return null;
  }




  /**
   * A hashtable where we keep game numbers mapped to GameInfoStruct objects
   * of games that haven't started yet.
   */

  private final Hashtable unstartedGamesData = new Hashtable(1);




  /**
   * Maps game numbers to InternalGameData objects of ongoing games.
   */

  private final Hashtable ongoingGamesData = new Hashtable(5);





  /**
   * A Hashtable mapping Game objects to Vectors of moves which were sent for
   * these games but the server didn't tell us yet whether the move is legal
   * or not.
   */

  private final Hashtable unechoedMoves = new Hashtable(1);




  /**
   * A list of game numbers of ongoing games which we can't support for some
   * reason (not a supported variant for example).
   */

  private final Vector unsupportedGames = new Vector();



  /**
   * The user's primary played (by the user) game, -1 if unknown. This is only
   * set when the user is playing more than one game.
   */

  private int primaryPlayedGame = -1;



  /**
   * The user's primary observed game, -1 if unknown. This is only set when
   * the user is observing more than one game.
   */

  private int primaryObservedGame = -1;
  


  /**
   * Returns the game with the specified number.
   * This method (currently) exists solely for the benefit of the arrow/circle
   * script.
   */

  public Game getGame(int gameNumber) throws NoSuchGameException{
    return getGameData(gameNumber).game;
  }



  /**
   * Returns the InternalGameData for the ongoing game with the specified
   * number. Throws a <code>NoSuchGameException</code> if there's no such game.
   */

  private InternalGameData getGameData(int gameNumber) throws NoSuchGameException{
    InternalGameData gameData = (InternalGameData)ongoingGamesData.get(new Integer(gameNumber));
    if (gameData == null)
      throw new NoSuchGameException();

    return gameData;
  }



  /**
   * Finds the (primary) game played by the user. Throws a
   * <code>NoSuchGameException</code> if there's no such game.
   */

  private InternalGameData findMyGame() throws NoSuchGameException{
    if (primaryPlayedGame != -1)
      return getGameData(primaryPlayedGame);

    Enumeration gameNumbers = ongoingGamesData.keys();
    while (gameNumbers.hasMoreElements()){
      Integer gameNumber = (Integer)gameNumbers.nextElement();
      InternalGameData gameData = (InternalGameData)ongoingGamesData.get(gameNumber);
      Game game = gameData.game;
      if (game.getGameType() == Game.MY_GAME)
        return gameData;
    }

    throw new NoSuchGameException();
  }



  /**
   * Finds the played user's game against the specified opponent.
   * Returns the game number of null if no such game exists.
   */

  private InternalGameData findMyGameAgainst(String playerName) throws NoSuchGameException{
    Enumeration gameNumbers = ongoingGamesData.keys();
    while (gameNumbers.hasMoreElements()){
      Integer gameNumber = (Integer)gameNumbers.nextElement();
      InternalGameData gameData = (InternalGameData)ongoingGamesData.get(gameNumber);
      Game game = gameData.game;
      Player userPlayer = game.getUserPlayer();
      if (userPlayer == null) // Not our game or not played
        continue;
      Player oppPlayer = userPlayer.getOpponent();
      if ((oppPlayer.isWhite() && game.getWhiteName().equals(playerName)) ||
          (oppPlayer.isBlack() && game.getBlackName().equals(playerName)))
        return gameData;
    }

    throw new NoSuchGameException();
  }



  /**
   * Saves the GameInfoStruct until we receive enough info to fire a
   * GameStartEvent.
   */

  protected boolean processGameInfo(GameInfoStruct data){
    unstartedGamesData.put(new Integer(data.getGameNumber()), data);

    return true;
  }



  /**
   * Fires an appropriate GameEvent depending on the situation.
   */

  protected boolean processStyle12(Style12Struct boardData){
    Integer gameNumber = new Integer(boardData.getGameNumber());
    InternalGameData gameData = (InternalGameData)ongoingGamesData.get(gameNumber);
    GameInfoStruct unstartedGameInfo = (GameInfoStruct)unstartedGamesData.remove(gameNumber);

    if (unstartedGameInfo != null) // A new game
      gameData = startGame(unstartedGameInfo, boardData);
    else if (gameData != null){ // A known game
      Style12Struct oldBoardData = gameData.boardData;
      int plyDifference = boardData.getPlayedPlyCount() - oldBoardData.getPlayedPlyCount();

      if (plyDifference < 0){
        if ((gameData.getMoveCount() < -plyDifference) || // Can't issue takeback
            gameData.isBSetup)
          changePosition(gameData, boardData);
        else
          issueTakeback(gameData, boardData);
      }
      else if (plyDifference == 0){
        if (gameData.isBSetup)
          changePosition(gameData, boardData);
        // This happens if you:
        // 1. Issue "refresh".
        // 2. Make an illegal move, because the server will re-send us the board
        //    (although we don't need it)
        // 3. Issue board setup commands.
      }
      else if (plyDifference == 1){
        if (boardData.getMoveVerbose() != null)
          makeMove(gameData, boardData);
        else
          changePosition(gameData, boardData); 
          // This shouldn't happen, but I'll leave it just in case
      }
      else if (plyDifference > 1){
        changePosition(gameData, boardData);
        // This happens if you:
        // 1. Issue "forward" with an argument of 2 or bigger.
      }
    }
    else if (!unsupportedGames.contains(gameNumber)){ 
      // Grr, the server started a game without sending us a GameInfo line.
      // Currently happens if you start examining a game (26.08.2002)

      // We have no choice but to fake the data, since the server simply doesn't
      // send us this information.
      GameInfoStruct fakeGameInfo = new GameInfoStruct(boardData.getGameNumber(),
        false, "fake-variant", false, false, false, boardData.getInitialTime(),
        boardData.getIncrement(), boardData.getInitialTime(), boardData.getIncrement(),
        0, -1, ' ', -1, ' ', false, false);

      gameData = startGame(fakeGameInfo, boardData);
    }

    if (gameData != null){
      updateClocks(gameData, boardData);

      Style12Struct oldBoardData = gameData.boardData;

      if ((oldBoardData != null) && (oldBoardData.isBoardFlipped() != boardData.isBoardFlipped()))
        flipBoard(gameData, boardData);

      gameData.boardData = boardData;
    }

    return true;
  }



  /**
   * Changes the bsetup state of the game.
   */

  protected boolean processBSetupMode(boolean entered){
    try{
      findMyGame().isBSetup = entered;
    } catch (NoSuchGameException e){}

    return super.processBSetupMode(entered);
  }




  /**
   * A small class for keeping internal data about a game.
   */

  private static class InternalGameData{


    /**
     * The Game object representing the game.
     */

    public final Game game;



    /**
     * A list of Moves done in the game.
     */

    public Vector moveList = new Vector();



    /**
     * The last Style12Struct we got for this game.
     */

    public Style12Struct boardData = null;



    /**
     * Is this game in bsetup mode?
     */

    public boolean isBSetup = false;



    /**
     * Works as a set of the offers currently in this game. The elements are
     * Pairs in which the first item is the player who made the offer and the
     * second one is the offer id. Takeback offers are kept separately.
     */

    private final Hashtable offers = new Hashtable();



    /**
     * The number of plies the white player offerred to takeback.
     */

    private int whiteTakeback;



    /**
     * The number of plies the black player offerred to takeback.
     */

    private int blackTakeback;



    /**
     * Creates a new InternalGameData.
     */

    public InternalGameData(Game game){
      this.game = game;
    }



    /**
     * Returns the amount of moves made in the game (as far as we counted).
     */

    public int getMoveCount(){
      return moveList.size();
    }


 
    /**
     * Adds the specified move to the moves list.
     */

    public void addMove(Move move){
      moveList.addElement(move);
    }



    /**
     * Removes the last <code>count</code> moves from the movelist, if possible.
     * Otherwise, throws an <code>IllegalArgumentException</code>.
     */

    public void removeLastMoves(int count){
      if (count > moveList.size())
        throw new IllegalArgumentException("Can't remove more elements than there are elements");

      int first = moveList.size() - 1;
      int last = moveList.size() - count;
      for (int i = first; i >= last; i--)
        moveList.removeElementAt(i);
    }



    /**
     * Removes all the moves made in the game.
     */

    public void clearMoves(){
      moveList.removeAllElements();
    }



    /**
     * Returns true if the specified offer is currently made by the specified
     * player in this game.
     */

    public boolean isOffered(int offerId, Player player){
      return offers.containsKey(new Pair(player, new Integer(offerId)));
    }



    /**
     * Sets the state of the specified offer in the game. Takeback offers are
     * handled by the setTakebackCount method.
     */

    public void setOffer(int offerId, Player player, boolean isMade){
      Pair offer = new Pair(player, new Integer(offerId));
      if (isMade) 
        offers.put(offer, offer);
      else
        offers.remove(offer);
    }



    /**
     * Sets the takeback offer in the game to the specified amount of plies.
     */

    public void setTakebackOffer(Player player, int plies){
      if (player.isWhite())
        whiteTakeback = plies;
      else
        blackTakeback = plies;
    }



    /**
     * Returns the amount of plies offered to take back by the specified player.
     */

    public int getTakebackOffer(Player player){
      if (player.isWhite())
        return whiteTakeback;
      else
        return blackTakeback;
    }


  }



  /**
   * Changes the primary played game.
   */

  protected boolean processSimulCurrentBoardChanged(int gameNumber, String oppName){
    primaryPlayedGame = gameNumber;

    return true;
  }



  /**
   * Changes the primary observed game.
   */

  protected boolean processPrimaryGameChanged(int gameNumber){
    primaryObservedGame = gameNumber;

    return true;
  }





  /**
   * Invokes <code>closeGame(int)</code>.
   */

  protected boolean processGameEnd(int gameNumber, String whiteName, String blackName,
      String reason, String result){

    int resultCode;
    if ("1-0".equals(result))
      resultCode = Game.WHITE_WINS;
    else if ("0-1".equals(result))
      resultCode = Game.BLACK_WINS;
    else if ("1/2-1/2".equals(result))
      resultCode = Game.DRAW;
    else
      resultCode = Game.UNKNOWN_RESULT;

    closeGame(gameNumber, resultCode);

    return false;
  }




  /**
   * Invokes <code>closeGame(int)</code>.
   */

  protected boolean processStoppedObserving(int gameNumber){
    closeGame(gameNumber, Game.UNKNOWN_RESULT);

    return false;
  }




  /**
   * Invokes <code>closeGame(int)</code>.
   */

  protected boolean processStoppedExamining(int gameNumber){
    closeGame(gameNumber, Game.UNKNOWN_RESULT);

    return false;
  }




  /**
   * Invokes <code>illegalMoveAttempted</code>.
   */

  protected boolean processIllegalMove(String moveString, String reason){
    illegalMoveAttempted(moveString);

    return false;
  }






  /**
   * Called when a new game is starting. Responsible for creating the game on
   * the client side and firing appropriate events. Returns an InternalGameData
   * instance for the newly created Game.
   */

  private InternalGameData startGame(GameInfoStruct gameInfo, Style12Struct boardData){
    String categoryName = gameInfo.getGameCategory();
    WildVariant variant = getVariant(categoryName);
    if (variant == null){
      processLine("This version of Jin does not support the wild variant ("+categoryName+") and is thus unable to display the game.");
      processLine("Please activate the appropriate command to abort this game");
      unsupportedGames.addElement(new Integer(gameInfo.getGameNumber()));
      return null;
    }

    int gameType;
    switch (boardData.getGameType()){
      case Style12Struct.MY_GAME: gameType = Game.MY_GAME; break;
      case Style12Struct.OBSERVED_GAME: gameType = Game.OBSERVED_GAME; break;
      case Style12Struct.ISOLATED_BOARD: gameType = Game.ISOLATED_BOARD; break;
      default:
        throw new IllegalArgumentException("Bad game type value: "+boardData.getGameType());
    }

    Position initPos = new Position(variant);
    initPos.setFEN(boardData.getBoardFEN());
    Player currentPlayer = playerForString(boardData.getCurrentPlayer());
    initPos.setCurrentPlayer(currentPlayer);

    String whiteName = boardData.getWhiteName();
    String blackName = boardData.getBlackName();

    int whiteTime = 1000 * gameInfo.getWhiteTime();
    int blackTime = 1000 * gameInfo.getBlackTime();
    int whiteInc = 1000 * gameInfo.getWhiteInc();
    int blackInc = 1000 * gameInfo.getBlackInc();

    int whiteRating = gameInfo.isWhiteRegistered() ? -1 : gameInfo.getWhiteRating();
    int blackRating = gameInfo.isBlackRegistered() ? -1 : gameInfo.getBlackRating();

    String gameID = String.valueOf(gameInfo.getGameNumber());

    boolean isRated = gameInfo.isGameRated();

    boolean isPlayed = boardData.isPlayedGame();

    String whiteTitles = "";
    String blackTitles = "";

    boolean initiallyFlipped = boardData.isBoardFlipped();

    Player userPlayer = null;
    if ((gameType == Game.MY_GAME) && isPlayed)
      userPlayer = boardData.isMyTurn() ? currentPlayer : currentPlayer.getOpponent();

    Game game = new Game(gameType, initPos, boardData.getPlayedPlyCount(), whiteName, blackName,
      whiteTime, whiteInc, blackTime, blackInc, whiteRating, blackRating, gameID, categoryName,
      isRated, isPlayed, whiteTitles, blackTitles, initiallyFlipped, userPlayer);

    InternalGameData gameData = new InternalGameData(game);

    ongoingGamesData.put(new Integer(gameInfo.getGameNumber()), gameData);

    listenerManager.fireGameEvent(new GameStartEvent(this, game));

    // The server doesn't send us seek remove lines during games, so we have
    // no choice but to remove *all* seeks during a game. The seeks are restored
    // when a game ends by setting seekinfo to 1 again.
    if (gameType == Game.MY_GAME)
      removeAllSeeks(); 

    return gameData;
  }




  /**
   * Gets called when a move is made. Fires an appropriate MoveMadeEvent.
   */

  private void makeMove(InternalGameData gameData, Style12Struct boardData){
    Game game = gameData.game;
    Style12Struct oldBoardData = gameData.boardData;

    String moveVerbose = boardData.getMoveVerbose();
    String moveSAN = boardData.getMoveSAN();

    WildVariant variant = game.getVariant();
    Position position = new Position(variant);
    position.setLexigraphic(oldBoardData.getBoardLexigraphic());
    Player currentPlayer = playerForString(oldBoardData.getCurrentPlayer());
    position.setCurrentPlayer(currentPlayer);

    Move move;
    Square fromSquare, toSquare;
    Piece promotionPiece = null;
    if (moveVerbose.equals("o-o"))
      move = variant.createShortCastling(position);
    else if (moveVerbose.equals("o-o-o"))
      move = variant.createLongCastling(position);
    else{
      fromSquare = Square.parseSquare(moveVerbose.substring(2, 4));
      toSquare = Square.parseSquare(moveVerbose.substring(5, 7));
      int promotionCharIndex = moveVerbose.indexOf("=")+1;
      if (promotionCharIndex != 0){
        String pieceString = moveVerbose.substring(promotionCharIndex, promotionCharIndex + 1);
        if (currentPlayer.isBlack()) // The server always sends upper case characters, even for black pieces.
          pieceString = pieceString.toLowerCase(); 
        promotionPiece = variant.parsePiece(pieceString);
      }

      move = variant.createMove(position, fromSquare, toSquare, promotionPiece, moveSAN);
    }

    listenerManager.fireGameEvent(new MoveMadeEvent(this, game, move, true)); 
      // (isNew == true) because FICS never sends the entire move history

    Vector unechoedGameMoves = (Vector)unechoedMoves.get(game);
    if ((unechoedGameMoves != null) && (unechoedGameMoves.size() != 0)){ // Looks like it's our move.
      Move madeMove = (Move)unechoedGameMoves.elementAt(0);
      if (moveToString(game, move).equals(moveToString(game, madeMove))) // Same move.
        unechoedGameMoves.removeElementAt(0); 
    }

    gameData.addMove(move);
  }




  /**
   * Fires an appropriate ClockAdjustmentEvent.
   */

  private void updateClocks(InternalGameData gameData, Style12Struct boardData){
    Game game = gameData.game;

    int whiteTime = boardData.getWhiteTime();
    int blackTime = boardData.getBlackTime();

    Player currentPlayer = playerForString(boardData.getCurrentPlayer());

    boolean whiteRunning = boardData.isClockRunning() && currentPlayer.isWhite();
    boolean blackRunning = boardData.isClockRunning() && currentPlayer.isBlack();

    listenerManager.fireGameEvent(new ClockAdjustmentEvent(this, game, Player.WHITE_PLAYER, whiteTime, whiteRunning));
    listenerManager.fireGameEvent(new ClockAdjustmentEvent(this, game, Player.BLACK_PLAYER, blackTime, blackRunning));
  }




  /**
   * Fires an appropriate GameEndEvent.
   */

  private void closeGame(int gameNumber, int result){
    Integer gameID = new Integer(gameNumber);

    if (gameID.intValue() == primaryPlayedGame)
      primaryPlayedGame = -1;
    else if (gameID.intValue() == primaryObservedGame)
      primaryObservedGame = -1;

    InternalGameData gameData = (InternalGameData)ongoingGamesData.remove(gameID);
    if (gameData != null){
      Game game = gameData.game;

      game.setResult(result);
      listenerManager.fireGameEvent(new GameEndEvent(this, game, result));

      if (game.getGameType() == Game.MY_GAME)
        setIvarState(Ivar.SEEKINFO, true); // Refresh the seeks
    }
    else
      unsupportedGames.removeElement(gameID);
  }



  /**
   * Fires an appropriate BoardFlipEvent.
   */

  private void flipBoard(InternalGameData gameData, Style12Struct newBoardData){
    listenerManager.fireGameEvent(new BoardFlipEvent(this, gameData.game, newBoardData.isBoardFlipped()));
  }




  /**
   * Fires an appropriate IllegalMoveEvent.
   */

  private void illegalMoveAttempted(String moveString){
    try{
      InternalGameData gameData = findMyGame(); 
      Game game = gameData.game;

      Vector unechoedGameMoves = (Vector)unechoedMoves.get(game);

      // Not a move we made (probably the user typed it in)
      if ((unechoedGameMoves == null) || (unechoedGameMoves.size() == 0)) 
        return;


      Move move = (Move)unechoedGameMoves.elementAt(0);

      // We have no choice but to allow (moveString == null) because the server
      // doesn't always send us the move string (for example if it's not our turn).
      if ((moveString == null) || moveToString(game, move).equals(moveString)){
        // Our move, probably

        unechoedGameMoves.removeAllElements();
        listenerManager.fireGameEvent(new IllegalMoveEvent(this, game, move));
      }
    } catch (NoSuchGameException e){}
  }




  /**
   * Fires an appropriate TakebackEvent.
   */

  private void issueTakeback(InternalGameData gameData, Style12Struct newBoardData){
    Style12Struct oldBoardData = gameData.boardData;
    int takebackCount = oldBoardData.getPlayedPlyCount() - newBoardData.getPlayedPlyCount();

    listenerManager.fireGameEvent(new TakebackEvent(this, gameData.game, takebackCount));

    gameData.removeLastMoves(takebackCount);
  }




  /**
   * Fires an appropriate PositionChangedEvent.
   */

  private void changePosition(InternalGameData gameData, Style12Struct newBoardData){
    Game game = gameData.game;

    Position newPos = game.getInitialPosition();
    newPos.setFEN(newBoardData.getBoardFEN());
    Player currentPlayer = playerForString(newBoardData.getCurrentPlayer());
    newPos.setCurrentPlayer(currentPlayer);

    game.setInitialPosition(newPos, newBoardData.getPlayedPlyCount());

    listenerManager.fireGameEvent(new PositionChangedEvent(this, game, newPos));

    gameData.clearMoves();

    // We do this because moves in bsetup mode cause position change events, not move events
    if (gameData.isBSetup){
      Vector unechoedGameMoves = (Vector)unechoedMoves.get(game);
      if ((unechoedGameMoves != null) && (unechoedGameMoves.size() != 0))
        unechoedGameMoves.removeElementAt(0); 
    }
  }



  /**
   * Maps seek IDs to Seek objects currently in the sought list.
   */

  private final Hashtable seeks = new Hashtable();




  /**
   * Returns the SeekJinListenerManager via which you can register and
   * unregister SeekListeners.
   */

  public SeekJinListenerManager getSeekJinListenerManager(){
    return getFreechessJinListenerManager();
  }




  /**
   * Creates an appropriate Seek object and fires a SeekEvent.
   */

  protected boolean processSeekAdded(SeekInfoStruct seekInfo){
    // We may get seeks after setting seekinfo to false because the server
    // already sent them when we sent it the request to set seekInfo to false.
    if (getRequestedIvarState(Ivar.SEEKINFO)){
      WildVariant variant = getVariant(seekInfo.getMatchType());
      if (variant != null){
        String seekID = String.valueOf(seekInfo.getSeekIndex());
        StringBuffer titlesBuf = new StringBuffer();
        int titles = seekInfo.getSeekerTitles();

        if ((titles & SeekInfoStruct.COMPUTER) != 0)
          titlesBuf.append("(C)");
        if ((titles & SeekInfoStruct.GM) != 0)
          titlesBuf.append("(GM)");
        if ((titles & SeekInfoStruct.IM) != 0)
          titlesBuf.append("(IM)");
        if ((titles & SeekInfoStruct.FM) != 0)
          titlesBuf.append("(FM)");
        if ((titles & SeekInfoStruct.WGM) != 0)
          titlesBuf.append("(WGM)");
        if ((titles & SeekInfoStruct.WIM) != 0)
          titlesBuf.append("(WIM)");
        if ((titles & SeekInfoStruct.WFM) != 0)
          titlesBuf.append("(WFM)");

        boolean isProvisional = (seekInfo.getSeekerProvShow() == 'P');

        boolean isSeekerRated = (seekInfo.getSeekerRating() != 0);

        boolean isRegistered = ((seekInfo.getSeekerTitles() & SeekInfoStruct.UNREGISTERED) == 0);

        boolean isComputer = ((seekInfo.getSeekerTitles() & SeekInfoStruct.COMPUTER) != 0);

        Player color;
        switch (seekInfo.getSeekerColor()){
          case 'W':
            color = Player.WHITE_PLAYER;
            break;
          case 'B':
            color = Player.BLACK_PLAYER;
            break;
          case '?':
            color = null;
            break;
          default:
            throw new IllegalStateException("Bad desired color char: "+seekInfo.getSeekerColor());
        }

        boolean isRatingLimited = ((seekInfo.getOpponentMinRating() > 0) || (seekInfo.getOpponentMaxRating() < 9999));

        Seek seek = new Seek(seekID, seekInfo.getSeekerHandle(), titlesBuf.toString(), seekInfo.getSeekerRating(),
          isProvisional, isRegistered, isSeekerRated, isComputer, variant, seekInfo.getMatchType(),
          seekInfo.getMatchTime()*60*1000, seekInfo.getMatchIncrement()*1000, seekInfo.isMatchRated(), color,
          isRatingLimited, seekInfo.getOpponentMinRating(), seekInfo.getOpponentMaxRating(),
          !seekInfo.isAutomaticAccept(), seekInfo.isFormulaUsed());

        Integer seekIndex = new Integer(seekInfo.getSeekIndex());

        Seek oldSeek = (Seek)seeks.get(seekIndex);
        if (oldSeek != null)
          listenerManager.fireSeekEvent(new SeekEvent(this, SeekEvent.SEEK_REMOVED, oldSeek));

        seeks.put(seekIndex, seek);
        listenerManager.fireSeekEvent(new SeekEvent(this, SeekEvent.SEEK_ADDED, seek));
      }
    }
    
    return true;
  }




  /**
   * Issues the appropriate SeekEvents and removes the seeks.
   */

  protected boolean processSeeksRemoved(int [] removedSeeks){
    for (int i = 0; i < removedSeeks.length; i++){
      Integer seekIndex = new Integer(removedSeeks[i]);
      Seek seek = (Seek)seeks.get(seekIndex);
      if (seek == null) // Happens if the seek is one we didn't fire an event for,
        continue;       // for example if we don't support the variant.

      listenerManager.fireSeekEvent(new SeekEvent(this, SeekEvent.SEEK_REMOVED, seek));

      seeks.remove(seekIndex);
    }
    
    return true;
  }




  /**
   * Issues the appropriate SeeksEvents and removes the seeks.
   */

  protected boolean processSeeksCleared(){
    removeAllSeeks();
    return true;
  }




  /**
   * Removes all the seeks and notifies the listeners.
   */

  private void removeAllSeeks(){
    int seeksCount = seeks.size();
    if (seeksCount != 0){
      Object [] seeksIndices = new Object[seeksCount];

      // Copy all the keys into a temporary array
      Enumeration seekIDsEnum = seeks.keys();
      for (int i = 0; i < seeksCount; i++)
        seeksIndices[i] = seekIDsEnum.nextElement();

      // Remove all the seeks one by one, notifying any interested listeners.
      for (int i = 0; i < seeksCount; i++){
        Object seekIndex = seeksIndices[i];
        Seek seek = (Seek)seeks.get(seekIndex);
        listenerManager.fireSeekEvent(new SeekEvent(this, SeekEvent.SEEK_REMOVED, seek));
        seeks.remove(seekIndex);
      }
    }
  }




  /**
   * This method is called by our FreechessJinListenerManager when a new
   * SeekListener is added and we already had registered listeners (meaning that
   * iv_seekinfo was already on, so we need to notify the new listeners of all
   * existing seeks as well).
   */

  void notFirstListenerAdded(SeekListener listener){
    Enumeration seeksEnum = seeks.elements();
    while (seeksEnum.hasMoreElements()){
      Seek seek = (Seek)seeksEnum.nextElement();
      SeekEvent evt = new SeekEvent(this, SeekEvent.SEEK_ADDED, seek);
      listener.seekAdded(evt);
    }
  }




  /**
   * This method is called by our ChessclubJinListenerManager when the last
   * SeekListener is removed.
   */

  void lastSeekListenerRemoved(){
    seeks.clear();
  }



  /**
   * Fires the appropriate OfferEvent.
   */

  protected boolean processOppOffered(String oppName, String offerName){
    try{
      InternalGameData gameData = findMyGameAgainst(oppName);

      updateOffers(gameData, offerName, gameData.game.getUserPlayer().getOpponent(), true);
    } catch (NoSuchGameException e){}

    return super.processOppOffered(offerName, oppName);
  }




  /**
   * Fires the appropriate OfferEvent(s).
   */

  protected boolean processOppOfferedTakeback(String oppName, int takebackCount){
    try{
      InternalGameData gameData = findMyGameAgainst(oppName);

      Player oppPlayer = gameData.game.getUserPlayer().getOpponent();
      // getUserPlayer shouldn't return null here because we've obtained the game
      // via findMyGameAgainst which should only return user played games.

      updateTakebackOffer(gameData, oppPlayer, takebackCount);
    } catch (NoSuchGameException e){}
    
    return super.processOppOfferedTakeback(oppName, takebackCount);
  }



  /**
   * Fires the appropriate OfferEvent(s).
   */

  protected boolean processTakebackOfferUpdated(int gameNum, int takebackCount){
    try{
      InternalGameData gameData = getGameData(gameNum);
      Player player;
      if (gameData.getTakebackOffer(Player.WHITE_PLAYER) != 0)
        player = Player.WHITE_PLAYER;
      else
        player = Player.BLACK_PLAYER;

      updateTakebackOffer(gameData, player, takebackCount);
    } catch (NoSuchGameException e){}

    return super.processTakebackOfferUpdated(gameNum, takebackCount);
  }



  /**
   * Fires the appropriate OfferEvent(s).
   */

  protected boolean processOppCounteredTakebackOffer(String oppName, int takebackCount){
    try{
      InternalGameData gameData = findMyGameAgainst(oppName);

      Player userPlayer = gameData.game.getUserPlayer(); 
      // getUserPlayer shouldn't return null here because we've obtained the game
      // via findMyGameAgainst which should only return user played games.

      updateTakebackOffer(gameData, userPlayer, 0);
      updateTakebackOffer(gameData, userPlayer.getOpponent(), takebackCount);
    } catch (NoSuchGameException e){}

    return super.processOppCounteredTakebackOffer(oppName, takebackCount);
  }



  /**
   * Fires the appropriate OfferEvent(s).
   */

  protected boolean processPlayerCounteredTakebackOffer(int gameNum, String playerName,
      int takebackCount){
    try{
      InternalGameData gameData = getGameData(gameNum);
      Player player = gameData.game.getPlayerNamed(playerName);

      updateTakebackOffer(gameData, player.getOpponent(), 0);
      updateTakebackOffer(gameData, player, takebackCount);
    } catch (NoSuchGameException e){}

    return super.processPlayerCounteredTakebackOffer(gameNum, playerName, takebackCount);
  }



  /**
   * Fires the appropriate OfferEvent(s).
   */

  protected boolean processUserOffered(String offerName){
    try{
      InternalGameData gameData = findMyGame();

      updateOffers(gameData, offerName, gameData.game.getUserPlayer(), true);
    } catch (NoSuchGameException e){}

    return super.processUserOffered(offerName);
  }




  /**
   * Fires the appropriate OfferEvent(s).
   */

  protected boolean processOppDeclined(String oppName, String offerName){
    try{
      InternalGameData gameData = findMyGameAgainst(oppName);
      
      updateOffers(gameData, offerName, gameData.game.getUserPlayer(), false);
    } catch (NoSuchGameException e){}

    return super.processOppDeclined(offerName, oppName);
  }



  /**
   * Fires the appropriate OfferEvent(s).
   */

  protected boolean processUserDeclined(String oppName, String offerName){
    try{
      InternalGameData gameData = findMyGameAgainst(oppName);
      
      updateOffers(gameData, offerName, gameData.game.getUserPlayer().getOpponent(), false);
    } catch (NoSuchGameException e){}

    return super.processUserDeclined(oppName, offerName);
  }



  /**
   * Fires the appropriate OfferEvent(s).
   */

  protected boolean processOppWithdrew(String oppName, String offerName){
    try{
      InternalGameData gameData = findMyGameAgainst(oppName);

      updateOffers(gameData, offerName, gameData.game.getUserPlayer().getOpponent(), false);
    } catch (NoSuchGameException e){}

    return super.processOppWithdrew(oppName, offerName);
  }



  /**
   * Fires the appropriate OfferEvent(s).
   */

  protected boolean processUserWithdrew(String oppName, String offerName){
    try{
      InternalGameData gameData = findMyGameAgainst(oppName);

      updateOffers(gameData, offerName, gameData.game.getUserPlayer(), false);
    } catch (NoSuchGameException e){}
    
    return super.processUserWithdrew(oppName, offerName);
  }


  /**
   * Fires the appropriate OfferEvent(s).
   */

  protected boolean processPlayerOffered(int gameNum, String playerName, String offerName){
    try{
      InternalGameData gameData = getGameData(gameNum);
      Player player = gameData.game.getPlayerNamed(playerName);

      updateOffers(gameData, offerName, player, true);
    } catch (NoSuchGameException e){}

    return super.processPlayerOffered(gameNum, playerName, offerName);
  }



  /**
   * Fires the appropriate OfferEvent(s).
   */

  protected boolean processPlayerDeclined(int gameNum, String playerName, String offerName){
    try{
      InternalGameData gameData = getGameData(gameNum);
      Player player = gameData.game.getPlayerNamed(playerName);

      updateOffers(gameData, offerName, player.getOpponent(), false);
    } catch (NoSuchGameException e){}

    return super.processPlayerDeclined(gameNum, playerName, offerName);
  }



  /**
   * Fires the appropriate OfferEvent(s).
   */

  protected boolean processPlayerWithdrew(int gameNum, String playerName, String offerName){
    try{
      InternalGameData gameData = getGameData(gameNum);
      Player player = gameData.game.getPlayerNamed(playerName);

      updateOffers(gameData, offerName, player, false);
    } catch (NoSuchGameException e){}

    return super.processPlayerWithdrew(gameNum, playerName, offerName);
  }



  /**
   * Fires the appropriate OfferEvent(s).
   */

  protected boolean processPlayerOfferedTakeback(int gameNum, String playerName, int takebackCount){
    try{
      InternalGameData gameData = getGameData(gameNum);
      Player player = gameData.game.getPlayerNamed(playerName);

      updateTakebackOffer(gameData, player, takebackCount);
    } catch (NoSuchGameException e){}

    return super.processPlayerOfferedTakeback(gameNum, playerName, takebackCount);
  }



  /**
   * Updates the specified offer, firing any necessary events.
   */

  private void updateOffers(InternalGameData gameData, String offerName, Player player, boolean on){
    int offerId;
    try{
      offerId = offerIdForOfferName(offerName);
    } catch (IllegalArgumentException e){return;}

    Game game = gameData.game;

    if (offerId == OfferEvent.TAKEBACK_OFFER){
      // We're forced to fake this so that an event is fired even if we start observing a game
      // with an existing takeback offer (of which we're not aware).
      if ((!on) && (gameData.getTakebackOffer(player) == 0))
        gameData.setTakebackOffer(player, 1);

      updateTakebackOffer(gameData, player.getOpponent(), 0); // Remove any existing offers
      updateTakebackOffer(gameData, player, on ? 1 : 0);
      // 1 as the server doesn't tell us how many
    }
    else{// if (gameData.isOffered(offerId, player) != on){ this
         // We check this because we might get such an event if we start observing a game with
         // an existing offer.

      gameData.setOffer(offerId, player, on);
      listenerManager.fireGameEvent(new OfferEvent(this, game, offerId, on, player));
    }
  }



  /**
   * Returns the offerId (as defined by OfferEvent) corresponding to the
   * specified offer name. Throws an IllegalArgumentException if the offer name
   * is not recognizes.
   */

  private static int offerIdForOfferName(String offerName) throws IllegalArgumentException{
    if ("draw".equals(offerName))
      return OfferEvent.DRAW_OFFER;
    else if ("abort".equals(offerName))
      return OfferEvent.ABORT_OFFER;
    else if ("adjourn".equals(offerName))
      return OfferEvent.ADJOURN_OFFER;
    else if ("takeback".equals(offerName))
      return OfferEvent.TAKEBACK_OFFER;
    else
      throw new IllegalArgumentException("Unknown offer name: "+offerName);
  }




  /**
   * Updates the takeback offer in the specified game to the specified amount of
   * plies.
   */

  private void updateTakebackOffer(InternalGameData gameData, Player player, int takebackCount){
    Game game = gameData.game;

    int oldTakeback = gameData.getTakebackOffer(player);
    if (oldTakeback != 0)
      listenerManager.fireGameEvent(new OfferEvent(this, game, false, player, oldTakeback));

    gameData.setTakebackOffer(player, takebackCount);

    if (takebackCount != 0)
      listenerManager.fireGameEvent(new OfferEvent(this, game, true, player, takebackCount));
  }




  /**
   * Accepts the given seek. Note that the given seek must be an instance generated
   * by this SeekJinConnection and it must be in the current sought list.
   */

  public void acceptSeek(Seek seek){
    if (!seeks.contains(seek))
      throw new IllegalArgumentException("The specified seek is not on the seek list");

    sendCommand("$play "+seek.getID());
  }



  /**
   * Sends the "exit" command to the server.
   */

  public void exit(){
    sendCommand("$quit");
  }




  /**
   * Quits the specified game.
   */

  public void quitGame(Game game){
    Object id = game.getID();
    switch (game.getGameType()){
      case Game.MY_GAME:
        if (game.isPlayed())
          sendCommand("$resign");
        else
          sendCommand("$unexamine");
        break;
      case Game.OBSERVED_GAME:
        sendCommand("$unobserve "+id);
        break;
      case Game.ISOLATED_BOARD:
        break;
    }
  }




  /**
   * Makes the given move in the given game.
   */

  public void makeMove(Game game, Move move){
    Enumeration gamesDataEnum = ongoingGamesData.elements();
    boolean ourGame = false;
    while (gamesDataEnum.hasMoreElements()){
      InternalGameData gameData = (InternalGameData)gamesDataEnum.nextElement();
      if (gameData.game == game){
        ourGame = true;
        break;
      }
    }

    if (!ourGame)
      throw new IllegalArgumentException("The specified Game object was not created by this JinConnection or the game has ended.");

    sendCommand(moveToString(game, move));

    Vector unechoedGameMoves = (Vector)unechoedMoves.get(game);
    if (unechoedGameMoves == null){
      unechoedGameMoves = new Vector(2);
      unechoedMoves.put(game, unechoedGameMoves);
    }
    unechoedGameMoves.addElement(move);
  }




  /**
   * Converts the given move into a string we can send to the server.
   */

  private static String moveToString(Game game, Move move){
    WildVariant variant = game.getVariant();
    if (move instanceof ChessMove){
      ChessMove cmove = (ChessMove)move;
      if (cmove.isShortCastling())
        return "O-O";
      else if (cmove.isLongCastling())
        return "O-O-O";

      String s = cmove.getStartingSquare().toString() + cmove.getEndingSquare().toString();
      if (cmove.isPromotion())
        return s + "=" + variant.pieceToString(cmove.getPromotionTarget());
      else
        return s;
    }
    else
      throw new IllegalArgumentException("Unsupported Move type: "+move.getClass());
  }





  /**
   * Resigns the given game. The given game must be a played game and of type
   * Game.MY_GAME.
   */

  public void resign(Game game){
    checkGameMineAndPlayed(game);

    sendCommand("$resign");
  }



  /**
   * Sends a request to draw the given game. The given game must be a played 
   * game and of type Game.MY_GAME.
   */

  public void requestDraw(Game game){
    checkGameMineAndPlayed(game);

    sendCommand("$draw");
  }




  /**
   * Returns <code>true</code>.
   */

  public boolean isAbortSupported(){
    return true;
  }



  /**
   * Sends a request to abort the given game. The given game must be a played 
   * game and of type Game.MY_GAME.
   */

  public void requestAbort(Game game){
    checkGameMineAndPlayed(game);

    sendCommand("$abort");
  }



  /**
   * Returns <code>true</code>.
   */

  public boolean isAdjournSupported(){
    return true;
  }



  /**
   * Sends a request to adjourn the given game. The given game must be a played
   * game and of type Game.MY_GAME.
   */

  public void requestAdjourn(Game game){
    checkGameMineAndPlayed(game);

    sendCommand("$adjourn");
  }



  /**
   * Goes back the given amount of plies in the given game. If the given amount
   * of plies is bigger than the amount of plies since the beginning of the game,
   * goes to the beginning of the game.
   */

  public void goBackward(Game game, int plyCount){
    checkGameMineAndExamined(game);

    sendCommand("$backward "+plyCount);
  }




  /**
   * Goes forward the given amount of plies in the given game. If the given amount
   * of plies is bigger than the amount of plies remaining until the end of the
   * game, goes to the end of the game.
   */

  public void goForward(Game game, int plyCount){
    checkGameMineAndExamined(game);

    sendCommand("$forward "+plyCount);
  }




  /**
   * Goes to the beginning of the given game.
   */

  public void goToBeginning(Game game){
    checkGameMineAndExamined(game);

    sendCommand("$backward 999");
  }



  /**
   * Goes to the end of the given game.
   */

  public void goToEnd(Game game){
    checkGameMineAndExamined(game);

    sendCommand("$forward 999");
  }



  /**
   * Throws an IllegalArgumentException if the given Game is not of type 
   * Game.MY_GAME or is not a played game. Otherwise, simply returns.
   */

  private void checkGameMineAndPlayed(Game game){
    if ((game.getGameType() != Game.MY_GAME) || (!game.isPlayed()))
      throw new IllegalArgumentException("The given game must be of type Game.MY_GAME and a played one");
  }




  /**
   * Throws an IllegalArgumentException if the given Game is not of type 
   * Game.MY_GAME or is a played game. Otherwise, simply returns.
   */

  private void checkGameMineAndExamined(Game game){
    if ((game.getGameType() != Game.MY_GAME)||game.isPlayed())
      throw new IllegalArgumentException("The given game must be of type Game.MY_GAME and an examined one");
  }




  /**
   * Overrides ChessclubConnection.execRunnable(Runnable) to execute the
   * runnable on the AWT thread using SwingUtilities.invokeLater(Runnable), 
   * since this class is meant to be used by Jin, a graphical interface using 
   * Swing.
   *
   * @see ChessclubConnection#execRunnable(Runnable)
   * @see SwingUtilities.invokeLater(Runnable)
   */

  public void execRunnable(Runnable runnable){
    SwingUtilities.invokeLater(runnable);
  }


}

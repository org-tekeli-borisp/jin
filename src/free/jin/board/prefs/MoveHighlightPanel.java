/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2004 Alexander Maryanovsky.
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

package free.jin.board.prefs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import free.chess.*;
import free.chess.event.MoveEvent;
import free.chess.event.MoveListener;
import free.jin.BadChangesException;
import free.jin.I18n;
import free.jin.board.BoardManager;
import free.jin.board.JinBoard;
import free.util.swing.ColorChooser;
import free.util.swing.PreferredSizedPanel;


/**
 * A preferences panel allowing the user to select his move highlighting
 * preferences.
 */
 
public class MoveHighlightPanel extends BoardModifyingPrefsPanel{
  
  
  
  /**
   * The radio button for no move highlighting.
   */
   
  private final JRadioButton none;
  
  
  
  /**
   * The radio button for target square move highlighting.
   */
   
  private final JRadioButton targetSquare;
  
  
  
  /**
   * The radio button for both squares move highlighting.
   */
   
  private final JRadioButton bothSquares;
  
  
  
  /**
   * The radio button for arrow move highlighting.
   */
   
  private final JRadioButton arrow;
  
  
  
  /**
   * The checkbox for whether the user's moves should be highlighted.
   */
   
  private final JCheckBox highlightOwnMoves;
  
  
  
  /**
   * The color chooser for the move highlighting color.
   */
   
  private final ColorChooser highlightColor;
  
  
  
  /**
   * The last move made on the board.
   */
   
  private Move lastMove = null;



  /**
   * Creates a new MoveHighlightPanel for the specified BoardManager and with
   * the specified preview board.
   */
   
  public MoveHighlightPanel(BoardManager boardManager, JinBoard previewBoard){
    super(boardManager, previewBoard);
    
    I18n i18n = I18n.get(MoveHighlightPanel.class);
    
    int highlightStyle = boardManager.getMoveHighlightingStyle();
    none = i18n.createRadioButton("noMoveHighlightRadioButton");
    targetSquare = i18n.createRadioButton("targetSquareHighlightRadioButton");
    bothSquares = i18n.createRadioButton("bothSquaresHighlightRadioButton");
    arrow = i18n.createRadioButton("arrowHighlightRadioButton");
    
    none.setSelected(highlightStyle == JBoard.NO_MOVE_HIGHLIGHTING);
    targetSquare.setSelected(highlightStyle == JBoard.TARGET_SQUARE_MOVE_HIGHLIGHTING);
    bothSquares.setSelected(highlightStyle == JBoard.BOTH_SQUARES_MOVE_HIGHLIGHTING);
    arrow.setSelected(highlightStyle == JBoard.ARROW_MOVE_HIGHLIGHTING);
    
    highlightOwnMoves = i18n.createCheckBox("highlightOwnMovesCheckBox");
    highlightOwnMoves.setSelected(boardManager.isHighlightingOwnMoves());
    
    highlightColor = i18n.createColorChooser("moveHighlightColorChooser");
    highlightColor.setColor(boardManager.getMoveHighlightingColor());
    
    
    ButtonGroup group = new ButtonGroup();
    group.add(none);
    group.add(targetSquare);
    group.add(bothSquares);
    group.add(arrow);
    
    setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    
    JPanel contentPanel = new PreferredSizedPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    contentPanel.setBorder(BorderFactory.createCompoundBorder(
      i18n.createTitledBorder("moveHighlightPanel"),
      BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    
    none.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    targetSquare.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    bothSquares.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    arrow.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    highlightOwnMoves.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    highlightColor.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    contentPanel.add(none);
    contentPanel.add(targetSquare);
    contentPanel.add(bothSquares);
    contentPanel.add(arrow);
    contentPanel.add(highlightOwnMoves);
    contentPanel.add(highlightColor);
    contentPanel.add(Box.createVerticalGlue());
    
    contentPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    add(contentPanel);
    
    ActionListener styleListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        MoveHighlightPanel.this.previewBoard.setMoveHighlightingStyle(getMoveHighlightingStyle());
        
        highlightOwnMoves.setEnabled(!none.isSelected());
        highlightColor.setEnabled(!none.isSelected());
        
        fireStateChanged();
      }
    };
    none.addActionListener(styleListener);
    targetSquare.addActionListener(styleListener);
    bothSquares.addActionListener(styleListener);
    arrow.addActionListener(styleListener);
    
    highlightOwnMoves.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        if (lastMove.getPlayer().isWhite())
          MoveHighlightPanel.this.previewBoard.setHighlightedMove(highlightOwnMoves.isSelected() ? lastMove : null);
        
        fireStateChanged();
      }
    });
    
    highlightColor.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent evt){
        MoveHighlightPanel.this.previewBoard.setMoveHighlightingColor(highlightColor.getColor());
        
        fireStateChanged();
      }
    });
    
    
    previewBoard.getPosition().addMoveListener(new MoveListener(){
      public void moveMade(MoveEvent evt){
        Move move = evt.getMove();
        if (move.getPlayer().isBlack() || highlightOwnMoves.isSelected())
          MoveHighlightPanel.this.previewBoard.setHighlightedMove(move);
        else
          MoveHighlightPanel.this.previewBoard.setHighlightedMove(null);
          
        lastMove = move;
      }
    });
  }
  
  
  
  /**
   * Returns the currently selected move highlighting style.
   */
   
  private int getMoveHighlightingStyle(){
    if (none.isSelected())
      return JBoard.NO_MOVE_HIGHLIGHTING;
    else if (targetSquare.isSelected())
      return JBoard.TARGET_SQUARE_MOVE_HIGHLIGHTING;
    else if (bothSquares.isSelected())
      return JBoard.BOTH_SQUARES_MOVE_HIGHLIGHTING;
    else if (arrow.isSelected())
      return JBoard.ARROW_MOVE_HIGHLIGHTING;
    else
      throw new IllegalStateException("None of the radio buttons are selected");
  }
  
  
  
  /**
   * Sets the initial properties of the preview board.
   */
   
  public void initPreviewBoard(){
    previewBoard.setMoveHighlightingStyle(getMoveHighlightingStyle());
    previewBoard.setMoveHighlightingColor(highlightColor.getColor());
    
    Position pos = previewBoard.getPosition();
    
    Move move = Chess.getInstance().createMove(pos,
      Square.parseSquare("f5"), Square.parseSquare("c8"), null, "Bc8");
      
    pos.makeMove(move);
    previewBoard.setHighlightedMove(move);
  }
  
  

  /**
   * Applies any changes made by the user.
   */
   
  public void applyChanges() throws BadChangesException{
    boardManager.setMoveHighlightingStyle(getMoveHighlightingStyle());
    boardManager.setHighlightingOwnMoves(highlightOwnMoves.isSelected());
    boardManager.setMoveHighlightingColor(highlightColor.getColor());
  }
  
  
   
}


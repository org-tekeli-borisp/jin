/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The utillib library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The utillib library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with utillib library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.util.bool;


/**
 * A superclass for all unary boolean <code>Conditionals</code>.
 */

public abstract class UnaryBooleanConditional implements Conditional{


  /**
   * The argument.
   */

  private final Conditional arg;



  /**
   * Creates a new <code>UnaryBooleanConditional</code> with the specified argument.
   */

  public UnaryBooleanConditional(Conditional arg){
    if (arg == null)
      throw new IllegalArgumentException("Argument may not be null");
    this.arg = arg;
  }



  /**
   * Returns the argument <code>Conditional</code>.
   */

  public final Conditional getArg(){
    return arg;
  }


}

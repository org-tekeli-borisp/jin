/**
 * The utillib library. More information is available at http://www.jinchess.com/. Copyright (C)
 * 2002 Alexander Maryanovsky. All rights reserved.
 *
 * <p>The utillib library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * <p>The utillib library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with utillib
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package free.util;

/** This is a replacement for the 1.2 class java.lang.UnsupportedOperationException. */
public class UnsupportedOperationException extends RuntimeException {

  /** Constructs an UnsupportedOperationException with no detail message. */
  public UnsupportedOperationException() {}

  /** Constructs an UnsupportedOperationException with the specified detail message. */
  public UnsupportedOperationException(String message) {
    super(message);
  }
}

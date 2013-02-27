package com.cellasoft.univrapp.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Internal helper class for date conversions.
 * 
 * @author Mr Horn
 */
public final class Dates {

  /**
   * @see <a href="http://www.ietf.org/rfc/rfc0822.txt">RFC 822</a>
   */
  private static final SimpleDateFormat RFC822 = new SimpleDateFormat(
      "EEE, dd MMM yyyy HH:mm:ss Z", java.util.Locale.ENGLISH);

  /* Hide constructor */
  private Dates() {}
  
  /**
   * Parses string as an RFC 822 date/time.
 * @throws Exception 
   * 
   * @throws RSSFault if the string is not a valid RFC 822 date/time
   */
  public static java.util.Date parseRfc822(String date) throws Exception {
    try {
      return RFC822.parse(date);
    } catch (ParseException e) {
      throw new Exception(e);
    }
  }

}
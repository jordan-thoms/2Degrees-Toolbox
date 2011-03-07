/*******************************************************************************
 * Copyright (c) 2011 Jordan Thoms.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package biz.shadowservices.DegreesToolbox;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateFormatters {
	// A common class which contains a whole lot of standard date formatters, since java's date handling sucks balls and
	// adding a 3rd-party library might grow the apk too much.
	public static SimpleDateFormat ISO8601FORMAT    = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	public static SimpleDateFormat ISO8601DATEONLYFORMAT    = new SimpleDateFormat("yyyy-MM-dd");
	public static SimpleDateFormat EXPIRESDATE = new SimpleDateFormat("dd MMM yyyy");
	public static SimpleDateFormat LASTUPDATETIME = new SimpleDateFormat("h:mm a");
	public static SimpleDateFormat LASTUPDATEDATE = new SimpleDateFormat("dd MMM");
	public static SimpleDateFormat DATETIME = new SimpleDateFormat("dd MMM h:mm a");
	public static SimpleDateFormat SHORTDATE = new SimpleDateFormat("dd/MM");
	public static boolean isSameDay(Calendar cal1, Calendar cal2) {
	        if (cal1 == null || cal2 == null) {
	            throw new IllegalArgumentException("The date must not be null");
	        }
	        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
	                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
	                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
	 }
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }

}

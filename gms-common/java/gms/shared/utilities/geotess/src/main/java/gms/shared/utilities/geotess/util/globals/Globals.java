package gms.shared.utilities.geotess.util.globals;

import gms.shared.utilities.geotess.util.containers.arraylist.ArrayListDouble;
import gms.shared.utilities.geotess.util.containers.arraylist.ArrayListInt;
import gms.shared.utilities.geotess.util.propertiesplus.PropertiesPlus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * This class contains base constants for use by EMPS code.
 */
@SuppressWarnings("serial")
public class Globals implements Serializable
{
	public static final double TWO_PI = 2.0 * Math.PI;
	public static final double PI_OVR_TWO = 0.5 * Math.PI;
	public static final double NA_VALUE = -999999.0;
	public static final float NA_VALUE_FLOAT = -999999f;

	/**
	 * System-dependent new-line character.
	 */
	public static final String NL = System.getProperty("line.separator");

	public static double sqr(double x) { return x*x; }

	/**
	 * Returns an array of string tokens from the input string str. The split
	 * delimiters are given in delim.
	 * 
	 * @return An array of string tokens from the input string str.
	 */
	public static String[] getTokens(String str, String delim)
	{
		StringTokenizer st = new StringTokenizer(str, delim);
		if (st.countTokens() == 0) return null;

		String[] sa = new String[st.countTokens()];
		int i = 0;
		while (st.hasMoreTokens())
		{
			String s = st.nextToken();
			if (!s.equals("")) sa[i++] = s;
		}
		if (i == 0)
			return null;
		else if (i < sa.length)
		{
			String[] sanew = new String[i];
			System.arraycopy(sa, 0, sanew, 0, i);
			return sanew;
		}
		else
			return sa;
	}

	/**
	 * Pads the input string s with additional spaces until it reaches length
	 * len. If s.length() is >= len then the input string is simply returned.
	 *  
	 * @param s The input string to be padded.
	 * @param len The length of the string on output if the input string was
	 *            less than len on input.
	 * @return The input string padded with additional spaces up to length len.
	 *         If the input string s.length() >= len then the input string is
	 *         returned unchanged.
	 */
	public static String padString(String s, int len)
	{
		while (s.length() < len) s += " ";
		return s;
	}

	/**
	 * Pads the input string with spaces (" ") so that the entire string is len
	 * characters long and the original input string is justified to the right.
	 * 
	 * @param s   The input string.
	 * @param len The output length of the returned string.
	 * @return The right-justified string.
	 */
	public static String rightJustifyString(String s, int len)
	{
		if (s.length() < len) s = repeat(" ", len - s.length()) + s;
		return s;
	}

	/**
	 * Pads the input string with spaces (" ") so that the entire string is len
	 * characters long and the original input string is justified to the left.
	 * 
	 * @param s   The input string.
	 * @param len The output length of the returned string.
	 * @return The left-justified string.
	 */
	public static String leftJustifyString(String s, int len)
	{
		if (s.length() < len) s += repeat(" ", len - s.length());
		return s;
	}

	/**
	 * Centers the input string in a total length string of len. If the string
	 * length exceeds len then the string is simply returned. If the string
	 * padding( before and after) is an odd number (total) then one additional
	 * space is added after if preferAfter is true. Otherwise, the additional
	 * space is added in front of the string.
	 *  
	 * @param s The input string to be centered.
	 * @param len The length of the string on output if the input string was
	 *            less than len on input.
	 * @param preferAfter If true and the padding space (before and after) sums
	 *                    to an odd number the additional space is added after.
	 * @return The input string padded with additional spaces before and after
	 *         such that the total length is len on output.
	 */
	public static String centerString(String s, int len, boolean preferAfter)
	{
		while (s.length() < len-1) s = " " + s + " ";
		if (s.length() < len)
			if (preferAfter)
				s = s + " ";
			else
				s += " ";

		return s;
	}

	/**
	 * Repeats the input string (rep) n times and returns the result.
	 * 
	 * @param rep Input string to be repeated.
	 * @param n   Number of times to repeat input sring.
	 * 
	 * @return Input String rep repeated n times.
	 */
	public static String repeat(String rep, int n)
	{
		String s = "";
		for (int i = 0; i < n; ++i)
			s += rep;
		return s;
	}

	/**
	 * String alignment enums.
	 * 
	 * @author Jim
	 *
	 */
	public enum TableAlignment
	{
		LEFT,
		RIGHT,
		CENTER;
	}

	/**
	 * Short cut to converting windows file paths to LINUX if the platform is a
	 * LINUX platform.
	 * 
	 * @param  inputFilePath The path to be converted.
	 * @return The input path converted to a LINUX file name if it was necessary.
	 *         Otherwise the original path is returned.
	 */
	public static String convertWinFilePathToLinux(String inputFilePath)
	{
		return PropertiesPlus.convertWinFilePathToLinux(inputFilePath);
	}

	/**
	 * Text table generator. Used to build an orderly table of entries supplied
	 * by the data array. The table can have row headers and column headers
	 * including neither, either, or both. The table data columns can be aligned
	 * as can the row headers.  
	 * 
	 * @param hdr        An extra line header printed before every line (typically
	 *                   just spaces.
	 * @param title      The table title.
	 * @param rowColHdr  The row/column descriptor.
	 * @param colHdr     A multi-line array of column headers. The number of rows
	 *                   is arbitrary but the number of columns must match that
	 *                   of the data array. If column headers are not desired set
	 *                   this parameter to null.
	 * @param colAlign   The column alignment. The number of columns must match
	 *                   that of the data array.
	 * @param rowHdr     An array of row headers. The number of row entries must
	 *                   match the number of rows in the data array. If row
	 *                   headers are not desired set this parameter to null.
	 * @param rowAlign   The row header column alignment setting.
	 * @param data       A two-dimensional array of data.
	 * @param colspc     An additional amount of spacing between each column.
	 * 
	 * @return           A string containing the table.
	 */
	public static String makeTable(String hdr, String title, String rowColHdr,
			String[][] colHdr, TableAlignment[] colAlign,
			String[] rowHdr, TableAlignment rowAlign,
			String[][] data, int colspc)
	{
		// create column widths

		int[] colWidth       = new int [data[0].length];
		int[] dataColWidth   = new int [data[0].length];
		int   rowColHdrWidth = 0;

		// if row headers are defined calculate the rowColHdrWidth

		if (rowHdr != null)
		{
			rowColHdrWidth  = rowColHdr.length();
			for (int i = 0; i < rowHdr.length; ++i)
			{
				if (rowColHdrWidth < rowHdr[i].length())
					rowColHdrWidth = rowHdr[i].length();
			}
		}

		// adjust column widths from the data

		String[] dataRow;
		for (int i = 0; i < data.length; ++i)
		{
			dataRow = data[i];
			for (int j = 0; j < dataRow.length; ++j)
			{
				if (i == 0)
				{
					colWidth[j] = dataRow[j].length();
					dataColWidth[j] = dataRow[j].length();
				}
				else
				{
					if (colWidth[j] < dataRow[j].length())
						colWidth[j] = dataRow[j].length();
					if (dataColWidth[j] < dataRow[j].length())
						dataColWidth[j] = dataRow[j].length();
				}
			}
		}

		// if column headers are defined adjust the column widths with those

		if (colHdr != null)
		{
			String[] colHdrRow;
			for (int i = 0; i < colHdr.length; ++i)
			{
				colHdrRow = colHdr[i];
				for (int j = 0; j < colHdrRow.length; ++j)
				{
					if (colWidth[j] < colHdrRow[j].length())
						colWidth[j] = colHdrRow[j].length();
				}
			}
		}

		// now colWidth has largest value for each column ... add colspc and
		// calculate table width

		if (rowHdr != null) rowColHdrWidth += colspc;
		int tableWidth = rowColHdrWidth;
		for (int j = 0; j < data[0].length; ++j)
		{
			colWidth[j] += colspc;
			tableWidth += colWidth[j];
		}

		// now build table

		String s = "";

		// center title in tableWidth

		s += hdr + centerString(title, tableWidth, true) + NL;
		s += hdr + NL;

		// output column headers if defined

		if (colHdr != null)
		{
			for (int i = 0; i < colHdr.length; ++i)
			{
				// if last row of header print rowColHdr in first column followed by
				// last column header row ... otherwise just add spaces

				s += hdr;
				if (rowHdr != null)
				{
					if (i == colHdr.length - 1)
						s += leftJustifyString(rowColHdr, rowColHdrWidth);
					else
						s += leftJustifyString("", rowColHdrWidth);
				}

				String[] colHdrRow = colHdr[i];
				for (int j = 0; j < colHdrRow.length; ++j)
				{
					s += centerString(colHdrRow[j], colWidth[j], true);
				}
				s += NL;
			}
		}

		// create data column padding

		String[] colHdrSpc = new String [colWidth.length];    
		for (int j = 0; j < colWidth.length; ++j)
			colHdrSpc[j] = repeat(" ", (colWidth[j] - dataColWidth[j]) / 2);

		// output header/data separator

		s += hdr + repeat("-", tableWidth) + NL;

		// now print data rows

		for (int i = 0; i < data.length; ++i)
		{
			dataRow = data[i];
			s += hdr;
			if (rowHdr != null)
				s += alignString(rowHdr[i], "", rowColHdrWidth, rowAlign);

			for (int j = 0; j < dataRow.length; ++j)
				s += alignString(dataRow[j], colHdrSpc[j], colWidth[j], colAlign[j]);

			s += NL;
		}
		s += NL;

		return s;
	}

	public static String alignString(String s, String justHdrSpc,
			int len, TableAlignment align)
	{
		switch (align)
		{
		case LEFT:
			return leftJustifyString(justHdrSpc + s, len);
		case RIGHT:
			return rightJustifyString(s + justHdrSpc, len);
		case CENTER:
			return centerString(s, len, true);
		}

		return "";
	}

	/**
	 * Returns the elapsed time from the input start time (if one argument), or
	 * if there are two arguments, the time difference between time[1] - time[0].
	 * The time string will look like "#:##:##:##:#### days" if the input time is
	 * one day or longer; "##:##:##:#### hrs" if the input time is less than a
	 * day but greater than or equal to 1 hour; "#:##:#### min" if the input time
	 * is less than an hour but greater than or equal to 1 minute; "#:#### sec"
	 * if the input time is less than 1 minute but greater than or equal to 1
	 * second; or "# msec" if the input time is less than 1 second.
	 * 
	 * @param time Array of longs = start time if one argument, = start time and
	 *             end time if two arguments.
	 * 
	 * @return The equivalent time string 
	 */
	public static String elapsedTimeString(Date ... time)
	{
		long endTime = 0;
		if (time.length > 1)
			endTime = time[1].getTime();
		else
			endTime = (new Date()).getTime();
		return timeString(endTime - time[0].getTime());
	}

	/**
	 * Returns the elapsed time from the input start time (if one argument), or
	 * if there are two arguments, the time difference between time[1] - time[0].
	 * The time string will look like "#:##:##:##:#### days" if the input time is
	 * one day or longer; "##:##:##:#### hrs" if the input time is less than a
	 * day but greater than or equal to 1 hour; "#:##:#### min" if the input time
	 * is less than an hour but greater than or equal to 1 minute; "#:#### sec"
	 * if the input time is less than 1 minute but greater than or equal to 1
	 * second; or "# msec" if the input time is less than 1 second.
	 * 
	 * @param time Array of longs = start time if one argument, = start time and
	 *             end time if two arguments.
	 * 
	 * @return The equivalent time string 
	 */
	public static String elapsedTimeString(long ... time)
	{
		long endTime = 0;
		if (time.length > 1)
			endTime = time[1];
		else
			endTime = (new Date()).getTime();
		return timeString(endTime - time[0]);
	}

	/**
	 * Returns the time string from the input milisecond number. The time string
	 * will look like "#:##:##:##:#### days" if the input time is one day or
	 * longer; "##:##:##:#### hrs" if the input time is less than a day but
	 * greater than or equal to 1 hour; "#:##:#### min" if the input time is less
	 * than an hour but greater than or equal to 1 minute; "#:#### sec" if the
	 * input time is less than 1 minute but greater than or equal to 1 second; or
	 * "# msec" if the input time is less than 1 second.
	 * 
	 * @param tm
	 *          The input time in miliseconds.
	 * @return The equivalent time string.
	 */
	public static String timeString(long tm)
	{
		String ts = "";
		int days, hrs, min, sec, msec;
		days = hrs = min = sec = msec = 0;

		// calculate hours, minutes, seconds and milliseconds

		if (tm > 86400000)
		{
			days = (int) (tm / 86400000);
			tm -= days * 86400000;
		}
		if (tm > 3600000)
		{
			hrs = (int) (tm / 3600000);
			tm -= hrs * 3600000;
		}
		if (tm > 60000)
		{
			min = (int) (tm / 60000);
			tm -= min * 60000;
		}
		if (tm > 1000)
		{
			sec = (int) (tm / 1000);
			tm -= sec * 1000;
		}
		msec = (int) tm;

		// find largest unit and form output string

		if (days > 0)
		{
			ts = days + ":";
			if (hrs < 10) ts += "0";
			ts += hrs + ":";
			if (min < 10) ts += "0";
			ts += min + ":";
			if (sec < 10) ts += "0";
			ts += sec + ":";
			if (msec < 10)
				ts += "00";
			else if (msec < 100) ts += "0";
			ts += msec + " days";
		}
		else if (hrs > 0)
		{
			ts = hrs + ":";
			if (min < 10) ts += "0";
			ts += min + ":";
			if (sec < 10) ts += "0";
			ts += sec + ":";
			if (msec < 10)
				ts += "00";
			else if (msec < 100) ts += "0";
			ts += msec + " hrs";
		}
		else if (min > 0)
		{
			ts = min + ":";
			if (sec < 10) ts += "0";
			ts += sec + ":";
			if (msec < 10)
				ts += "00";
			else if (msec < 100) ts += "0";
			ts += msec + " min";
		}
		else if (sec > 0)
		{
			ts = sec + ":";
			if (msec < 10)
				ts += "00";
			else if (msec < 100) ts += "0";
			ts += msec + " sec";
		}
		else
			ts = msec + " msec";

		// return result

		return ts;
	}

	/**
	 * Returns the elapsed time from the input start time (if one argument), or
	 * if there are two arguments, the time difference between time[1] - time[0].
	 * If the input number is larger than 1 day of miliseconds then it is output
	 * as dt/1000/60/60/24 days, if larger than 1 hour it is output as
	 * dt/1000/60/60 hours, if larger than 1 minute it is output as dt/1000/60
	 * minutes, if larger than 1 second it is output as dt/1000 seconds,
	 * otherwise it is output as dt miliseconds.
	 * 
	 * @param time Array of longs = start time if one argument, = start time and
	 *             end time if two arguments.
	 * 
	 * @return The equivalent time string 
	 */
	public static String elapsedTimeString2(Date ... time)
	{
		long endTime = 0;
		if (time.length > 1)
			endTime = time[1].getTime();
		else
			endTime = (new Date()).getTime();
		double dt = endTime-time[0].getTime();
		return timeString(dt);
	}

	/**
	 * Converts the input memory value (mem) in units of bytes into a string with
	 * units of KB if mem > 1024, MB if mem > 1024^2, GB if mem > 1024^3, or TB
	 * if mem > 1024^4. The string is retuned as "x unit" where x is mem/1024^i
	 * and unit is described as above.
	 * 
	 * @param mem The input memory in units of bytes.
	 * @return The string "x unit" where x is mem/1024^i (i=0,1,2,3, or 4) and
	 *         unit is the corresponding unit bytes, KB, MB, GB, or TB.
	 */
	public static String memoryUnit(long mem)
	{
		double cKB = 1024.0;
		double memc = mem;
		String units = "bytes";
		if (memc > cKB) // > KB
		{
			double cMB = cKB * 1024.0;
			if (memc > cMB) // > MB
			{
				double cGB = cMB * 1024.0;
				if (memc > cGB) // > GB
				{
					double cTB = cGB * 1024.0;
					if (memc > cTB) // > TB
					{
						memc /= cTB;
						units = "TB";
					}
					else
					{
						memc /= cGB;
						units = "GB";
					}
				}
				else
				{
					memc /= cMB;
					units = "MB";
				}
			}
			else
			{
				memc /= cKB;
				units = "KB";
			}
		}
		return String.format("%7.2f %s", memc, units);
	}

	/**
	 * Returns the elapsed time from the input start time (if one argument), or
	 * if there are two arguments, the time difference between time[1] - time[0].
	 * If the input number is larger than 1 day of miliseconds then it is output
	 * as dt/1000/60/60/24 days, if larger than 1 hour it is output as
	 * dt/1000/60/60 hours, if larger than 1 minute it is output as dt/1000/60
	 * minutes, if larger than 1 second it is output as dt/1000 seconds,
	 * otherwise it is output as dt miliseconds.
	 * 
	 * @param time Array of longs = start time if one argument, = start time and
	 *             end time if two arguments.
	 * 
	 * @return The equivalent time string 
	 */
	public static String elapsedTimeString2(long ... time)
	{
		long endTime = 0;
		if (time.length > 1)
			endTime = time[1];
		else
			endTime = (new Date()).getTime();
		double dt = endTime-time[0];
		return timeString(dt);
	}

	/**
	 * Returns the elapsed time from the input start time (if one argument), or
	 * if there are two arguments, the time difference between time[1] - time[0].
	 * If the input number is larger than 1 day of miliseconds then it is output
	 * as dt/1000/60/60/24 day, if larger than 1 hour it is output as
	 * dt/1000/60/60 hr, if larger than 1 minute it is output as dt/1000/60
	 * min, if larger than 1 second it is output as dt/1000 sec,
	 * otherwise it is output as dt msec.
	 * 
	 * @param time Array of longs = start time if one argument, = start time and
	 *             end time if two arguments.
	 * 
	 * @return The equivalent time string 
	 */
	public static String elapsedTimeString3(long ... time)
	{
		long endTime = 0;
		if (time.length > 1)
			endTime = time[1];
		else
			endTime = (new Date()).getTime();
		double dt = endTime-time[0];
		return timeStringAbbrvUnits(dt);
	}

	/**
	 * Compute elapsed time from startTime to System.currentTimeMillis().  
	 * Input is the startTime in milliseconds. Output is
	 * either seconds, minutes, hours or days, depending on the 
	 * amount of time specified.
	 * @param startTime in msec.  
	 * @return elapsed time
	 */
	static public String elapsedTime(long startTime)
	{
		return elapsedTime(1e-3*(System.currentTimeMillis()-startTime));
	}

	/**
	 * Formats elapsed time.  Input is in seconds. Output is
	 * either seconds, minutes, hours or days, depending on the 
	 * amount of time specified.
	 * @param dt in seconds
	 * @return elapsed time
	 */
	static public String elapsedTime(double dt)
	{
		String units = "seconds";
		if (dt >= 60.)
		{
			dt /= 60.;
			units = "minutes";

			if (dt >= 60.)
			{
				dt /= 60.;
				units = "hours";

				if (dt >= 24.)
				{
					dt /= 24.;
					units = "days";
				}
			}
		}
		return String.format("%9.6f %s", dt, units);
	}

	/**
	 * Returns the elapsed time from the input number in miliseconds. If the
	 * input number is larger than 1 day of miliseconds then it is output as
	 * dt/1000/60/60/24 days, if larger than 1 hour it is output as dt/1000/60/60
	 * hours, if larger than 1 minute it is output as dt/1000/60 minutes, if
	 * larger than 1 second it is output as dt/1000 seconds, otherwise it is
	 * output as dt miliseconds.
	 * 
	 * @param dt The input time in miliseconds to be output as days, hours,
	 *           minutes, seconds, miliseconds depending on its magnitude.
	 * @return The formated string with units.
	 */
	public static String timeString(double dt)
	{
		String units = "miliseconds";
		if (dt > 1000)
		{
			dt /= 1000.;
			units = "seconds";

			if (dt >= 60.)
			{
				dt /= 60.;
				units = "minutes";

				if (dt >= 60.)
				{
					dt /= 60.;
					units = "hours";

					if (dt >= 24.)
					{
						dt /= 24.;
						units = "days";
					}
				}
			}
		}
		return String.format("%6.2f %s", dt, units);
	}

	/**
	 * Returns the elapsed time from the input number in miliseconds. If the
	 * input number is larger than 1 day of miliseconds then it is output as
	 * dt/1000/60/60/24 days, if larger than 1 hour it is output as dt/1000/60/60
	 * hours, if larger than 1 minute it is output as dt/1000/60 minutes, if
	 * larger than 1 second it is output as dt/1000 seconds, otherwise it is
	 * output as dt miliseconds. All units are output in abbreviated format
	 * ("msec", "sec", "min", "hr", "day").
	 * 
	 * @param dt The input time in miliseconds to be output as day, hr,
	 *           min, sec, msec depending on its magnitude.
	 * @return The formated string with abbreviated units.
	 */
	public static String timeStringAbbrvUnits(double dt)
	{
		String units = "msec";
		if (dt >= 1000)
		{
			dt /= 1000.;
			units = "sec";

			if (dt >= 60.)
			{
				dt /= 60.;
				units = "min";

				if (dt >= 60.)
				{
					dt /= 60.;
					units = "hr";

					if (dt >= 24.)
					{
						dt /= 24.;
						units = "day";
					}
				}
			}
		}
		return String.format("%7.2f %s", dt, units);
	}

	/**
	 * Converts the multi-line string str into the same string with hdr prepended
	 * at the beginning of each line. Useful for indenting a previously
	 * constructed string that uses NL or "\n" as a line separator.
	 * 
	 * @param str Multi-line string which will have hdr prepended to each line. 
	 * @param hdr Header to prepend to each line of the string.
	 * @return The new string with hdr prepended to each line.
	 */
	public static String prependLineHeader(String str, String hdr)
	{
		/**
		 * Simple inner class used to test for the next line separator
		 * in an input string. The class has two functions that return
		 * the next separator location in the string and the length of
		 * the separator. The class searches for separators NL and "\n".
		 * 
		 * @author jrhipp
		 *
		 */
		final class TestIndex
		{
			/**
			 * Length of last tested separator.
			 */
			private int L = 0;

			/**
			 * The "\r\n" separator.
			 */
			private final String RN = "\r\n";

			/**
			 * The "\n" separator.
			 */
			private final String N  = "\n";

			/**
			 * The "\r" separator.
			 */
			private final String R  = "\r";

			/**
			 * Returns the index of the next separator from fromIndex. If none are
			 * found after fromIndex -1 is returned.
			 * 
			 * @param str The string for which the next line separator will be found.
			 * @param fromIndex The index from which the search for the next line
			 *                  separator in str is begun.
			 * @return The next index in str that a line separator begins on.
			 */
			public int test(String str, int fromIndex)
			{
				// default return index to -1 and search from fromIndex for the next
				// line separator of type NL or "\n"

				int endIndex = -1;
				int endIndexRN = str.indexOf(RN, fromIndex);
				int endIndexN  = str.indexOf(N, fromIndex);
				int endIndexR  = str.indexOf(R, fromIndex);

				// determine if type NL or "\n" (if either) is closer

				if (endIndexRN != -1)
				{
					endIndex = endIndexRN;
					L = RN.length();
				}
				else if (endIndexN != -1)
				{
					endIndex = endIndexN;
					L = N.length();
				}
				else if (endIndexR != -1)
				{
					endIndex = endIndexR;
					L = R.length();
				}
				//        if (((endIndexRN != -1) && (endIndexN == -1)) ||
				//            ((endIndexRN != -1) && (endIndexN != -1) &&
				//             (endIndexRN < endIndexN)))
				//        {
				//          endIndex = endIndexRN;
				//          L = RN.length();
				//        }
				//        else if (((endIndexRN == -1) && (endIndexN != -1)) ||
				//                 ((endIndexRN != -1) && (endIndexN != -1) &&
				//                  (endIndexN < endIndexRN)))
				//        {
				//          endIndex = endIndexN;
				//          L = N.length();
				//        }

				// return result

				return endIndex;
			}

			/**
			 * Return the length of the last discovered line separator.
			 * 
			 * @return The length of the last discovered line separator.
			 */
			public int eolLength()
			{
				return L;
			}
		};

		// create string to contain result and a new TestIndex class

		String s = "";
		TestIndex testIndex = new TestIndex();

		// initialize fromIndex to 0 and get endIndex and length of separator (L)

		int fromIndex = 0;
		int endIndex  = testIndex.test(str, fromIndex);
		int L         = testIndex.eolLength();

		// loop until no separators remain

		while(endIndex != -1)
		{
			// prepend next line to s

			s        += hdr + str.substring(fromIndex, endIndex + L);

			// adjust fromIndex and get next end index and separator length

			fromIndex = endIndex + L;
			endIndex  = testIndex.test(str, fromIndex);
			L         = testIndex.eolLength();
		}

		// done ... prepend last line and return

		s += hdr + str.substring(fromIndex);
		return s;
	}

	/**
	 * Returns the current time stamp formatted as
	 *     "MMMM dd, yyyy, hh:mm:ss a"
	 *
	 * @return The current time stamp
	 */
	public static String getTimeStamp()
	{
		return getTimeStamp("MMMM dd, yyyy, hh:mm:ss a");
	}

	/**
	 * Returns the time stamp for the input time in milliseconds formatted as
	 *     "MMMM dd, yyyy, hh:mm:ss a"
	 *
	 * @param tim The input time to be output as a formatted string.
	 * @return The time stamp for the input time
	 */
	public static String getTimeStamp(long tim)
	{
		return getTimeStamp(tim, "MMMM dd, yyyy, hh:mm:ss a");
	}

	/**
	 * Returns the time stamp for the input time in milliseconds formatted as
	 * frmt.
	 *
	 * @param tim The input time to be output as a formatted string.
	 * @param frmt The format of the output time string.
	 * @return The time stamp for the input time
	 */
	public static String getTimeStamp(long tim, String frmt)
	{
		SimpleDateFormat formatter = new SimpleDateFormat(frmt);
		return formatter.format(new Date(tim));
	}

	/**
	 * Returns the current time stamp formatted as
	 *     "MMMM_dd_yyyy_hh_mm_ss_a"
	 * 
	 * which can be used as part of a valid file name.
	 * 
	 * @return The current time stamp
	 */
	public static String getValidFileNameTimeStamp()
	{
		String d = getTimeStamp("MMMM dd, yyyy, hh:mm:ss a");
		d = d.replaceAll(", ", "_");
		d = d.replaceAll(" ", "_");
		d = d.replaceAll(":", "_");

		return d;
	}

	/**
	 * Returns the current time stamp formatted according to the input
	 * format string frmt.
	 * 
	 * @param frmt The format of the output time string.
	 * @return The current time stamp.
	 */
	public static String getTimeStamp(String frmt)
	{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat(frmt);
		return formatter.format(cal.getTime());
	}

	/**
	 * Read a String from a binary file. First, read the length of the String
	 * (number of characters), then read that number of characters into the
	 * String.
	 * 
	 * @param input
	 *            DataInputStream
	 * @return String
	 * @throws IOException
	 */
	public static String readString(DataInputStream input) throws IOException
	{ return readString(input, input.readInt()); }

	/**
	 * Read a String of a specified length from a binary file. 
	 * 
	 * @param input DataInputStream
	 * @param nChar the number of characters to read into the string.
	 * @return String
	 * @throws IOException
	 */
	public static String readString(DataInputStream input, int nChar) throws IOException
	{
		if (nChar == 0) return "";
		byte[] bytes = new byte[nChar];
		input.read(bytes);
		return new String(bytes);
	}

	/**
	 * Write a String to a binary file. First write the length of the String
	 * (int number of characters) then write that many characters.
	 * 
	 * @param output
	 *            DataOutputStream
	 * @param s
	 *            String
	 * @throws IOException
	 */
	public static void writeString(DataOutputStream output, String s)
			throws IOException
	{
		output.writeInt(s.length());
		if (s.length() > 0)
			output.writeBytes(s);
	}

	/**
	 * Read a String from a ByteBuffer. First reads the integer length of String
	 * then the actual String contents.
	 * 
	 * @param input
	 * @return the String
	 */
	public static String readString(ByteBuffer input) {
		int size = input.getInt();
		if (size == 0)
			return "";
		byte[] buf = new byte[size];
		input.get(buf);
		return new String(buf);
	}

	/**
	 * Write integer length of String, followed by String contents to a
	 * ByteBuffer.
	 * 
	 * @param output
	 * @param s
	 * @throws IOException
	 */
	public static void writeString(ByteBuffer output, String s) {
		if (s == null || s.isEmpty())
			output.putInt(0);
		else {
			output.putInt(s.length());
			output.put(s.getBytes());
		}
	}

	/**
	 * Find index i such that x is >= xx[i] and < xx[i+1]. 
	 * If x <  xx[0] returns -1 except if range0 is true return 0. 
	 * If x == xx[xx.length-1] return xx.length-2
	 * If x >  xx[xx.length-1] return xx.length-1 except if rangeN is true return xx.length-2
	 * <p>
	 * This method is translation from Numerical Recipes in C++.
	 * 
	 * @param x array of monotonically increasing or decreasing x values
	 * @param xx the value of x for which the index is desired.
	 * @param range0 If x <  xx[0] returns -1 except if range0 is true return 0.
	 * @param rangeN If x >  xx[xx.length-1] return xx.length-1 except if rangeN is true return xx.length-2
	 */
	static public int hunt(double[] x, double xx, boolean range0, boolean rangeN)
	{
		int i = hunt(x,xx);
		if (i == -1 && range0) return 0;
		if (i == x.length-1 && rangeN) return x.length-2;
		return i;
	}

	/**
	 * Find index i such that xx is >= x[i] and < x[i+1]. 
	 * <br>If xx <  x[0] returns -1. 
	 * <br>If xx == x[xx.length-1] return x.length-2
	 * <br>If xx >  x[xx.length-1] return x.length-1
	 * <p>
	 * This method is translation from Numerical Recipes in C++.
	 * 
	 * @param x array of monotonically increasing or decreasing x values
	 * @param xx the value of x for which the index is desired.
	 * @return the index such that xx is >= x[i] and < x[i+1]. 
	 */
	static public int hunt(double[] x, double xx)
	{
		//		void NR::locate(Vec_I_DP &xx, const DP x, int &j)
		//		{
		//			int ju,jm,jl;
		//			bool ascnd;
		//
		//			int n=xx.size();
		//			jl=-1;
		//			ju=n;
		//			ascnd=(xx[n-1] >= xx[0]);
		//			while (ju-jl > 1) {
		//				jm=(ju+jl) >> 1;
		//				if (x >= xx[jm] == ascnd)
		//					jl=jm;
		//				else
		//					ju=jm;
		//			}
		//			if (x == xx[0]) j=0;
		//			else if (x == xx[n-1]) j=n-2;
		//			else j=jl;
		//		}

		int n=x.length;
		if (xx == x[0])  return 0;
		if (xx == x[n-1])  return n-2;

		int ju,jm,jl;
		boolean ascnd=(x[n-1] >= x[0]);;

		jl=-1;
		ju=n;
		while (ju-jl > 1) {
			jm=(ju+jl) >> 1;
		if (xx >= x[jm] == ascnd)
			jl=jm;
		else
			ju=jm;
		}
		return jl;
	}

	/**
	 * Find index i such that xx is >= x[i] and < x[i+1]. 
	 * <br>If xx <  x[0] returns -1. 
	 * <br>If xx == x[xx.length-1] return x.length-2
	 * <br>If xx >  x[xx.length-1] return x.length-1
	 * <p>
	 * This method is translation from Numerical Recipes in C++.
	 * 
	 * @param x array of monotonically increasing or decreasing x values
	 * @param xx the value of x for which the index is desired.
	 * @return the index such that xx is >= x[i] and < x[i+1]. 
	 */
	static public int hunt(float[] x, float xx)
	{
		int n=x.length;
		if (xx == x[0])  return 0;
		if (xx == x[n-1])  return n-2;

		int ju,jm,jl;
		boolean ascnd=(x[n-1] >= x[0]);;

		jl=-1;
		ju=n;
		while (ju-jl > 1) {
			jm=(ju+jl) >> 1;
			if (xx >= x[jm] == ascnd)
				jl=jm;
			else
				ju=jm;
		}
		return jl;
	}

	/**
	 * Interpolate a value from two 1-dimensional arrays
	 * @param x - monotonically increasing or decreasing array of input values in x direction
	 * @param y - array of input values in y direction
	 * @param xx - input value in x direction
	 * @param range0 If x <  xx[0] and range0 return y[0]. If x <  xx[0] and range0 is false return NaN.  
	 * @param rangeN If x >  xx[n-1] and rangeN return y[n-1]. If x >  xx[n-1] and rangeN is false return NaN.  
	 * @return interpolated output value in y direction
	 */
	static public double interpolate(double[] x, double[] y, double xx, boolean range0, boolean rangeN)
	{
		int i = Globals.hunt(x, xx);
		if (i == -1) return range0 ? y[0] : Double.NaN;
		if (i == x.length-1)  return rangeN ? y[y.length-1] : Double.NaN;
		return y[i] + (xx-x[i]) * (y[i+1]-y[i])/(x[i+1]-x[i]);
	}

	/**
	 * Interpolate a value from two 1-dimensional arrays
	 * @param x - monotonically increasing or decreasing array of input values in x direction
	 * @param y - array of input values in y direction
	 * @param xx - input value in x direction
	 * @param range0 If x <  xx[0] and range0 return y[0]. If x <  xx[0] and range0 is false return NaN.  
	 * @param rangeN If x >  xx[n-1] and rangeN return y[n-1]. If x >  xx[n-1] and rangeN is false return NaN.  
	 * @return interpolated output value in y direction
	 */
	static public float interpolate(float[] x, float[] y, float xx, boolean range0, boolean rangeN)
	{
		int i = Globals.hunt(x, xx);
		if (i == -1) return range0 ? y[0] : Float.NaN;
		if (i == x.length-1)  return rangeN ? y[y.length-1] : Float.NaN;
		return y[i] + (xx-x[i]) * (y[i+1]-y[i])/(x[i+1]-x[i]);
	}

	/**
	 * Interpolate a value from two 1-dimensional arrays
	 * @param x - monotonically increasing or decreasing array of input values in x direction
	 * @param y - array of input values in y direction
	 * @param xx - input value in x direction
	 * @return interpolated output value in y direction
	 */
	static public float interpolate(float[] x, float[] y, float xx)
	{
		int i = Globals.hunt(x, xx);
		if (i < 0 || i >= x.length-1) 
			return Float.NaN;
		return y[i] + (xx-x[i]) * (y[i+1]-y[i])/(x[i+1]-x[i]);
	}

	/**
	 * Interpolate a value from two 1-dimensional arrays
	 * @param x - monotonically increasing or decreasing array of input values in x direction
	 * @param y - array of input values in y direction
	 * @param xx - input value in x direction
	 * @return interpolated output value in y direction
	 */
	static public double interpolate(double[] x, double[] y, double xx)
	{
		int i = Globals.hunt(x, xx);
		if (i < 0 || i >= x.length-1) 
			return Double.NaN;
		return y[i] + (xx-x[i]) * (y[i+1]-y[i])/(x[i+1]-x[i]);
	}

	/**
	 * Perform 2D bilateral interpolation.
	 * @param v 2D array of values that are to be interpolated
	 * @param x 1D array of values in x direction
	 * @param y 1D array of values in y direction
	 * @param xx x-coordinate where interpolation is to occur
	 * @param yy y-coordinate where interpolation is to occur
	 * @param permissive if false and either xx or yy is out of range, NaN is returned.
	 * If true and xx or yy is out of range, a value is interpolated as if the coordinate 
	 * point resided on the edge of the valid range.
	 * @return interpolated value or NaN.
	 */
	static public double interpolate(double[][] v, double[] x, double[] y, double xx, double yy, boolean permissive)
	{
		int i = Globals.hunt(x, xx, permissive, permissive);
		if (i >= 0 && i < x.length-1)
		{
			int j = Globals.hunt(y, yy, permissive, permissive);
			if (j >= 0 && j < y.length-1)
			{
				double dx = (x[i+1]-xx)/(x[i+1]-x[i]);
				double dy = (y[j+1]-yy)/(y[j+1]-y[j]);
				return v[i][j]*dx*dy 
						+v[i+1][j]*(1.-dx)*dy
						+v[i][j+1]*dx*(1.-dy)
						+v[i+1][j+1]*(1.-dx)*(1.-dy);
			}
		}
		return Double.NaN;
	}

	/**
	 * Perform 3D trilateral interpolation
	 * @param v 3D array of values that are to be interpolated
	 * @param x 1D array of values in x direction
	 * @param y 1D array of values in y direction
	 * @param z 1D array of values in z direction
	 * @param xx x-coordinate where interpolation is to occur
	 * @param yy y-coordinate where interpolation is to occur
	 * @param zz z-coordinate where interpolation is to occur
	 * @param permissive if false and either xx, yy or zz is out of range, NaN is returned.
	 * If true and xx, yy or zz is out of range, a value is interpolated as if the coordinate 
	 * point resided on the edge of the valid range.
	 * @return
	 */
	static public double interpolate(double[][][] v, double[] x, double[] y, double[] z, double xx, double yy, double zz, boolean permissive)
	{
		int i = Globals.hunt(x, xx, permissive, permissive);
		if (i >= 0 && i < x.length-1)
		{
			int j = Globals.hunt(y, yy, permissive, permissive);
			if (j >= 0 && j < y.length-1)
			{
				int k = Globals.hunt(z, zz, permissive, permissive);
				if (k >= 0 && k < z.length-1)
				{
					double dx0 = (xx-x[i])/(x[i+1]-x[i]);
					double dx1 = (x[i+1]-xx)/(x[i+1]-x[i]);
					double dy0 = (yy-y[j])/(y[j+1]-y[j]);
					double dy1 = (y[j+1]-yy)/(y[j+1]-y[j]);
					double dz0 = (zz-z[k])/(z[k+1]-z[k]);
					double dz1 = (z[k+1]-zz)/(z[k+1]-z[k]);
					return v[i][j][k]*dx1*dy1*dz1 
							+v[i+1][j][k]*dx0*dy1*dz1
							+v[i][j+1][k]*dx1*dy0*dz1
							+v[i][j][k+1]*dx1*dy1*dz0							
							+v[i+1][j+1][k]*dx0*dy0*dz1
							+v[i+1][j][k+1]*dx0*dy1*dz0
							+v[i][j+1][k+1]*dx1*dy0*dz0
							+v[i+1][j+1][k+1]*dx0*dy0*dz0;
				}
			}
		}
		return Double.NaN;
	}
	static public double[] getArrayDouble(double first, double last, double interval)
	{
		if (first == last)
			return new double[] {first};
		int n = (int)Math.ceil(Math.abs(last-first)/interval);
		interval = (last-first)/n;
		double[] x = new double[n+1];
		for (int i=0; i<=n; ++i)
			x[i] = first+i*interval;
		return x;
	}

	static public float[] getArrayFloat(double first, double last, double interval)
	{
		if (first == last)
			return new float[] {(float) first};
		int n = (int)Math.ceil(Math.abs(last-first)/interval);
		interval = (last-first)/n;
		float[] x = new float[n+1];
		for (int i=0; i<=n; ++i)
			x[i] = (float) (first+i*interval);
		return x;
	}

	static public void histogram(ArrayListDouble values, double binSize, ArrayListDouble bins, ArrayListInt counts)
	{
		bins.clear();
		counts.clear();

		double vmin = Double.POSITIVE_INFINITY;
		double vmax = Double.NEGATIVE_INFINITY;

		for (int i=0; i<values.size(); ++i)
		{
			double v = values.get(i);
			if (v < vmin) vmin = v;
			if (v > vmax) vmax = v;
		}

		if (Double.isInfinite(vmin))
			return;

		vmin = binSize * Math.floor(vmin/binSize);
		vmax = binSize * Math.ceil(vmax/binSize);
		int n = (int) Math.ceil((vmax-vmin)/binSize);
		bins.setSize(n);
		counts.setSize(n);
		for (int i=0; i<n; ++i)
		{
			bins.set(i, vmin + i * binSize);
			counts.set(i, 0);
		}

		int index;
		for (int i=0; i<values.size(); ++i)
		{
			index = (int)((values.get(i)-vmin)/binSize);
			counts.set(index, counts.get(index)+1);
		}
	}

	/**
	 * Given a range of integers, return an int[] that contains all the integers.
	 * For example: "1, 5-7, 9" will return [1,5,6,7,9].
	 * @param stringList comma-delimited list of ranges.
	 * @return
	 */
	static public int[] getList(String stringList)
	{
		ArrayListInt list = new ArrayListInt();

		String[] sublists = stringList.replaceAll(" ", "").split(",");
		for (String s : sublists)
		{
			String[] range = s.split("-");
			if (range.length == 1)
				list.add(Integer.parseInt(range[0]));
			else if (range.length == 2)
				for (int i=Integer.parseInt(range[0]); i <= Integer.parseInt(range[1]); ++i)
					list.add(i);
		}

		return list.toArray();
	}

	/**
	 * Return a String containing the file size (e.g. "62.52 GB").
	 * If f is a directory, uses recursion to sum up the size of all
	 * files in f and all subdirectories.
	 * @param f
	 * @return
	 */
	static public String getFileSize(File f)
	{
		long[] size = new long[] {0L};
		fsize(f, size);
		return getFileSize((double)size[0]);
	}
	
	public static String getFileSize(double nBytes)
	{
		String[] units = new String[] {"B", "KB", "MB", "GB", "TB"};
		int n=0;
		while (nBytes > 1024. && n < units.length)
		{
			nBytes /= 1024.;
			++n;
		}
		return String.format("%1.2f %s", nBytes, units[n]);
		
	}

	/**
	 * Return the size of the specified file.
	 * If f is a directory, uses recursion to sum up the size of all
	 * files in f and all subdirectories.
	 * @param f
	 * @return
	 */
	static public long getFileSizeBytes(File f)
	{
		long[] size = new long[] {0L};
		fsize(f, size);
		return size[0];
	}

	static private void fsize(File f, long[] size)
	{
		if (f.exists())
		{
			if (f.isFile())
				size[0] += f.length();
			if (f.isDirectory())
				for (File ff : f.listFiles())
					fsize(ff, size);
		}
	}
	
	public static ArrayList<Double> toArrayList(double ... a)
	{
		ArrayList<Double> aa = new ArrayList<>(a.length);
		for (double x : a) aa.add(x);
		return aa;
	}

	public static ArrayList<Float> toArrayList(float ... a)
	{
		ArrayList<Float> aa = new ArrayList<>(a.length);
		for (float x : a) aa.add(x);
		return aa;
	}

	public static ArrayList<Integer> toArrayList(int ... a)
	{
		ArrayList<Integer> aa = new ArrayList<>(a.length);
		for (int x : a) aa.add(x);
		return aa;
	}
	
	static public String getExceptionAsString(Exception ex)
	{
		StringBuffer buf = new StringBuffer();
		if (ex.getMessage() != null)
			buf.append(ex.getMessage()).append('\n');
		else
			buf.append("Exception.getMessage() is null\n");
		buf.append(getStackTraceAsString(ex));
		return buf.toString();
	}

    static public String getStackTraceAsString(Exception ex)
    {
    	StringBuffer buf = new StringBuffer();
		// Recreate the stack trace into the error String.
		for (int i = 0; i < ex.getStackTrace().length; i++)
			buf.append(ex.getStackTrace()[i].toString()).append("\n");
    	return buf.toString();
    }
    
}

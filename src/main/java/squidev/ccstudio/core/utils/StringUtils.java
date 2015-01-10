/*
 * Segments from apache.commons.lang3.StringUtils
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package squidev.ccstudio.core.utils;


public class StringUtils {
	/**
	 * The empty String {@code ""}.
	 */
	public static final String EMPTY = "";

	/**
	 * Represents a failed index search.
	 */
	public static final int INDEX_NOT_FOUND = -1;

	/**
	 * <p>Checks if a CharSequence is empty ("") or null.</p>
	 *
	 * <pre>
	 * StringUtils.isEmpty(null)      = true
	 * StringUtils.isEmpty("")        = true
	 * StringUtils.isEmpty(" ")       = false
	 * StringUtils.isEmpty("bob")     = false
	 * StringUtils.isEmpty("  bob  ") = false
	 * </pre>
	 *
	 * <p>NOTE: This method changed in Lang version 2.0.
	 * It no longer trims the CharSequence.
	 * That functionality is available in isBlank().</p>
	 *
	 * @param cs  the CharSequence to check, may be null
	 * @return {@code true} if the CharSequence is empty or null
	 */
	public static boolean isEmpty(final CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

	/**
	 * <p>Strips any of a set of characters from the start and end of a String.
	 * This is similar to {@link String#trim()} but allows the characters
	 * to be stripped to be controlled.</p>
	 *
	 * <p>A {@code null} input String returns {@code null}.
	 * An empty string ("") input returns the empty string.</p>
	 *
	 * <pre>
	 * StringUtils.strip(null, *)          = null
	 * StringUtils.strip("", *)            = ""
	 * StringUtils.strip("abc", null)      = "abc"
	 * StringUtils.strip("  abc", null)    = "abc"
	 * StringUtils.strip("abc  ", null)    = "abc"
	 * StringUtils.strip(" abc ", null)    = "abc"
	 * StringUtils.strip("  abcyx", "xyz") = "  abc"
	 * </pre>
	 *
	 * @param str  the String to remove characters from, may be null
	 * @param stripChars  the characters to remove, null treated as whitespace
	 * @return the stripped String, {@code null} if null String input
	 */
	public static String strip(String str, final String stripChars) {
		if (isEmpty(str)) {
			return str;
		}
		str = stripStart(str, stripChars);
		return stripEnd(str, stripChars);
	}

	/**
	 * <p>Strips any of a set of characters from the start of a String.</p>
	 *
	 * <p>A {@code null} input String returns {@code null}.
	 * An empty string ("") input returns the empty string.</p>
	 *
	 * <p>If the stripChars String is {@code null}, whitespace is
	 * stripped as defined by {@link Character#isWhitespace(char)}.</p>
	 *
	 * <pre>
	 * StringUtils.stripStart(null, *)          = null
	 * StringUtils.stripStart("", *)            = ""
	 * StringUtils.stripStart("abc", "")        = "abc"
	 * StringUtils.stripStart("abc", null)      = "abc"
	 * StringUtils.stripStart("  abc", null)    = "abc"
	 * StringUtils.stripStart("abc  ", null)    = "abc  "
	 * StringUtils.stripStart(" abc ", null)    = "abc "
	 * StringUtils.stripStart("yxabc  ", "xyz") = "abc  "
	 * </pre>
	 *
	 * @param str  the String to remove characters from, may be null
	 * @param stripChars  the characters to remove, null treated as whitespace
	 * @return the stripped String, {@code null} if null String input
	 */
	public static String stripStart(final String str, final String stripChars) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return str;
		}
		int start = 0;
		if (stripChars == null) {
			while (start != strLen && Character.isWhitespace(str.charAt(start))) {
				start++;
			}
		} else if (stripChars.isEmpty()) {
			return str;
		} else {
			while (start != strLen && stripChars.indexOf(str.charAt(start)) != INDEX_NOT_FOUND) {
				start++;
			}
		}
		return str.substring(start);
	}

	/**
	 * <p>Strips any of a set of characters from the end of a String.</p>
	 *
	 * <p>A {@code null} input String returns {@code null}.
	 * An empty string ("") input returns the empty string.</p>
	 *
	 * <p>If the stripChars String is {@code null}, whitespace is
	 * stripped as defined by {@link Character#isWhitespace(char)}.</p>
	 *
	 * <pre>
	 * StringUtils.stripEnd(null, *)          = null
	 * StringUtils.stripEnd("", *)            = ""
	 * StringUtils.stripEnd("abc", "")        = "abc"
	 * StringUtils.stripEnd("abc", null)      = "abc"
	 * StringUtils.stripEnd("  abc", null)    = "  abc"
	 * StringUtils.stripEnd("abc  ", null)    = "abc"
	 * StringUtils.stripEnd(" abc ", null)    = " abc"
	 * StringUtils.stripEnd("  abcyx", "xyz") = "  abc"
	 * StringUtils.stripEnd("120.00", ".0")   = "12"
	 * </pre>
	 *
	 * @param str  the String to remove characters from, may be null
	 * @param stripChars  the set of characters to remove, null treated as whitespace
	 * @return the stripped String, {@code null} if null String input
	 */
	public static String stripEnd(final String str, final String stripChars) {
		int end;
		if (str == null || (end = str.length()) == 0) {
			return str;
		}

		if (stripChars == null) {
			while (end != 0 && Character.isWhitespace(str.charAt(end - 1))) {
				end--;
			}
		} else if (stripChars.isEmpty()) {
			return str;
		} else {
			while (end != 0 && stripChars.indexOf(str.charAt(end - 1)) != INDEX_NOT_FOUND) {
				end--;
			}
		}
		return str.substring(0, end);
	}


	/**
	 * <p>Joins the elements of the provided array into a single String
	 * containing the provided list of elements.</p>
	 *
	 * <p>No delimiter is added before or after the list.
	 * Null objects or empty strings within the array are represented by
	 * empty strings.</p>
	 *
	 * <pre>
	 * StringUtils.join(null, *)               = null
	 * StringUtils.join([], *)                 = ""
	 * StringUtils.join([null], *)             = ""
	 * StringUtils.join(["a", "b", "c"], ';')  = "a;b;c"
	 * StringUtils.join(["a", "b", "c"], null) = "abc"
	 * StringUtils.join([null, "", "a"], ';')  = ";;a"
	 * </pre>
	 *
	 * @param array  the array of values to join together, may be null
	 * @param separator  the separator character to use
	 * @return the joined String, {@code null} if null array input
	 * @since 2.0
	 */
	public static String join(final Object[] array, final char separator) {
		if (array == null) {
			return null;
		}
		return join(array, separator, 0, array.length);
	}

	/**
	 * <p>Joins the elements of the provided array into a single String
	 * containing the provided list of elements.</p>
	 *
	 * <p>No delimiter is added before or after the list.
	 * Null objects or empty strings within the array are represented by
	 * empty strings.</p>
	 *
	 * <pre>
	 * StringUtils.join(null, *)               = null
	 * StringUtils.join([], *)                 = ""
	 * StringUtils.join([null], *)             = ""
	 * StringUtils.join(["a", "b", "c"], ';')  = "a;b;c"
	 * StringUtils.join(["a", "b", "c"], null) = "abc"
	 * StringUtils.join([null, "", "a"], ';')  = ";;a"
	 * </pre>
	 *
	 * @param array  the array of values to join together, may be null
	 * @param separator  the separator character to use
	 * @param startIndex the first index to start joining from.  It is
	 * an error to pass in an end index past the end of the array
	 * @param endIndex the index to stop joining from (exclusive). It is
	 * an error to pass in an end index past the end of the array
	 * @return the joined String, {@code null} if null array input
	 * @since 2.0
	 */
	public static String join(final Object[] array, final char separator, final int startIndex, final int endIndex) {
		if (array == null) {
			return null;
		}
		final int noOfItems = endIndex - startIndex;
		if (noOfItems <= 0) {
			return EMPTY;
		}
		final StringBuilder buf = new StringBuilder(noOfItems * 16);
		for (int i = startIndex; i < endIndex; i++) {
			if (i > startIndex) {
				buf.append(separator);
			}
			if (array[i] != null) {
				buf.append(array[i]);
			}
		}
		return buf.toString();
	}
}

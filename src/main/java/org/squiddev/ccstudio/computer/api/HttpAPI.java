package org.squiddev.ccstudio.computer.api;

import org.luaj.vm2.*;
import org.squiddev.ccstudio.computer.Computer;
import org.squiddev.luaj.api.LuaAPI;
import org.squiddev.luaj.api.LuaFunction;
import org.squiddev.luaj.api.validation.StrictValidator;
import org.squiddev.luaj.api.validation.ValidationClass;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@LuaAPI("http")
@ValidationClass(StrictValidator.class)
public class HttpAPI {
	protected final Computer computer;

	public HttpAPI(Computer computer) {
		this.computer = computer;
	}

	@LuaFunction
	public Varargs checkURL(String url) {
		try {
			getURL(url);
			return LuaBoolean.TRUE;
		} catch (RuntimeException e) {
			return LuaValue.varargsOf(LuaBoolean.FALSE, LuaValue.valueOf(e.getMessage()));
		}
	}

	@LuaFunction
	public Varargs request(String urlString, Varargs args) {
		String postString = null;
		if (args.narg() >= 1 && args.arg1() instanceof LuaString) {
			postString = args.arg1().toString();
		}

		LuaTable headers = null;
		if (args.narg() >= 2 && args.arg(2) instanceof LuaTable) {
			headers = (LuaTable) args.arg(2);
		}

		try {
			new HttpRequest(urlString, postString, headers);
			return LuaBoolean.TRUE;
		} catch (RuntimeException e) {
			return LuaValue.varargsOf(LuaBoolean.FALSE, LuaValue.valueOf(e.getMessage()));
		}
	}

	public static URL getURL(String urlString) {
		URL url;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			throw new RuntimeException("URL malformed");
		}

		String protocol = url.getProtocol().toLowerCase();
		if ((!protocol.equals("http")) && (!protocol.equals("https"))) {
			throw new RuntimeException("URL not http");
		}

		return url;
	}

	public class HttpRequest {
		protected int responseCode;
		protected BufferedReader result;

		public HttpRequest(String urlString, String postData, LuaTable headers) {
			URL url = getURL(urlString);

			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						if (postData == null) {
							connection.setRequestMethod("GET");
						} else {
							connection.setRequestMethod("POST");
							connection.setDoOutput(true);
						}

						LuaValue key = LuaValue.NIL;
						while (headers != null) {
							Varargs keyVal = headers.next(key);
							key = keyVal.arg1();
							if (key.isnil()) break;

							LuaValue value = keyVal.arg(2);
							if (key instanceof LuaString && value instanceof LuaString) {
								connection.setRequestProperty(key.toString(), value.toString());
							}
						}

						if (postData != null) {
							BufferedWriter postWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
							postWriter.write(postData, 0, postData.length());
							postWriter.close();
						}

						BufferedReader resultReader = result = new BufferedReader(new InputStreamReader(connection.getInputStream()));
						StringBuilder resultOutput = new StringBuilder();

						// Read each line
						String line;
						while ((line = resultReader.readLine()) != null) {
							if (!computer.isAlive()) throw new RuntimeException("Computer is aborted");

							resultOutput.append(line).append('\n');
						}

						resultReader.close();

						responseCode = connection.getResponseCode();
						connection.disconnect();

						result = new BufferedReader(new StringReader(resultOutput.toString()));

						computer.queueEvent("http_success", LuaString.valueOf(urlString), computer.createLuaObject(HttpRequest.this).getTable());

					} catch (IOException e) {
						computer.queueEvent("http_failure", LuaString.valueOf(urlString), LuaString.valueOf("Could not connect"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		}

		@LuaFunction
		public String readLine() {
			try {
				return result.readLine();
			} catch (IOException e) {
				return null;
			}
		}

		@LuaFunction
		public String readAll() {
			try {
				StringBuilder contents = new StringBuilder();
				String line = result.readLine();
				while (line != null) {
					contents.append(line);
					line = result.readLine();
					if (line != null) contents.append('\n');
				}

				return contents.toString();
			} catch (IOException e) {
				return null;
			}
		}

		@LuaFunction
		public void close() {
			try {
				result.close();
			} catch (IOException ignored) {
			}
		}

		@LuaFunction
		public int getResponseCode() {
			return responseCode;
		}
	}
}

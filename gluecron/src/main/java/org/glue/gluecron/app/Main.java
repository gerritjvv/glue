package org.glue.gluecron.app;

public class Main {

	public static void main(String[] args) throws Throwable {

		final String app = args[0].trim();

		if (app.equals("list")) {
			ListFiles.main(args);
		} else if (app.equals("cron")) {
			CronApp.main(args);
		} else {
		}

	}
}

package org.glue.gluecron.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.glue.gluecron.trigger.TriggerListener;
import org.glue.gluecron.trigger.TriggersManager;

/**
 * 
 * Run as a standalone app that checks triggers and submits workflows if new
 * files/conditions are found.
 * 
 */
public class GlueTriggerWatch implements TriggerListener {

	private static final Logger LOG = Logger.getLogger(GlueTriggerWatch.class);

	final TriggersManager triggerManager;
	final String launchCmd;

	public GlueTriggerWatch(Configuration conf, TriggersManager manager) {

		this.triggerManager = manager;
		triggerManager.setTriggerListener(this);

		launchCmd = conf.getString("launch.cmd",
				"/opt/glue/bin/glue-client.sh -submit");

	}

	void shutdown() {

	}

	@Override
	public void launch(String[] unitNames) {

		final List<String> cmds = Arrays.asList(launchCmd.split(" "));

		//ensure that no duplicate names exist
		final Set<String> nameSet = new HashSet<String>(Arrays.asList(unitNames));
		
		for (String unitName : nameSet) {
			LOG.info("Launch: " + unitName);

			try {
				// launch command
				List<String> list = new ArrayList<String>(cmds);
				list.add(unitName);

				Process process = new ProcessBuilder(list).start();
				process.waitFor();

				// output data for the process
				outputData(process.getInputStream());
				outputData(process.getErrorStream());

			} catch (IOException e) {
				LOG.error(e.toString(), e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}

		}

	}

	private static final void outputData(InputStream in) throws IOException {
		if (in == null)
			return;

		BufferedReader bin = new BufferedReader(new InputStreamReader(in));

		String line = null;
		while ((line = bin.readLine()) != null) {
			LOG.info(line);
		}

	}
}

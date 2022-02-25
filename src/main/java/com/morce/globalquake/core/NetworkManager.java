package com.morce.globalquake.core;

import java.util.Calendar;

import com.morce.globalquake.database.SeedlinkNetwork;
import com.morce.globalquake.database.StationManager;

import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.seedlink.SeedlinkPacket;
import edu.sc.seis.seisFile.seedlink.SeedlinkReader;

public class NetworkManager {

	protected static final int RECONNECT_DELAY = 10;
	private GlobalQuake globalQuake;
	private Calendar lastData;

	public NetworkManager(GlobalQuake globalQuake) {
		this.globalQuake = globalQuake;
	}

	public GlobalQuake getGlobalQuake() {
		return globalQuake;
	}

	public void run() {
		for (SeedlinkNetwork seedlink : StationManager.seedlinks) {
			if (seedlink.selectedStations == 0) {
				System.out.println("No stations selected at " + seedlink.getHost());
				continue;
			}
			Thread seedlinkThread = new Thread("Network Thread - "+seedlink.getHost()) {
				@Override
				public void run() {
					while (true) {
						SeedlinkReader reader = null;
						try {
							seedlink.status = SeedlinkNetwork.CONNECTING;
							seedlink.connectedStations = 0;
							System.out.println("Connecting to seedlink server \"" + seedlink.getHost() + "\"");
							reader = new SeedlinkReader(seedlink.getHost(), 18000, 90, false);

							for (GlobalStation s : globalQuake.getStations()) {
								if (s.getSeedlinkNetwork() == seedlink.getId()) {
									System.out.println("Connecting to " + s.getStationCode() + " " + s.getNetworkCode()
											+ " " + s.getChannelName() + " " + s.getLocationCode() + ", "
											+ s.getSensitivity());
									reader.select(s.getNetworkCode(), s.getStationCode(), s.getLocationCode(),
											s.getChannelName());
									seedlink.connectedStations++;
								}
							}

							reader.startData("", "");
							seedlink.status = SeedlinkNetwork.CONNECTED;
							while (reader.hasNext()) {
								SeedlinkPacket slp = reader.readPacket();
								try {
									newPacket(slp.getMiniSeed());
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							reader.close();
						} catch (Exception e) {
							seedlink.status = SeedlinkNetwork.DISCONNECTED;
							seedlink.connectedStations = 0;
							e.printStackTrace();
							if (reader != null) {
								try {
									reader.close();
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}
							System.err.println(seedlink.getHost() + " Crashed, Reconnecting after " + RECONNECT_DELAY
									+ " seconds...");
							try {
								sleep(RECONNECT_DELAY * 1000);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
						}
					}
				}
			};
			seedlink.seedlinkThread = seedlinkThread;
			seedlinkThread.start();
		}
	}

	private void newPacket(DataRecord dr) {
		if (lastData == null || dr.getLastSampleBtime().convertToCalendar().after(lastData)) {
			lastData = dr.getLastSampleBtime().convertToCalendar();
		}
		String network = dr.getHeader().getNetworkCode().replaceAll(" ", "");
		String station = dr.getHeader().getStationIdentifier().replaceAll(" ", "");
		for (GlobalStation s : globalQuake.getStations()) {
			if (s.getNetworkCode().equals(network) && s.getStationCode().equals(station)) {
				s.addRecord(dr);
			}
		}
	}

}
package cn.jsou.ftpclient.ftp;

import java.util.HashSet;
import java.util.Set;

public class ServerInfo {
	private final Set<String> features = new HashSet<>();
	private       String      sysInfo;

	public ServerInfo() {}

	public String getSysInfo() {
		return sysInfo;
	}

	public void setSysInfo(String sysInfo) {
		this.sysInfo = sysInfo;
	}

	public void addFeature(String feature) {
		features.add(feature);
	}

	public boolean hasFeature(String feature) {
		return features.contains(feature);
	}
}

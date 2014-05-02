package org.icatproject.ids.storage;

import org.icatproject.ids.plugin.DfInfo;

public class DfInfoImpl implements DfInfo {

	private String createId;
	private long dfId;
	private String dfLocation;
	private String dfName;
	private String modId;

	public DfInfoImpl(String dfName) {
		this.dfName = dfName;
	}

	@Override
	public String getCreateId() {
		return createId;
	}

	@Override
	public long getDfId() {
		return dfId;
	}

	@Override
	public String getDfLocation() {
		return dfLocation;
	}

	@Override
	public String getDfName() {
		return dfName;
	}

	@Override
	public String getModId() {
		return modId;
	}

}

package org.icatproject.ids.storage;

import org.icatproject.ids.plugin.DsInfo;

public class DsInfoImpl implements DsInfo {

	private long dsId;
	private String dsLocation;
	private String dsName;
	private long facilityId;
	private String facilityName;
	private long invId;
	private String invName;
	private String visitId;

	public DsInfoImpl(String facilityName, String invName, String visitId, String dsName) {
		this.dsName = dsName;
		this.facilityName = facilityName;
		this.invName = invName;
		this.visitId = visitId;
	}

	@Override
	public long getDsId() {
		return dsId;
	}

	@Override
	public String getDsName() {
		return dsName;
	}

	@Override
	public String getDsLocation() {
		return dsLocation;
	}

	@Override
	public long getFacilityId() {
		return facilityId;
	}

	@Override
	public String getFacilityName() {
		return facilityName;
	}

	@Override
	public long getInvId() {
		return invId;
	}

	@Override
	public String getInvName() {
		return invName;
	}

	@Override
	public String getVisitId() {
		return visitId;
	}

}

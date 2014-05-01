package org.icatproject.ids.storage;

import org.icatproject.ids.plugin.DfInfo;
import org.icatproject.ids.plugin.DsInfo;
import org.icatproject.ids.plugin.ZipMapperInterface;

public class ZipMapper implements ZipMapperInterface {

	@Override
	public String getFullEntryName(DsInfo dsInfo, DfInfo dfInfo) {
		return "ids/" + dsInfo.getFacilityName() + "/" + dsInfo.getInvName() + "/"
				+ dsInfo.getVisitId() + "/" + dsInfo.getDsName() + "/" + dfInfo.getDfName();
	}

	@Override
	public String getFileName(String fullEntryName) {
		int l = fullEntryName.lastIndexOf('/');
		return l >= 0 ? fullEntryName.substring(l + 1) : null;
	}

}
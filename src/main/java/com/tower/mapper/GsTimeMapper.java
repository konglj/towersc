package com.tower.mapper;

import com.tower.common.bean.GsTime;

public interface GsTimeMapper {
	
	public GsTime getGsTime(int cityid);
	
	public int insertGsTime(GsTime gsTime);
	
	public int updateGsTime(GsTime gsTime);

}

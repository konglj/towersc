package com.tower.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tower.common.bean.City;
import com.tower.common.bean.GsTime;
import com.tower.mapper.GsTimeMapper;
import com.tower.mapper.SettingMapper;
import com.tower.service.GsTimeServcie;

@Service
public class GsTimeServiceImpl implements GsTimeServcie {

	@Autowired
	private GsTimeMapper gsTimeMapper;
	
	@Autowired
	private SettingMapper settingMapper;
	
	@Override
	@Transactional
	public int updateGsTime(int adminid, List<City> citys) {
		String gstime=settingMapper.getSettingContent("gstime");
		if(gstime == null)
			return  0;
		GsTime gsTime=null;
		int count=0;
		for (City city : citys) {
			gsTime=gsTimeMapper.getGsTime(city.getId());
			if(gsTime == null){
				gsTime=new GsTime();
				gsTime.setGstime(gstime);
				gsTime.setAdminid(adminid);
				gsTime.setTowercity(city.getId());
				count=gsTimeMapper.insertGsTime(gsTime);
			}else{
				gsTime.setGstime(gstime);
				count=gsTimeMapper.updateGsTime(gsTime);
			}
			if(count == 0)
				throw new RuntimeException();
		}
		return 1;	
	}

}

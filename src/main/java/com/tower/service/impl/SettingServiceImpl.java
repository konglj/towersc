package com.tower.service.impl;

import javax.xml.ws.soap.Addressing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tower.mapper.SettingMapper;
import com.tower.service.SettingService;

@Service
public class SettingServiceImpl implements SettingService {

	@Autowired
	private SettingMapper settingMapper;
	
	@Override
	public int updateTowerGs() {
		return settingMapper.updateTowerGs();
	}

}

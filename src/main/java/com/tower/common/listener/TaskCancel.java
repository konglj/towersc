package com.tower.common.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.tower.common.Config;
import com.tower.common.util.ParamerUtil;
import com.tower.mapper.TowersMapper;
import com.tower.service.OrderService;
import com.tower.service.TowerService;

@Component
public class TaskCancel {

	@Autowired
	private OrderService orderService;

	@Autowired
	private TowerService towerService;

	
	/**
	 * 订单失效，站址失效
	 */
	@Scheduled(cron = "0 0/30 * * * ? ")
	public void task() {
		System.out.println("1111 start");
		List<Map> orders = orderService.getCheckCancelOrder();
		if (orders != null && orders.size() > 0) {
			System.out.println("1111 start size:"+orders.size());
			for (Map map : orders) {
				System.out.println("1111 order id:"+map.get("orderid"));
				System.out.println("1111 order Tower_wx_id:"+map.get("towerwxid"));
				System.out.println("1111 order Tower_id:"+map.get("towerid"));
				try {
					
				
				int count=orderService.updateOrderCancel(map);
				System.out.println("1111 start update:"+count);
				} catch (Exception e) {
					System.out.println("1111 start update cancel error");
				}
			}

		}

		System.out.println("111");
	}

	/**
	 * 降级处理
	 */
	/*
	@Scheduled(cron = "0 0 12 * * ?")
	public void getUnOrderTower() {
		System.out.println("2222 start");
		try {

			List<Map> towers = towerService.getUnOrderTower();
			if (towers != null && towers.size() > 0) {
				for (Map map : towers) {

					
					String time="";
					if (map.get("lastordertime") == null) {
						String visitime = map.get("towervisibletime")==null?null:map.get("towervisibletime").toString();
						// 判读日志
						time=visitime;
						

					} else {
						time=map.get("lastordertime").toString();

					}
					if(ParamerUtil.isUpdateTowerLevel(time)){
					   Map mapLeve=new HashMap();
					   mapLeve.put("towerid", map.get("towerid"));
					   mapLeve.put("towerlevel", Config.jjtowerlevel);
					   int count=towerService.updateTowerLevel(mapLeve);
					}
				
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("22");
	}
*/
	
	@Scheduled(cron = "0 0 12 * * ?")
	public void getPosterOrderUndo() {
		System.out.println("33333 start");
		try {

			List<Map> orders = orderService.getCheckPostUnDo(Config.postundoday);
			if (orders != null && orders.size() > 0) {
				for (Map map : orders) {
					try {
						
						orderService.updateSendMessage(map);
						
					} catch (Exception e) {
						e.printStackTrace();
					
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("33");
	}

}

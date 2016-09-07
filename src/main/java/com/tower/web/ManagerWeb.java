package com.tower.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.tower.common.bean.AdminManagerInfo;
import com.tower.common.bean.Area;
import com.tower.common.bean.BindUser;
import com.tower.common.bean.BindUserPage;
import com.tower.common.bean.City;
import com.tower.common.bean.ManagerPage;
import com.tower.common.bean.ManagerPower;
import com.tower.common.bean.ManagerQueryItems;
import com.tower.common.bean.Power;
import com.tower.common.bean.SysUserInfo;
import com.tower.common.bean.TxPage;
import com.tower.common.bean.TxParameter;
import com.tower.common.bean.TxRecord;
import com.tower.common.bean.UserManagePage;
import com.tower.common.util.ExcelDC;
import com.tower.common.util.PageUtil;
import com.tower.common.util.ParamerUtil;
import com.tower.common.util.ResultUtil;
import com.tower.common.util.TimeUtil;
import com.tower.service.AreaService;
import com.tower.service.ManagerService;

@Controller
@RequestMapping("/manager")
public class ManagerWeb {

	@Autowired
	private ManagerService managerService;

	@Autowired
	private AreaService areaService;

	@RequestMapping("/manager")
	public ModelAndView managerAdmins(HttpServletRequest request,
			ManagerPage managerPage) {

		boolean isback=ParamerUtil.getIsBackPage(request);
		if(isback){
			managerPage=(ManagerPage)request.getSession().getAttribute("manager");
		}else{
			request.getSession().setAttribute("manager", managerPage);
		}
		ManagerQueryItems parameter = managerPage.getQueryItem();
		SysUserInfo sysUserInfo = ParamerUtil.getSysUserFromSesson(request);
		List<City> citys = sysUserInfo.getCitys();
		List<Area> areas = new ArrayList<Area>();// sysUserInfo.getAreas();

		//int cityid = managerPage.getQueryItem().getCityId();

		// 判断角色

		if(sysUserInfo.getAdminpower()!=1){
		  managerPage.setAdminCitys(sysUserInfo.getCitys());
		}
		managerPage.setPower(sysUserInfo.getAdminpower());
		try {
			Map map=new HashMap();
			map.put("id",sysUserInfo.getAdminpower() );
			List<Power> powers = managerService.getChildPowers(map);
			managerPage.setChildPowers(powers);
		} catch (Exception e) {
			e.printStackTrace();
		}

	
		try {
			List<AdminManagerInfo> admins = managerService
					.getAdmins(managerPage);
			managerPage.setAdmins(admins);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// int adminCount = managerService.getSysUsersCount(managerPage);
			// managerPage.setPagerowtotal(adminCount);
			// System.out.println("adminCount="+adminCount);
		} catch (Exception e) {

		}

		// 设置页面
		PageUtil pageUtil = new PageUtil(managerPage.getPageNo(),
				managerPage.getPagerowtotal());
		managerPage.setUsercitys(citys);
		managerPage.setPageinfo(pageUtil.getPage());
		managerPage.setAdminid(sysUserInfo.getAdminid());
		

		return new ModelAndView("/manager/manager", "managerPage", managerPage);
	}

	@RequestMapping("/editadmin")
	public ModelAndView editadmin(HttpServletRequest request, int id) {

		AdminManagerInfo admin = managerService.getAdmin(id);
		if (admin == null) {
			return null;
		}

		SysUserInfo sysUserInfo = ParamerUtil.getSysUserFromSesson(request);
		// 获取管理员自已管辖的权限
		Map map = new HashMap();
		map.put("id", sysUserInfo.getAdminpower());
		List<Power> childPowers = managerService.getChildPowers(map);
		// 获取管理员自己
		ManagerPage managerPage = new ManagerPage();
		managerPage.setAdmin(admin);
		
		int adminType = ParamerUtil.getAdminType(admin.getAdminpower());
		managerPage.setAdmintype(adminType);
		//超级管理员
		if(sysUserInfo.getAdminpower() ==1){
			List<City> citys=areaService.getCitys(null);
			
			managerPage.setUsercitys(citys);
		}else{
			
			map.clear();
			List<City> citys=sysUserInfo.getCitys();
			
			managerPage.setUsercitys(citys);
			
		}
		
		
		if(adminType == 2){
			map.put("cityid",admin.getAdminareacity());
			managerPage.setUserareas(areaService.getAreas(map));
			for (Area area : managerPage.getUserareas()) {
				String areaStrs=admin.getAdminarea();
				String []str=areaStrs.split(",");
				for (String s : str) {
					if(s!=null && !s.equals("")&&Integer.valueOf(s)==area.getId()){
						area.setIscheck(1);
						
					}
				}
			}
		}
		
		
		
		
		
		/*
		List<City> citys=areaService.getCitys(null);
		map.put("cityid", citys.get(0).getId());
		managerPage.setDefaultcityname(citys.get(0).getCityname());
		*/
		managerPage.setChildPowers(childPowers);
		
		// 获取

		return new ModelAndView("/manager/editmanager", "managerPage",
				managerPage);
	}

	@RequestMapping("/updateadmin")
	@ResponseBody
	public Object updateadmin(HttpServletRequest request,
			ManagerPage managerPage) {

		Map map = new HashMap();
		map.put("id", managerPage.getAdmin().getId());
		map.put("adminname", managerPage.getAdminname());
		map.put("adminpower", managerPage.getPower());
		int powertype=ParamerUtil.getAdminType(managerPage.getPower());
		if(powertype==2){
			String adminjiedaos=managerPage.getAdminjiedaos();
			if(adminjiedaos==null||adminjiedaos.equals(""))
				return ResultUtil.generateResponseMsg("error", "获取管理区域失败！");
			if(adminjiedaos.endsWith(","))
				adminjiedaos=adminjiedaos.substring(0,adminjiedaos.length()-1);
				
			map.put("adminarea", adminjiedaos);
			/*
			Map mapAreas=new HashMap();
			String [] areasStr=adminjiedaos.split(",");
			mapAreas.put("areas", areasStr);
			String citysStr=areaService.getCitysStrByAreas(mapAreas);
			if(citysStr!=null&&citysStr.endsWith(","))
			  citysStr=citysStr.substring(0,citysStr.length()-1);
			  */
		}
		if(powertype!=0)
			map.put("adminareacity", managerPage.getAdminCity());
		int count = 0;
		try {
			count = managerService.changeAdmin(map);
		} catch (Exception e) {

		}
		if (count == 0)
			return ResultUtil.generateResponseMsg("error");
		return ResultUtil.generateResponseMsg("success");
	}

	@RequestMapping("/addadmin")
	public ModelAndView addAdmin(HttpServletRequest request) {

		AdminManagerInfo admin = new AdminManagerInfo();
		ManagerPage managerPage = new ManagerPage();
		managerPage.setAdmin(admin);
		SysUserInfo sysUserInfo = ParamerUtil.getSysUserFromSesson(request);
		// 获取管理员自已管辖的权限
		Map map = new HashMap();
		map.put("id", sysUserInfo.getAdminpower());
		List<Power> childPowers = managerService.getChildPowers(map);
		/*
		 * //获取管理员自己 List<City> citys=sysUserInfo.getCitys(); List<Area>
		 * areas=new ArrayList<Area>(); List<City> usercitys=new
		 * ArrayList<City>(); int
		 * adminType=ParamerUtil.getAdminType(sysUserInfo.getAdminpower());
		 * if(adminType==0){ //默认角色选中城市管理员
		 * managerPage.getAdmin().setAdminpower(2);
		 * usercitys=sysUserInfo.getCitys(); } else if(adminType==1){
		 * managerPage.getAdmin().setAdminpower(3);
		 * usercitys=sysUserInfo.getCitys();
		 * managerPage.setOtherareas(sysUserInfo.getAreas()); }else
		 * if(adminType==2){ managerPage.setAdminareas(sysUserInfo.getAreas());
		 * 
		 * }
		 * 
		 * List<Area> userareas=new ArrayList<Area>();
		 * managerPage.setUserareas(userareas);
		 */

		List<City> citys=sysUserInfo.getCitys();
		map.put("cityid", citys.get(0).getId());
		//managerPage.setDefaultcityname(citys.get(0).getCityname());
		managerPage.setUserareas(areaService.getAreas(map));
		managerPage.setUsercitys(citys);

		managerPage.setChildPowers(childPowers);
		return new ModelAndView("/manager/addmanager", "managerPage",
				managerPage);
	}

	@RequestMapping("/addadmin1")
	public ModelAndView addAdmin1(HttpServletRequest request) {

		AdminManagerInfo admin = new AdminManagerInfo();
		ManagerPage managerPage = new ManagerPage();
		managerPage.setAdmin(admin);
		SysUserInfo sysUserInfo = ParamerUtil.getSysUserFromSesson(request);
		List<City> citys = sysUserInfo.getCitys();
		List<Area> areas = new ArrayList<Area>();

		int adminType = ParamerUtil.getAdminType(sysUserInfo.getAdminpower());
		if (adminType == 1) {
			managerPage.setAdminCity(citys.get(0).getCityid());
		} else if (adminType == 2) {
			managerPage.setAdminareas(sysUserInfo.getAreas());
		}
		List<City> usercitys = areaService.getCitys(null);
		List<Area> userareas = new ArrayList<Area>();
		managerPage.setUsercitys(usercitys);
		managerPage.setUserareas(userareas);
		return new ModelAndView("/manager/addmanager", "managerPage",
				managerPage);
	}

	@RequestMapping("/add_admin")
	@ResponseBody
	public Object addadmin(HttpServletRequest request, ManagerPage managerPage) {

		int count = 0;
		count = managerService.checkAdminidExist(managerPage.getAdminid());
		if (count > 0) {
			return ResultUtil.generateResponseMsg("error", "管理员账号名已存在");
		}
		Map map = new HashMap();
		map.put("adminid", managerPage.getAdminid());
		map.put("adminname", managerPage.getAdminname());
		map.put("adminpower", managerPage.getPower());
		int powertype=ParamerUtil.getAdminType(managerPage.getPower());
		if(powertype==2){
			String adminjiedaos=managerPage.getAdminjiedaos();
			if(adminjiedaos==null||adminjiedaos.equals(""))
				return ResultUtil.generateResponseMsg("error", "获取管理区域失败！");
			if(adminjiedaos.endsWith(","))
				adminjiedaos=adminjiedaos.substring(0,adminjiedaos.length()-1);
				
			map.put("adminarea", adminjiedaos);
			/*
			Map mapAreas=new HashMap();
			String [] areasStr=adminjiedaos.split(",");
			mapAreas.put("areas", areasStr);
			String citysStr=areaService.getCitysStrByAreas(mapAreas);
			if(citysStr!=null&&citysStr.endsWith(","))
			  citysStr=citysStr.substring(0,citysStr.length()-1);
			  */
			
		}
		if(powertype!=0)
			map.put("adminareacity", managerPage.getAdminCity());
		try {
			count = managerService.addAdmin(map);
		} catch (Exception e) {

		}
		if (count == 0)
			return ResultUtil.generateResponseMsg("error", "添加失败");
		return ResultUtil.generateResponseMsg("success");
	}

	@RequestMapping("/del_admin")
	@ResponseBody
	public Object deladmin(HttpServletRequest request, String ids) {
		Map map = new HashMap();
		map.put("ids", ids);

		int count = 0;
		try {
			count = managerService.deleteAdmin(map);
		} catch (Exception e) {

		}
		if (count == 0)
			return ResultUtil.generateResponseMsg("error");
		return ResultUtil.generateResponseMsg("success");
	}

	@RequestMapping("/resetpwd")
	@ResponseBody
	public Object resetpwd(HttpServletRequest request, int id) {
		int count = 0;
		try {
			count = managerService.changeAdminPwd(id);
		} catch (Exception e) {

		}
		if (count == 0)
			return ResultUtil.generateResponseMsg("error");
		return ResultUtil.generateResponseMsg("success");
	}

	@RequestMapping("/get_bind_users")
	public ModelAndView getBindUsers(HttpServletRequest request,
			BindUserPage bindUserPage) {

		managerService.getBindsUsers(bindUserPage);
		// 设置页面
		PageUtil pageUtil = new PageUtil(bindUserPage.getPageno(),
				bindUserPage.getPagerowtotal());
		bindUserPage.setPageinfo(pageUtil.getPage());
		return new ModelAndView("/manager/bind_user", "bindUserPage",
				bindUserPage);
	}

	@RequestMapping("/binduser")
	@ResponseBody
	public Object bindUser(int adminid, String wxid) {
		Map map = new HashMap();
		map.put("id", adminid);
		map.put("wxid", wxid);
		int count = managerService.bindUser(map);
		if (count == 0)
			return ResultUtil.generateResponseMsg("error");
		return ResultUtil.generateResponseMsg("success");
	}

	@RequestMapping("/unbinduser")
	@ResponseBody
	public Object unBindUser(int adminid) {

		Map map = new HashMap();
		map.put("id", adminid);
		int count = managerService.unBindUser(map);
		if (count == 0)
			return ResultUtil.generateResponseMsg("error");
		return ResultUtil.generateResponseMsg("success");
	}
	
	@RequestMapping("/dc_manager")
	@ResponseBody
	public void DcManager(HttpServletRequest request,HttpServletResponse response,
			ManagerPage managerPage) throws IOException{
		ManagerQueryItems parameter = managerPage.getQueryItem();
		SysUserInfo sysUserInfo = ParamerUtil.getSysUserFromSesson(request);
		//List<City> citys = sysUserInfo.getCitys();
		//List<Area> areas = new ArrayList<Area>();// sysUserInfo.getAreas();

		//int cityid = managerPage.getQueryItem().getCityId();

		// 判断角色

		if(sysUserInfo.getAdminpower()!=1){
		  managerPage.setAdminCitys(sysUserInfo.getCitys());
		}
		managerPage.setPower(sysUserInfo.getAdminpower());
		try {
			if (parameter.getAdminName() != null) {
				parameter.setAdminName(new String(parameter.getAdminName()
						.getBytes("ISO8859-1"), "utf-8"));
			}
		} catch (UnsupportedEncodingException e) {
		}

		List<Map> admins = managerService.getDcAdmins(managerPage);
		List<City> cities=areaService.getCityAndAreas(null);
		if(admins!=null&&admins.size()!=0){
			for (Map map : admins) {
				int power=Integer.valueOf(map.get("adminpower").toString());
				int powertype=ParamerUtil.getAdminType(power);
				if(powertype==0){
					map.put("city","全部");
					map.put("area", "全部");
				}else if(powertype==1){
					map.put("area", "全部");
				}else{
				   String areaStr=(String)map.get("adminarea");
				   String areas="";
				   int cityid=Integer.valueOf(map.get("cityid").toString());
				   if(areaStr!=null){
					   areaStr=","+areaStr+",";
					   for (City city : cities) {
						if(city.getId()==cityid){
							for (Area area : city.getAreas()) {
								String a=","+area.getId()+",";
								if(areaStr.contains(a)){
									areas+=area.getAreaname()+",";
								}
								
							}
						}
					}
					   
				   }
				   if(areas!=null&&areas.endsWith(","))
					   areas=areas.substring(0,areas.length()-1);
					map.put("area", areas);
				}
			}
		}
		

		ExcelDC ecDc = new ExcelDC(request, "manager.xls");
		HSSFWorkbook workbook = ecDc.getManagers(admins);
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			workbook.write(os);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] content = os.toByteArray();
		InputStream is = new ByteArrayInputStream(content);
		String filename="管理员详情("+TimeUtil.getNowDay()+").xls";
		 try {
			filename = new String((filename).getBytes(), "iso-8859-1");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// 设置response参数，可以打开下载页面
		response.reset();
		response.setContentType("application/vnd.ms-excel;charset=utf-8");
		response.setHeader("Content-Disposition",
				"attachment;filename="+filename);

		

		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;

		try {
			ServletOutputStream out = response.getOutputStream();
			bis = new BufferedInputStream(is);
			bos = new BufferedOutputStream(out);

			byte[] buff = new byte[2048];
			int bytesRead;

			// Simple read/write loop.
			while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
				bos.write(buff, 0, bytesRead);
			}

		} catch (final IOException e) {
			throw e;
		} finally {
			if (bis != null)
				bis.close();
			if (bos != null)
				bos.close();
		}
	}

}

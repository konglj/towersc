function tower_pd_info(userwxid,towerid){
	$("#userwxid").val(userwxid);
	$("#towerid").val(towerid);
	$(".tip").fadeIn(200);
}

function tower_pd_cancel(){
	$(".tip").fadeOut(200);
}

function user_city_change() {
	var city = $("#user_city");
	var area = $("#user_area");
	city_change(city, area);

}


function seach_money(){
	$("#pageno").val(1);
	$("#form_usermoney").submit();
	
}

function seach_user(){
	$("#pageno").val(1);
	$("#form_usermanage").submit();
	
}
function seach_pd_user(){
	$("#pageno").val(1);
	$("#form_userpd").submit();
}


function tower_pd(){
	$(".tip").fadeOut(200);
	var userwxid=$("#userwxid").val();
	var towerid=$("#towerid").val();
	if(userwxid=='' || towerid==0){
		alert("派单失败！");
		return;
	}	
	$.ajax({
		url : "../order/add_order.html",
		type : 'post',
		dataType : "json",
		data : {
			userwxid : userwxid,
			towerids : towerid
			
		},
		success : function(data) {
			if (data.result == "success") {
				if (confirm("派单成功！")) {
				  window.location = '../tower/tower_no.html';
				}else{
					 window.location = '../tower/tower_no.html';
				}
			} else {
				alert(data.msg);
			}
		}
	});

}

function dj(wxid, state) {
	var info = '';
	if (state == 0)
		info = "您确定要冻结该用户吗？";
	else {
		info = "您确定要解冻该用户吗？";
	}
	if (confirm(info)) {
		$.ajax({
			url : "updatestate.html",
			type : 'post',
			dataType : "json",
			data : {
				wxid : wxid,
				state : state
			},
			success : function(data) {
				if (data.result == "success") {
					if (confirm("操作成功！")) {
					window.location = 'usermanage.html';
					}else{
						window.location = 'usermanage.html';
					}
				} else {
					alert("操作失败！");
				}
			}
		});
	}
}

function yz(wxid, state) {
	var info = "您确定要验证该用户吗？";

	if (confirm(info)) {
		$.ajax({
			url : "updateveritystate.html",
			type : 'post',
			dataType : "json",
			data : {
				wxid : wxid
			},
			success : function(data) {
				if (data.result == "success") {
					if (confirm("操作成功！")) {
					window.location = 'usermanage.html';
					}else{
						window.location = 'usermanage.html';
					}
				} else {
					alert("操作失败！");
				}
			}
		});
	}
}

function modifytype() {
	$.ajax({
		url : "changetype.html",
		type : 'post',
		dataType : "json",
		data : $("#form_modifytype").serialize(),
		success : function(data) {
			if (data.result == "success") {
				if (confirm("修改成功！")) {
				window.location = 'usermanage.html';
				}else{
					window.location = 'usermanage.html';
				}
			} else {
				alert("修改失败！");
			}
		}
	});
}
function modifybz(){
	
	$.ajax({
		url : "changebz.html",
		type : 'post',
		dataType : "json",
		data : $("#form_modifybz").serialize(),
		success : function(data) {
			if (data.result == "success") {
				if (confirm("修改成功！")) {
				window.location = 'usermanage.html';
				}else{
					window.location = 'usermanage.html';
				}
			} else {
				alert("修改失败！");
			}
		}
	});
}

function dc_users() {
	location.href = "dc_users.html?" + $('#form_usermanage').serialize();

}
function dc_user_images(){
	location.href = "dc_user_image.html?" + $('#form_usermanage').serialize();
}

function dc_fee() {
	location.href = "dc_fee.html?" + $('#form_usermoney').serialize();

}

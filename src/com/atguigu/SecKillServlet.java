package com.atguigu;

import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SecKillServlet extends HttpServlet {
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String userid = new Random().nextInt(50000) +"" ; 

		String prodid =request.getParameter("prodid");
		
		//boolean if_success=SecKill_redis.doSecKill(userid,prodid);
		//boolean if_success=SecKill_redis1.doSecKill(userid,prodid);
		//boolean if_success=SecKill_redis2.doSecKill(userid,prodid);
		boolean if_success=SecKill_redisByScript.doSecKill(userid,prodid);
 
		response.getWriter().print(if_success);
	}
}

package com.atguigu;

import java.io.IOException;

import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

//案例1 存在超卖问题
public class SecKill_redis {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SecKill_redis.class);

	public static void main(String[] args) {

		Jedis jedis = new Jedis("192.168.135.100", 6379);

		System.out.println(jedis.ping());

		jedis.close();

	}

	public static boolean doSecKill(String uid, String prodid) throws IOException {
		String qtKey = "sk:"+prodid+":qt"; //库存key名称
		String usrKey = "sk:"+prodid+":usr"; //存储已经秒杀成功人的集合的key
		
		Jedis jedis = new Jedis("192.168.135.100", 6379);
		
		//判断用户是否已经秒到（不能重复秒杀）
		if(jedis.sismember(usrKey, uid)) { //返回结果为true，表示已经秒成功过，不能重复秒
			System.err.println("不能重复秒杀");
			jedis.close();
			return false;
		}
		
		//判断库存
		String qtKeyStr = jedis.get(qtKey);
		if(qtKeyStr == null || "".equals(qtKeyStr)) {
			System.err.println("库存未初始化");
			jedis.close();
			return false;
		}
		int qtCount = Integer.parseInt(qtKeyStr);
		if(qtCount<=0) {
			System.err.println("已经秒光了....");
			jedis.close();
			return false;
		}
		
		//减库存
		jedis.decr(qtKey);
		
		//保存：秒杀成功的人员列表（加人）
		jedis.sadd(usrKey, uid);//使用set类型存储秒杀成功的人。
		
		System.out.println("秒杀成功了....");
		jedis.close();
		
		return true;
	}

}

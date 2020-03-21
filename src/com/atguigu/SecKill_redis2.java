package com.atguigu;

import java.io.IOException;
import java.util.List;

import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

//案例3：
//1.连接超时问题（缓解，不能根除）
public class SecKill_redis2 {

	public static void main(String[] args) {

		Jedis jedis = new Jedis("192.168.135.100", 6379);

		System.out.println(jedis.ping());

		jedis.close();

	}

	public static boolean doSecKill(String uid, String prodid) throws IOException {
		String qtKey = "sk:"+prodid+":qt"; //库存key名称
		String usrKey = "sk:"+prodid+":usr"; //存储已经秒杀成功人的集合的key
		
		//Jedis jedis = new Jedis("192.168.135.128", 6379);
		JedisPool jedisPool = JedisPoolUtil.getJedisPoolInstance();
		Jedis jedis = jedisPool.getResource();
		
		jedis.watch(qtKey);
		
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
		
		//组队
		Transaction multi = jedis.multi();
		
		//减库存
		multi.decr(qtKey);
		
		//保存：秒杀成功的人员列表（加人）
		multi.sadd(usrKey, uid);//使用set类型存储秒杀成功的人。
		
		List<Object> execResult = multi.exec();
		if(execResult==null || execResult.size()==0) {
			System.out.println("秒杀失败的....");	
			jedis.close();
			return false;
		}
		System.out.println("秒杀成功了....");		
		jedis.close();
		
		return true;
	}

}

/**
 * 
 */
package inetbas.web.outsys.task;

import inetbas.sserv.SQLExecQuery;

/**
 * @author www.bip-soft.com
 *
 */
public class RabbitMQManager implements Runnable{
	private static SQLExecQuery _eq;
	private static RabbitMQManager rm;
	private static boolean cc=true;
	private static String cc1="";
	public static void exec(SQLExecQuery eq,boolean brestart){
		if(_eq!=null&&_eq.equals(eq)&&!brestart){
			return ;
		}
		_eq = eq;
		synchronized (cc1) {
			if(rm==null){
				rm = new RabbitMQManager();
			}else{
				if(brestart){
					rm.cc = false;
					rm = new RabbitMQManager();
					rm.cc = true;
				}
			}
			Thread th0 = new Thread(rm);
			th0.setPriority(Thread.MIN_PRIORITY);//;--最低优先级运行
			th0.start();//;--起动线程
		}
	}
	
	public static void start(boolean brestart){
		System.out.println("start");
		synchronized (cc1) {
			if(brestart){
				rm.cc = false;
				rm = new RabbitMQManager();
				rm.cc = true;
			}
			Thread th0 = new Thread(rm);
			th0.setPriority(Thread.MIN_PRIORITY);//;--最低优先级运行
			th0.start();//;--起动线程
		}
	}
	
	@Override
	public void run() {
		int i=0;
		synchronized (cc1) {
			while (cc) {
				System.out.println(i++);
				System.out.println(Thread.currentThread().getId());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(i==20){
					cc = false;
					cc1.notifyAll();
					rm.exec(_eq,true);
				}
			}
		}
	}

}

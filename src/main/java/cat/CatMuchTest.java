package cat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.tuchaoshi.base.concurrent.ForkJoinPool;


/**
 * 模拟链路
 * @author zzm
 *
 */
public class CatMuchTest {

	private static final ThreadLocal<Cat.Context> CAT_CONTEXT = new ThreadLocal<Cat.Context>();
	
	private static ForkJoinPool<Boolean> forkJoinPool = new ForkJoinPool<Boolean>(10);
	
	private static  Cat.Context RPC_CAT_CONTEXT = null;

	private static Cat.Context getContext() {
		Cat.Context context = CAT_CONTEXT.get();
		if (context == null) {
			context = initContext();
			CAT_CONTEXT.set(context);
		}
		return context;
	}

	private static Cat.Context initContext() {
		Cat.Context context = new DubboCatContext();
		if(RPC_CAT_CONTEXT!=null){
			context = RPC_CAT_CONTEXT;
		}
		return context;
	}

	public static void main(String[] args) throws Exception {
		client();
	}
	
	/**
	 * 服务端 PigeonService
	 */
	public static void service2(){
		 Transaction t = Cat.getProducer().newTransaction(
				 CatConstants.CROSS_SERVER, "myservice2transaction");
			try {
				Cat.getProducer().logEvent(CatConstants.PROVIDER_CALL_APP, "myservice2Testevent",
						Event.SUCCESS, "keyValuePairs");
				t.setStatus(Transaction.SUCCESS);
				 Cat.Context context = getContext();
				 Cat.logRemoteCallServer(context);
				 System.out.println("service2:  ROOT="+context.getProperty(context.ROOT)+" PARENT="+context.getProperty(context.PARENT)+" CHILD="+context.getProperty(context.CHILD));
				 System.out.println("service2 transaction=="+t);
			} catch (Exception e) {
				e.printStackTrace();
				Cat.getProducer().logError(e);
			} finally {
				t.complete();
				CAT_CONTEXT.remove();
			}
	}
	
	/**
	 * 服务端 PigeonService
	 */
	public static void service1(){
		
		 Transaction t = Cat.getProducer().newTransaction(
				 CatConstants.CROSS_SERVER, "myservice1transaction");
			try {
				Cat.getProducer().logEvent(CatConstants.PROVIDER_CALL_APP, "myservice1Testevent",
						Event.SUCCESS, "keyValuePairs");
				t.setStatus(Transaction.SUCCESS);
				 Cat.Context context = getContext();
				 Cat.logRemoteCallServer(context);
				 
				 
				 System.out.println(" service1  :" +"ROOT="+context.getProperty(context.ROOT)+" PARENT="+context.getProperty(context.PARENT)+" CHILD="+context.getProperty(context.CHILD));
		          
				 RPC_CAT_CONTEXT = context;
				 
				 service1Call();
				 
				
				 System.out.println("service1 transaction=="+t);
			} catch (Exception e) {
				e.printStackTrace();
				Cat.getProducer().logError(e);
			} finally {
				t.complete();
				CAT_CONTEXT.remove();
			}
		 
	}
	
	
	/**
	 * 服务端作为另外一个服务端的客户端  PigeonCall
	 */
	public static void service1Call(){
		Transaction t = Cat.getProducer().newTransaction(
				CatConstants.CROSS_CONSUMER, "myservice1Calltransaction");
		try {
			// yourBusinessOperation();
			//Thread.sleep(500);
			Cat.getProducer().logEvent(CatConstants.CONSUMER_CALL_APP, "myservice1CallTestevent",
					Event.SUCCESS, "keyValuePairs");
			t.setStatus(Transaction.SUCCESS);
			 Cat.Context context = getContext();
			 Cat.logRemoteCallClient(context);
			 //設置rpc context 
			 RPC_CAT_CONTEXT = context;
			 
			 System.out.println(" service1Call  :" + "ROOT="+context.getProperty(context.ROOT)+" PARENT="+context.getProperty(context.PARENT)+" CHILD="+context.getProperty(context.CHILD));
			 
			 forkJoinPool.addTask(new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					 service2();
					return true;
				}
			});
			 
			 forkJoinPool.executeTask();
			
			 System.out.println("service1Call transaction=="+t);
		} catch (Exception e) {
			e.printStackTrace();
			Cat.getProducer().logError(e);
		} finally {
			t.complete();
            //CAT_CONTEXT.remove();
		}
	}
	
	
	/**
	 * 客户端 PigeonCall
	 */
	public static void client(){
		Transaction t = Cat.getProducer().newTransaction(
				CatConstants.CROSS_CONSUMER, "myClienttransaction");
		try {
			// yourBusinessOperation();
			//Thread.sleep(500);
			Cat.getProducer().logEvent(CatConstants.CONSUMER_CALL_APP, "myClientTestevent",
					Event.SUCCESS, "keyValuePairs");
			t.setStatus(Transaction.SUCCESS);
			 Cat.Context context = getContext();
			 Cat.logRemoteCallClient(context);
			 //設置rpc context 
			 RPC_CAT_CONTEXT = context;
			 
			 System.out.println(" client  :" + "ROOT="+context.getProperty(context.ROOT)+" PARENT="+context.getProperty(context.PARENT)+" CHILD="+context.getProperty(context.CHILD));
	          
			 
			 forkJoinPool.addTask(new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					 service1();
					return true;
				}
			});
			 
			 forkJoinPool.executeTask();
			
			 System.out.println("client transaction=="+t);
		} catch (Exception e) {
			e.printStackTrace();
			Cat.getProducer().logError(e);
		} finally {
			t.complete();
			CAT_CONTEXT.remove();
		}
	}

	static class DubboCatContext implements Cat.Context {

		private Map<String, String> properties = new HashMap<String, String>();

		@Override
		public void addProperty(String key, String value) {
			properties.put(key, value);
		}

		@Override
		public String getProperty(String key) {
			return properties.get(key);
		}
	}

}

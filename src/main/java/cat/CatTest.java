package cat;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;

/**
 * cat 测试
 * @author zzm
 *
 */
public class CatTest {

	public static void main(String[] args) throws Exception {
		
		for(int i=0;i<100;i++){
			Transaction t = Cat.getProducer().newTransaction(
					"Testtransaction", "your transaction name");
			try {
				// yourBusinessOperation();
				//Thread.sleep(500);
				Cat.getProducer().logEvent("Testevent", "your event name",
						Event.SUCCESS, "keyValuePairs");
				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				e.printStackTrace();
				Cat.getProducer().logError(e);
				// 用log4j记录系统异常，以便在Logview中看到此信息 t.setStatus(e); throw e;
				// (CAT所有的API都可以单独使用，也可以组合使用，比如Transaction中嵌套Event或者Metric。)
				// (注意如果这里希望异常继续向上抛，需要继续向上抛出，往往需要抛出异常，让上层应用知道。)
				// (如果认为这个异常在这边可以被吃掉，则不需要在抛出异常。)

			} finally {
				t.complete();
				
			}
			System.out.println(i);
			Thread.sleep(5000);
			System.out.println(t.isSuccess());
			System.out.println(t.isCompleted());
			
		}
		
		Thread.sleep(60000 * 60);
		
	}

}

package cat;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;

/**
 * cat ����
 * @author zzm
 *
 */
public class CatTest {

	public static void main(String[] args) throws Exception {
		
		for(int i=0;i<100;i++){
			long start = System.currentTimeMillis();
			Transaction t = Cat.getProducer().newTransaction(
					"Testtransaction", "your transaction name");
			System.out.println("create Transaction time : " +(System.currentTimeMillis() - start));

			
			try {
				// yourBusinessOperation();
				//Thread.sleep(500);
				Cat.getProducer().logEvent("Testevent", "your event name",
						Event.SUCCESS, "keyValuePairs");
				t.setStatus(Transaction.SUCCESS);
				
				
			} catch (Exception e) {
				e.printStackTrace();
				Cat.getProducer().logError(e);
				// ��log4j��¼ϵͳ�쳣���Ա���Logview�п�������Ϣ t.setStatus(e); throw e;
				// (CAT���е�API�����Ե���ʹ�ã�Ҳ�������ʹ�ã�����Transaction��Ƕ��Event����Metric��)
				// (ע���������ϣ���쳣���������ף���Ҫ���������׳���������Ҫ�׳��쳣�����ϲ�Ӧ��֪����)
				// (�����Ϊ����쳣����߿��Ա��Ե�������Ҫ���׳��쳣��)
			} finally {
				
				t.complete();
				System.out.println("all time : " +(System.currentTimeMillis() - start));
			}
			System.out.println(i);
			Thread.sleep(5000);
			System.out.println(t.isSuccess());
			System.out.println(t.isCompleted());
			
		}
		
		Thread.sleep(60000 * 60);
		
	}

}

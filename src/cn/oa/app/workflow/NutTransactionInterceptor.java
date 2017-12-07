package cn.oa.app.workflow;

import java.sql.Connection;

import org.nutz.trans.Trans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snaker.engine.access.transaction.TransactionInterceptor;
import org.snaker.engine.access.transaction.TransactionStatus;

public class NutTransactionInterceptor extends TransactionInterceptor {

	private static final Logger log = LoggerFactory.getLogger(NutTransactionInterceptor.class);

	public void initialize(Object accessObject) {
	}

	protected TransactionStatus getTransaction() {
		try {
			boolean isNew = false;
			if (Trans.get() == null) {
				Trans.begin(Connection.TRANSACTION_REPEATABLE_READ);
				isNew = true;
			}
			return new TransactionStatus(Trans.get(), isNew);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	protected void commit(TransactionStatus status) {
		try {
			Trans.commit();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			try {
				Trans.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw new RuntimeException(e.getMessage(), e);
			}
		}
	}

	protected void rollback(TransactionStatus status) {
		try {
			Trans.rollback();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			try {
				Trans.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw new RuntimeException(e.getMessage(), e);
			}
		}
	}
}

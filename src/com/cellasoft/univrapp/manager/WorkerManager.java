package com.cellasoft.univrapp.manager;

public class WorkerManager {

	private static WorkerManager wm;
	private Thread currentWorker;

	public static WorkerManager getInstance() {
		if (wm == null) {
			wm = new WorkerManager();
		}
		return wm;
	}

	/**
	 * Imposta l'{@link RSSWorker} attualmente attivo e termina quello
	 * precedente.
	 * 
	 * @param worker
	 *            il nuovo worker.
	 */
	public synchronized void setCurrentWorker(Thread worker) {
		interruptCurrentWorker();
		currentWorker = worker;
	}

	/**
	 * Verifica se il worker dato è quello corrente attivo.
	 * 
	 * @param worker
	 *            thread da confrontare con quella corrente.
	 * @return <b>true</b> se il worker dato è quello corrente; <b>false</b>
	 *         altrimenti.
	 */
	public synchronized boolean isCurrentWorker(Thread worker) {
		return (currentWorker == worker);
	}

	/**
	 * Interrompe il worker corrente.
	 */
	public synchronized void interruptCurrentWorker() {
		if (currentWorker != null)
			currentWorker.interrupt();
	}
}

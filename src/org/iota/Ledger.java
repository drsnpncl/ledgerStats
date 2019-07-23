package org.iota;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ledger {
	
	Utilities utils = new Utilities();
	List<Transaction> ledger = new ArrayList<>();
	
	public Ledger(String database) {
		/*An instance of whole ledger created with input database*/
		ledger = utils.readLedger(database);
	}
	
	/*	Calculates average depth of a DAG*/
	public float getAvgMinDepth(List<Transaction> ledger) {
		Map<Integer, Integer> depths = utils.generateMinDepth(ledger);
		int totalDepth = 0;
		int totalNodes = 0;
		float avgMinDepth = 0f;
		
		for(Integer key : depths.keySet()) {
			totalDepth += depths.get(key);
			totalNodes++;
		}
		
		avgMinDepth = (float) totalDepth/totalNodes;
		return avgMinDepth;
	}
	
	/*	Calculates average transactions per depth*/
	public float getAvgTxnPerDepth(List<Transaction> ledger) {
		Map<Integer, Integer> minDepths = utils.generateMinDepth(ledger);
		Map<Integer, Integer> depthMap = new HashMap<>();
		int depthCount = 0;
		float avgTxnPerDepth = 0f;
		
		minDepths.forEach((key, value) -> {
			if(depthMap.containsKey(value)) {
				depthMap.put(value, depthMap.get(value) + 1);
			} else {
				/*We will ignore the point of origin*/
				if(key != 1) {
					depthMap.put(value, 1);
				}
			}
		});
				
		for(int value : depthMap.values()) depthCount += value;
		
		avgTxnPerDepth = (float) depthCount/depthMap.keySet().size();
		return avgTxnPerDepth;
	}
	
	/*	Calculates average of in-references per node	*/
	/*	referenceMap contains a map of (txn, list of txns in-reference of this txn)	*/
	public float getAvgInReferences(List<Transaction> ledger) {
		Map<Integer, List<Integer>> referenceMap = utils.getInReferences(ledger);
		Map<Integer, Integer> referenceCount = new HashMap<>();
		int totalReferences = 0;
		int totalNodes = 0;
		int size = 0;
		float avgInReferences = 0f;
		
		for(Integer key : referenceMap.keySet()) {
			size = referenceMap.get(key).size();
			totalReferences += size;
			referenceCount.put(key, size);
			totalNodes++;
		}
		System.out.println(referenceMap);
		avgInReferences = (float) totalReferences/totalNodes;
		return avgInReferences;
	}
	
	/*	Cumulative InRefenrences determines the list of txns needs to be changes in order to change a perticular transaction	*/
	public float getAvgCumulativeInReferences(List<Transaction> ledger) {
		Map<Integer, List<Integer>> cumulativeInRefMap = utils.getCumulativeInReferences(ledger);
		Map<Integer, Integer> cumulativeInRefCount = new HashMap<>();
		int totalReferences = 0;
		int totalNodes = 0;
		int size = 0;
		float avgCumulativeInRef = 0f;
		
		for(Integer key : cumulativeInRefMap.keySet()) {
			if(key != 1) {
				size = cumulativeInRefMap.get(key).size();
				totalReferences += size;
				cumulativeInRefCount.put(key, size);
				totalNodes++;
			}
		}

		avgCumulativeInRef = (float) totalReferences/totalNodes;
		return avgCumulativeInRef;
	}
	
	/*	It return percentage transactions to be changed for this transaction	*/
	public float getSecurityFactorByTxn(int txn, List<Transaction> ledger) {
		if(txn == 1) {
			System.out.println("Origin is not a valid transaction.");
			return 0;
		} else {
			Map<Integer, List<Integer>> cumulativeInRefMap = utils.getCumulativeInReferences(ledger);
			int size = cumulativeInRefMap.get(txn).size();
			return (float) size/ledger.size() * 100;
		}
	}
	
	/* Security factor is average percentage of transactions to be changed in order to change a particular transaction */
	public float getAverageSecurityFactor(List<Transaction> ledger) {
		return getAvgCumulativeInReferences(ledger)/ledger.size() * 100;
	}

	/*	Calculates average of number of cumulative approvals per transactions */
	/*	Does not include origin	*/
	/*	Map of approvedTransactions is a map of (txn, list of txns it approves)	*/
	public float getAvgApprovals(List<Transaction> ledger) {
		Map<Integer, List<Integer>> approvedtransactions = utils.getApprovedTransactions(ledger);
		Map<Integer, Integer> approvalCount = new HashMap<>();
		int totalApprovals = 0;
		int totalNodes = 0;
		int size = 0;
		float avgApprovals = 0f;
		
		for(Integer key : approvedtransactions.keySet()) {
			size = approvedtransactions.get(key).size();
			totalApprovals += size;
			approvalCount.put(key, size);
			totalNodes++;
		}
		
		avgApprovals = (float) totalApprovals/totalNodes;
		return avgApprovals;
	}
	
	/*	This method calculates the strength of the ledger, hence the tangle
	 * 	It's a ratio of average approvals in the ledger(tangle) to maximum number of average approvals possible
	 * 	In the ideal case every new transaction approves all previous transactions on the ledger 
	 * */
	public float getLedgerStrength(List<Transaction> ledger) {
		float avgApprovals = getAvgApprovals(ledger);
		float ledgerStrength = (float) ((avgApprovals * 100) / utils.getMaxStrength(ledger.size()));
		return ledgerStrength;
	}
	
	public static void main(String[] args) {
		Ledger ledger = new Ledger(args[0]);
		System.out.println("AVG DAG DEPTH: " + ledger.getAvgMinDepth(ledger.ledger));
		System.out.println("AVG REF: " + ledger.getAvgInReferences(ledger.ledger));
		System.out.println("AVG TXN PER DEPTH: " + ledger.getAvgTxnPerDepth(ledger.ledger));
		System.out.println("AVG APPROVALS: " + ledger.getAvgApprovals(ledger.ledger));
		System.out.println("LEDGER STRENGTH: " + ledger.getLedgerStrength(ledger.ledger) + "%");
		System.out.println("AVG CUMULATIVE IN REF: " + ledger.getAvgCumulativeInReferences(ledger.ledger));
		System.out.println("AVG SECURITY FACTOR: " + ledger.getAverageSecurityFactor(ledger.ledger) + "%");
		System.out.println("SECURITY FACTOR OF TXN 2: " + ledger.getSecurityFactorByTxn(2, ledger.ledger) + "%");
	}
}

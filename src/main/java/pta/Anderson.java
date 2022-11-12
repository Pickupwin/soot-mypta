package pta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashSet;

import soot.Local;

class Constraint {
	public final Identifier dst, src;
	public Constraint(Identifier dst, Identifier src){
		this.dst=dst;
		this.src=src;
	}
}

public class Anderson {

	public final Set<String> printDone = new HashSet<>();

	public final Map<String, Set<String>> pointTo = new HashMap<>();

	private final List<Constraint> ConstraintsList = new ArrayList<Constraint>();

	private Map<String, Integer> methodCounter = new HashMap<String, Integer>();


	private static final String REP_PREFIX = "@repeat";
	private static final String HEAP_PREFIX = "@heap";

	private boolean solveAlias(Identifier lhs, Identifier rhs){
		// if (printDone.add(lhs.toString()+"  <==  "+rhs.toString()))
		// 	System.out.println(lhs.toString()+"  <==  "+rhs.toString());
		boolean ret = false;
		List<String> rhsList = rhs.genStringList(pointTo);
		if (lhs.fa == null){
			// System.out.println(lhs.toString());
			return false;
		}
		List<String> lhsList = lhs.fa.genStringList(pointTo);
		for (String ls: lhsList) {
			for (String rs: rhsList){
				ret = ret || (pointTo.putIfAbsent(ls+"."+lhs.name, new HashSet<String>()) == null);
				ret = ret || pointTo.get(ls+"."+lhs.name).add(rs);
			}
		}
		return ret;
	}

	private boolean solveAliasEx(Identifier lhs, Identifier rhs){
		boolean ret = false;
		String lhs_m = lhs.getMethod();
		String rhs_m = rhs.getMethod();
		if(!lhs_m.startsWith(REP_PREFIX) && !lhs_m.startsWith(HEAP_PREFIX)) {
			if (lhs_m.equals(rhs_m)) {
				Integer cnt = methodCounter.getOrDefault(lhs_m, 0);
				for (int i=1;i<cnt;++i){
					ret = ret || solveAlias(lhs.addPrefix(REP_PREFIX+i), rhs.addPrefix(REP_PREFIX+i));
				}
				ret = ret || solveAlias(lhs.addPrefix(REP_PREFIX), rhs.addPrefix(REP_PREFIX));
				ret = ret || solveAlias(lhs, rhs);
				return ret;
			}
			Integer cnt = methodCounter.getOrDefault(lhs_m, 0);
			for (int i=1;i<cnt;++i){
				ret = ret || solveAlias(lhs.addPrefix(REP_PREFIX+i), rhs);
			}
			ret = ret || solveAlias(lhs.addPrefix(REP_PREFIX), rhs);
		}
		if (!rhs_m.startsWith(REP_PREFIX) && !rhs_m.startsWith(HEAP_PREFIX)) {
			Integer cnt = methodCounter.getOrDefault(rhs_m, 0);
			for (int i=1;i<cnt;++i){
				ret = ret || solveAlias(lhs, rhs.addPrefix(REP_PREFIX+i));
			}
			ret = ret || solveAlias(lhs, rhs.addPrefix(REP_PREFIX));
		}
		ret = ret || solveAlias(lhs, rhs);
		return ret;
	}

	public void addConstraint(Identifier dst, Identifier src) {
		ConstraintsList.add(new Constraint(dst, src));
	}

	public void solve(Map<String, Integer> mc) {
		this.methodCounter=mc;
		boolean modified = false;
		do{
			modified = false;
			for (Constraint c : ConstraintsList) {
				modified = modified || solveAliasEx(c.dst, c.src);
			}
		} while(modified);
	}
	
}

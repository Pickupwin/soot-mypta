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

	public final Map<String, Set<String>> pointTo = new HashMap<>();

	private final List<Constraint> ConstraintsList = new ArrayList<Constraint>();

	private boolean solveAlias(Identifier lhs, Identifier rhs){
		boolean ret = false;
		List<String> rhsList = rhs.genStringList(pointTo);
		if (lhs.fa == null){
			System.out.println(lhs.toString());
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

	public void addConstraint(Identifier dst, Identifier src) {
		// for (String ls: dst.genStringList(pointTo)) {
		// 	System.out.print(ls);
		// }
		// System.out.print("==");
		// for (String ls: src.genStringList(pointTo)) {
		// 	System.out.println(ls);
		// }
		ConstraintsList.add(new Constraint(dst, src));
	}

	public void solve() {
		boolean modified = false;
		do{
			modified = false;
			for (Constraint c : ConstraintsList) {
				modified = modified || solveAlias(c.dst, c.src);
			}
		} while(modified);
	}
	
}

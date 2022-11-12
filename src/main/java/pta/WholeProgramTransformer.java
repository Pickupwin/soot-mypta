package pta;

import java.util.Map;
import java.util.Map.Entry;

import java.util.List;

import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Set;
import java.util.HashSet;

import soot.Local;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AnyNewExpr;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.DefinitionStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ThrowStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.ParameterRef;
import soot.jimple.ThisRef;
import soot.jimple.ArrayRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NewExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.queue.QueueReader;

public class WholeProgramTransformer extends SceneTransformer {
	
	private final Set<String> doneMethod = new HashSet<>();

	private final Anderson solver = new Anderson();

	private static final String ARGS_PREFIX = "@args";
	private static final String HEAP_PREFIX = "@heap";
	private static final String RET_LOCAL = "@return";
	private static final String THIS_LOCAL = "@this";
	private static final String ELEM_PREFIX = "@elem";
	private static final String ARRAY_ALL = ELEM_PREFIX+"_all";
	private static final String ARRAY_ANY = ELEM_PREFIX+"_any";

	private static final Identifier STATIC_IDENTIFIER = new Identifier("Heap__");
	private static final Identifier THROW_IDENTIFIER = new Identifier(STATIC_IDENTIFIER, "@throw");

	private int allocId;
	private int maxAlloxId=-1;
	private boolean visitgg=false;

	private Identifier genIdentifier(Value v, String mSig){

		if (v instanceof AnyNewExpr) {
			int tmp=this.allocId;this.allocId=-1;	//TODO
			return new Identifier(HEAP_PREFIX+tmp);
		}
		if (v instanceof CastExpr) {
			return genIdentifier(((CastExpr) v).getOp(), mSig);
		}

		if (v instanceof InstanceFieldRef) {
			SootField f = ((InstanceFieldRef) v).getField();
			Value base = ((InstanceFieldRef) v).getBase();
			return new Identifier(genIdentifier(base, mSig), f.getSignature());
		}
		if (v instanceof StaticFieldRef) {
			SootField f = ((StaticFieldRef) v).getField();
			return new Identifier(STATIC_IDENTIFIER, f.getSignature());
		}

		if (v instanceof InvokeExpr) {
			// System.out.println("Invoke__  "+v.toString());
			InvokeExpr iv = (InvokeExpr) v;
			List<Value> args = iv.getArgs();
			String iSig = iv.getMethod().getSignature();
			// methodCounter[iSig]+=1;
			for (int i=0;i<args.size();++i) {
				Identifier dst = new Identifier(new Identifier(iSig), ARGS_PREFIX+i);
				Identifier src = genIdentifier(args.get(i), mSig);
				solver.addConstraint(dst, src);
			}
			if (iv instanceof InstanceInvokeExpr) {
				Identifier dst = new Identifier(new Identifier(iSig), THIS_LOCAL);
				Identifier src = genIdentifier(((InstanceInvokeExpr) v).getBase(), mSig);
				solver.addConstraint(dst, src);
			}
			return new Identifier(new Identifier(iSig), RET_LOCAL);
		}
		if (v instanceof Local) {
			return new Identifier(new Identifier(mSig), ((Local) v).getName());
		}
		if (v instanceof ParameterRef) {
			return new Identifier(new Identifier(mSig), ARGS_PREFIX+((ParameterRef) v).getIndex());
		}
		if (v instanceof ThisRef) {
			// System.out.println("This__  "+v.toString());
            return new Identifier(new Identifier(mSig), THIS_LOCAL);
        }
		if (v instanceof ArrayRef) {
			ArrayRef av = (ArrayRef) v;
			Identifier base = genIdentifier(av.getBase(), mSig);
			Value idx = av.getIndex();
			Identifier all = new Identifier(base, ARRAY_ALL);
			Identifier any = new Identifier(base, ARRAY_ANY);
			if (idx instanceof IntConstant) {
				Identifier item = new Identifier(base, ELEM_PREFIX+((IntConstant) idx).value);
				solver.addConstraint(item, any);
				solver.addConstraint(all, item);
				return item;
			}
			else{
				solver.addConstraint(all, any);
				return all;
			}
		}
		if (v instanceof CaughtExceptionRef) {
			return THROW_IDENTIFIER;
		}
		if (v instanceof IntConstant) {
			// System.out.println("IntConstant " + v.toString());
			return new Identifier(STATIC_IDENTIFIER, "@Constant"+v.toString());
		}
		// System.out.println(v.toString());
		// this.visitgg=true;
		return new Identifier("UNSUPPORTED");


	}



	private void doOverride(SootMethod m, SootClass c) {
		SootMethod mVir = c.getMethodUnsafe(m.getSubSignature());
		if (mVir!=null){
			String vSig = mVir.getSignature();
			String mSig = m.getSignature();
			
			Identifier lhs, rhs;

			lhs = new Identifier(new Identifier(vSig), RET_LOCAL);
			rhs = new Identifier(new Identifier(mSig), RET_LOCAL);
			// System.out.println(lhs.toString()+" == "+rhs.toString());
			solver.addConstraint(lhs, rhs);

			lhs = new Identifier(new Identifier(mSig), THIS_LOCAL);
			rhs = new Identifier(new Identifier(vSig), THIS_LOCAL);
			// System.out.println(lhs.toString()+" == "+rhs.toString());
			solver.addConstraint(lhs, rhs);

			int arg_len = m.getParameterCount();
			for (int i=0;i<arg_len;++i){
				lhs = new Identifier(new Identifier(mSig), ARGS_PREFIX+i);
				rhs = new Identifier(new Identifier(vSig), ARGS_PREFIX+i);
				// System.out.println(lhs.toString()+" == "+rhs.toString());
				solver.addConstraint(lhs, rhs);
			}
			checkOverride(mVir);
		}
		else{
			checkSuper(m, c);
		}
	}

	private void checkSuper(SootMethod m, SootClass c) {
		if(c.hasSuperclass())
			doOverride(m, c.getSuperclass());
		for (SootClass si: c.getInterfaces())
			doOverride(m, si);
	}
	
	private void checkOverride(SootMethod m) {
		if (doneMethod.add(m.getSignature())) {
			checkSuper(m, m.getDeclaringClass());
		}
	}


	@Override
	protected void internalTransform(String arg0, Map<String, String> arg1) {
		
		TreeMap<Integer, Identifier> queries = new TreeMap<Integer, Identifier>();
		
		ReachableMethods reachableMethods = Scene.v().getReachableMethods();
		QueueReader<MethodOrMethodContext> qr = reachableMethods.listener();		
		
		this.allocId = -1;

		while (qr.hasNext()) {
			SootMethod sm = qr.next().method();

			String mSig=sm.getSignature();
			if(mSig.startsWith("<java") || 
				mSig.startsWith("<sun") || 
				mSig.startsWith("<org") || 
				mSig.startsWith("<jdk"))
				continue;	//java, sun, org, jdk
			
			boolean isNonVirtual = sm.isConstructor() || sm.isStatic() || sm.isStaticInitializer() || sm.isPrivate();
			if(!isNonVirtual) checkOverride(sm);
			if (sm.hasActiveBody()) {
				for (Unit u : sm.getActiveBody().getUnits()) {
					if (u instanceof InvokeStmt) {
						InvokeExpr ie = ((InvokeStmt) u).getInvokeExpr();
						if (ie.getMethod().toString().equals("<benchmark.internal.BenchmarkN: void alloc(int)>")) {
							this.allocId = ((IntConstant)ie.getArgs().get(0)).value;
							if(this.maxAlloxId<this.allocId){
								this.maxAlloxId=this.allocId;
							}
							continue;
						}
						if (ie.getMethod().toString().equals("<benchmark.internal.BenchmarkN: void test(int,java.lang.Object)>")) {
							Value v = ie.getArgs().get(1);
							int id = ((IntConstant)ie.getArgs().get(0)).value;
							queries.put(id, genIdentifier(ie.getArgs().get(1), mSig));
							continue;
						}
						genIdentifier(((InvokeStmt) u).getInvokeExpr(), mSig);
						continue;
					}
					if (u instanceof DefinitionStmt) {
						DefinitionStmt du = (DefinitionStmt) u;
						Identifier lhs = genIdentifier(du.getLeftOp(), mSig);
						Identifier rhs = genIdentifier(du.getRightOp(), mSig);
						if (ARRAY_ALL.equals(lhs.name)) {
							Identifier lf = new Identifier(lhs.fa, ARRAY_ANY);
							solver.addConstraint(lf, rhs);
						}
						else {
							solver.addConstraint(lhs, rhs);
						}
						continue;
					}
					if (u instanceof ReturnStmt) {
						Identifier rhs = genIdentifier(((ReturnStmt) u).getOp(), mSig);
						solver.addConstraint(new Identifier(new Identifier(mSig), RET_LOCAL), rhs);
						continue;
					}
					if (u instanceof ThrowStmt) {
						Identifier rhs = genIdentifier(((ThrowStmt) u).getOp(), mSig);
						solver.addConstraint(THROW_IDENTIFIER, rhs);
						continue;
					}
				}
			}
		}
		
		solver.solve();

		String answer = "";
		for (Entry<Integer, Identifier> q : queries.entrySet()) {
			List<String> result = q.getValue().genStringList(solver.pointTo);
			answer += q.getKey().toString() + ":";
			if(this.visitgg){
				for (int i=1;i<=this.maxAlloxId;++i)
					answer += " " + i;
			}
			else{
				if (result != null) {
					for (String i : result) {
						Integer tmp=-1;
						if (i.startsWith(HEAP_PREFIX) && !i.contains(".")){
							try{
								tmp=Integer.parseInt(i.substring(HEAP_PREFIX.length()));
							}
							catch (Exception e){
								// e.printStackTrace();
							}
							if(tmp>=0){
								answer += " " + tmp;
							}
						}
						// answer += " " + i;
					}
				}
			}
			answer += "\n";
		}
		AnswerPrinter.printAnswer(answer);
		
	}

}

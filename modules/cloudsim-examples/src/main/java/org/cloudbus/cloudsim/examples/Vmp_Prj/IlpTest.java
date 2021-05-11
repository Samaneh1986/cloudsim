package org.cloudbus.cloudsim.examples.Vmp_Prj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.quantego.clp.CLP;
import com.quantego.clp.CLP.STATUS;
import com.quantego.clp.CLPConstraint.TYPE;
import com.quantego.clp.CLPExpression;
import com.quantego.clp.CLPVariable;
import com.quantego.clp.CLPVariableSet;

public class IlpTest {

	public static void main(String[] args) {
		// TODO Auto-generat
		
		
		CLP clp = new CLP().verbose(1);
		
		CLPVariable[] X = new CLPVariable[10]; 
		
		for(int i=0 ; i<10 ; i++){
			X[i] = clp.addVariable().obj(1); 
			clp.setVariableBounds(X[i], 0, 1);
		}
	/*	for(int i=0 ; i<10 ; i++){
			clp.createExpression().add(1,X[i]).geq(0); 
		}
		for(int i=0 ; i<10 ; i++){
			clp.createExpression().add(1,X[i]).leq(1); 
		}*/
		CLPExpression expr1 = clp.createExpression();
		for(int i=0 ; i<10 ; i++){
			expr1.add(1,X[i]); 
		}
		expr1.geq(3.6);
		STATUS result = clp.minimize(); 
		for(int i=0 ; i<10 ; i++)
			System.out.println(X[i].getSolution());
	}
	
}

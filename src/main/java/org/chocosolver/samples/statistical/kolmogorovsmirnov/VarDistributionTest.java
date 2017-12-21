package org.chocosolver.samples.statistical.kolmogorovsmirnov;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactorySt;
import org.chocosolver.solver.constraints.statistical.kolmogorovsmirnov.distributions.ExponentialDistVar;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

public class VarDistributionTest extends AbstractProblem {

    // input data
    int[] dataX = {	4, 5, 33, 9, 11, 9, 3, 17, 80, 7, 3, 12, 15, 21, 5, 6, 2, 27, 77, 1, 
    		52, 25, 8, 3, 6, 6, 23, 22, 4, 58, 1, 4, 9, 2, 74, 15, 5, 46, 78, 23, 
    		20, 13, 21, 7, 2, 8, 60, 6, 13, 5, 25, 33, 1, 28, 12, 4, 8, 11, 15, 
    		4, 44, 24, 21, 51, 24, 9, 2, 5, 11, 99, 13, 19, 23, 24, 10, 18, 60, 
    		8, 20, 4, 33, 1, 2, 1, 21, 29, 59, 6, 27, 9, 30, 7, 43, 10, 5, 11, 
    		15, 45, 55, 39, 93, 13, 28, 5, 39, 5, 12, 19, 27, 32, 9, 7, 3, 5, 49, 
    		34, 4, 12, 51, 6, 8, 11, 28, 1, 5, 22, 15, 3, 33, 1, 13, 4, 6, 85, 
    		10, 2, 1, 19, 20, 8, 16, 22, 12, 36, 42, 17, 16, 5, 7, 34, 21, 9, 8, 
    		5, 1, 8, 1, 2, 16, 20, 9, 1, 10, 5, 26, 5, 8, 13, 32, 10, 4, 4, 24, 
    		27, 20, 38, 20, 20, 4, 43, 4, 27, 8, 1, 32, 19, 15, 3, 33, 1, 39, 17, 
    		26, 42, 25, 10, 4, 6, 26, 17, 38, 4, 8, 60, 18, 47, 32, 52, 21, 70, 
    		1, 62, 44, 50, 14, 4, 21, 35, 16, 13}; //Exponential[5]
	              //{9, 3, 7, 8, 8, 5, 8, 5, 3, 6}; //Poisson[7]

    // variables
    public IntVar[] populationX;
    public IntVar lambda;

    public void setUp() {
        // read data
    }

    @Override
    public void createSolver() {
        solver = new Solver("VarDistributionTest");
    }

    @Override
    public void buildModel() {
        setUp();
        int populationXSize = dataX.length;
        populationX = new IntVar[populationXSize];
        for(int i = 0; i < populationXSize; i++)
        	populationX[i] = VariableFactory.bounded("sample "+i, dataX[i], dataX[i], solver);

        lambda = VariableFactory.bounded("lambda", 1, 40, solver);
        
        solver.post(IntConstraintFactorySt.arithmSt(populationX, new ExponentialDistVar(lambda), "=", 0.95));
    }
    
    private static IntVar[] mergeArrays(IntVar[] var1, IntVar[] var2){
    	IntVar[] var3 = new IntVar[var1.length+var2.length];
    	System.arraycopy(var1, 0, var3, 0, var1.length);
    	System.arraycopy(var2, 0, var3, var1.length, var2.length);
    	return var3;
    }

    @Override
    public void configureSearch() {
        AbstractStrategy<IntVar> strat = IntStrategyFactory.domOverWDeg(mergeArrays(populationX,new IntVar[]{lambda}),2211);
        // trick : top-down maximization
        solver.set(strat);
    }

    @Override
    public void solve() {
    	StringBuilder st = new StringBuilder();
    	boolean solution = solver.findSolution();
    	do{
    		if(solution) {
    			/*for(int i = 0; i < populationX.length; i++){
    				st.append(populationX[i].getValue()+", ");
    			}*/
    			st.append(lambda.getValue()+", ");
    		}else{
    			st.append("No solution!");
    		}
    	}while(solution = solver.nextSolution());
    	System.out.println(st.toString());
    }

    @Override
    public void prettyOut() {
        
    }

    public static void main(String[] args) {
    	String[] str={"-log","SILENT"};
        new VarDistributionTest().execute(str);
    }
}


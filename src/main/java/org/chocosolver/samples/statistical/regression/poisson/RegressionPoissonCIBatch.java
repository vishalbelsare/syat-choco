/*
 * syat-choco: a Choco extension for Declarative Statistics.
 * 
 * MIT License
 * 
 * Copyright (c) 2016 Roberto Rossi
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.chocosolver.samples.statistical.regression.poisson;

import java.util.Random;
import java.util.stream.DoubleStream;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.constraints.statistical.chisquare.ChiSquareFitPoisson;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.probdist.PoissonDist;

public class RegressionPoissonCIBatch extends AbstractProblem {
   
   public RealVar slope;
   public RealVar quadratic;
   public RealVar poissonRate;
   
   public RealVar[] residual;
   public IntVar[] binVariables;
   public RealVar[] realBinViews;
   
   double[] observations;
   double[] binBounds;
   double significance;
   
   public RegressionPoissonCIBatch(double[] observations,
                                      double[] binBounds,
                                      double significance){
      this.observations = observations;
      this.binBounds = binBounds.clone();
      this.significance = significance;
   }
   
   RealVar chiSqStatistics;
   RealVar[] allRV;
   
   double precision = 0.0001;
   
   ChiSquareDist chiSqDist;
   
   @Override
   public void createSolver() {
       solver = new Solver("Regression");
   }
   
   @Override
   public void buildModel() {
      slope = VariableFactory.real("Slope", trueSlope-precision, trueSlope+precision, precision, solver);
      quadratic = VariableFactory.real("Quadratic", trueQuadratic-precision, trueQuadratic+precision, precision, solver);
      poissonRate = VariableFactory.real("Rate", truePoissonRate-precision, truePoissonRate+precision, precision, solver);
      
      residual = new RealVar[this.observations.length];
      for(int i = 0; i < this.observations.length; i++)
         residual[i] = VariableFactory.real("Residual "+(i+1), 0, this.binBounds[this.binBounds.length-2], precision, solver);
      
      for(int i = 0; i < this.residual.length; i++){
         String residualExp = "{0}="+this.observations[i]+"-{1}*"+(i+1.0)+"-"+(i+1.0)+"^{2}";
         solver.post(new RealConstraint("residual "+i,
               residualExp,
               Ibex.HC4_NEWTON, 
               new RealVar[]{residual[i],slope,quadratic}
               ));
      }
      
      binVariables = new IntVar[this.binBounds.length-1];
      for(int i = 0; i < this.binVariables.length; i++)
         binVariables[i] = VariableFactory.bounded("Bin "+(i+1), 0, this.observations.length, solver);
      
      this.chiSqDist = new ChiSquareDist(this.binVariables.length-1);
      
      chiSqStatistics = VF.real("chiSqStatistics", 0, this.chiSqDist.inverseF(1-significance), precision, solver);
      ChiSquareFitPoisson.decomposition("chiSqTest", residual, binVariables, binBounds, poissonRate, chiSqStatistics, precision, false);
   }
   
   @Override
   public void configureSearch() {
      
      solver.set(
            new RealStrategy(new RealVar[]{slope,quadratic,poissonRate}, new Cyclic(), new RealDomainMiddle()),
            new RealStrategy(residual, new Cyclic(), new RealDomainMiddle()),
            IntStrategyFactory.activity(binVariables,1234)
       );
       SearchMonitorFactory.limitTime(solver,5000);
   }
   
   @Override
   public void solve() {
     StringBuilder st = new StringBuilder();
     boolean solution = solver.findSolution();
     //do{
        st.append("---\n");
        if(solution) {
           st.append(slope.toString()+", "+quadratic.toString()+", "+poissonRate.toString()+"\n");
           for(int i = 0; i < residual.length; i++){
              st.append(residual[i].toString()+", ");
           }
           st.append("\n");
           for(int i = 0; i < binVariables.length; i++){
              st.append(binVariables[i].toString()+", ");
           }
           st.append("\n");
           st.append(chiSqStatistics.getLB()+" "+chiSqStatistics.getUB());
           st.append("\n");
           feasibleCount++;
        }else{
           st.append("No solution!");
        }
     //}while(solution = solver.nextSolution());
     System.out.println(st.toString());
   }

   @Override
   public void prettyOut() {
       
   }
   
   static double truePoissonRate = 10;
   static double trueSlope = 1;
   static double trueQuadratic = 0.5;
   
   public static double[] generateObservations(Random rnd, int nbObservations){
      PoissonDist dist = new PoissonDist(truePoissonRate);
      return DoubleStream.iterate(1, i -> i + 1).map(i -> trueSlope*i+Math.pow(i, trueQuadratic)).map(i -> i + dist.inverseF(rnd.nextDouble())).limit(nbObservations).toArray();
   }
   
   static int feasibleCount = 0;
   
   public static void coverageProbability(){
      String[] str={"-log","SOLUTION"};
      
      Random rnd = new Random(1234);
      
      int nbObservations = 50;
      
      double replications = 1000;
      
      for(int k = 0; k < replications; k++){
         double[] observations = generateObservations(rnd, nbObservations);
         //Arrays.stream(observations).forEach(a -> System.out.print(a+"\t"));
         int bins = 20;
         double[] binBounds = DoubleStream.iterate(0, i -> i + 2).limit(bins).toArray();                                 
         double significance = 0.05;
      
         RegressionPoissonCIBatch regression = new RegressionPoissonCIBatch(observations, binBounds, significance);
         regression.execute(str);
         try {
            regression.finalize();
         } catch (Throwable e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
         }
         regression = null;
         System.gc();
         
         System.out.println(feasibleCount/(k+1.0) + "(" + k + ")");
         try {
            Thread.sleep(500);
         } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      System.out.println(feasibleCount/replications);
   }
   
   public static void main(String[] args) {
      
      coverageProbability();
      
   }
}

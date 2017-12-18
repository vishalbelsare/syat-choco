package org.chocosolver.solver.constraints.statistical.chisquare.test;

import static org.junit.Assert.*;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.statistical.chisquare.ChiSquareIndependence;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.RealStrategyFactory;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;

public class ChiSquareIndependenceTest {

   @Before
   public void setUp() throws Exception {
   }

   @After
   public void tearDown() throws Exception {
      Thread.sleep(2000);
      System.gc();
   }

   @Test
   public void testInteger() {
      String[] str={"-log","SOLUTION"};
      
      int[][] valuesA = {{3},{3},{1},{1},{2}};
      int[][] valuesB = {{1},{1},{2},{2},{3}};
      int[] binCounts = {0,valuesA.length};
      int[][] binBounds = {{1,2,3,4},{1,2,3,4}};
      double confidence = 0.95;
      double chiSqUB = new ChiSquareDist((binBounds[0].length-1)*(binBounds[1].length-1)).inverseF(confidence);
      
      double[] chiSqStatistic = {0,chiSqUB};
      
      ChiSquareIndependenceInteger cs = new ChiSquareIndependenceInteger(valuesA, valuesB, binCounts, binBounds, chiSqStatistic);
      cs.execute(str);
   }
   
   @Test
   public void testReal() {
      String[] str={"-log","SOLUTION"};
      
      double[][] valuesA = {{3,3},{3,3},{1,1},{1,1},{2,2}};
      double[][] valuesB = {{1,1},{1,1},{2,2},{2,2},{3,3}};
      double[][] binBounds = {{0.5,1.5,2.5,3.5},{0.5,1.5,2.5,3.5}};
      double confidence = 0.95;
      double chiSqUB = new ChiSquareDist((binBounds[0].length-1)*(binBounds[1].length-1)).inverseF(confidence);
      
      double[] chiSqStatistic = {0,chiSqUB};
      
      ChiSquareIndependenceReal cs = new ChiSquareIndependenceReal(valuesA, valuesB, binBounds, chiSqStatistic);
      cs.execute(str);
   }

   class ChiSquareIndependenceInteger extends AbstractProblem {
      public IntVar[] seriesA;
      public IntVar[] seriesB;
      public IntVar[][] binVariables;
      IntVar[] marginalsH; 
      IntVar[] marginalsV; 
      public RealVar chiSqstatisticVariable;
      
      int[][] valuesA;
      int[][] valuesB;
      int[] binCounts;
      int[][] binBounds;
      
      double[] chiSqStatistic;
      
      double precision = 1.e-4;
      
      public ChiSquareIndependenceInteger(int[][] valuesA,
                                          int[][] valuesB, 
                                          int[] binCounts,
                                          int[][] binBounds,
                                          double[] chiSqStatistic){
         this.valuesA = valuesA.clone();
         this.valuesB = valuesB.clone();
         this.binCounts = binCounts.clone();
         this.binBounds = binBounds.clone();
         this.chiSqStatistic = chiSqStatistic.clone();
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("ChiSquareInteger");
      }
      
      @Override
      public void buildModel() {
         seriesA = new IntVar[this.valuesA.length];
         for(int i = 0; i < this.valuesA.length; i++)
            seriesA[i] = VariableFactory.enumerated("Value A"+(i+1), valuesA[i], solver);
         
         seriesB = new IntVar[this.valuesB.length];
         for(int i = 0; i < this.valuesB.length; i++)
            seriesB[i] = VariableFactory.enumerated("Value B"+(i+1), valuesB[i], solver);
         
         chiSqstatisticVariable = VF.real("chiSqStatistics", chiSqStatistic[0], chiSqStatistic[1], precision, solver);
         
         ChiSquareIndependence.decomposition("chiSqConstraint", seriesA, seriesB, this.binBounds, chiSqstatisticVariable, precision, false);
      }
      
      @Override
      public void configureSearch() {  
         /*solver.set(
               IntStrategyFactory.activity(seriesA,1234),
               IntStrategyFactory.activity(seriesB,1234),
               RealStrategyFactory.cyclic_middle(new RealVar[]{chiSqstatisticVariable})
               );*/
      }
      
      @Override
      public void solve() {
        StringBuilder st = new StringBuilder();
        boolean solution = solver.findSolution();
        //do{
           st.append("---\n");
           if(solution) {
              
              assertTrue(chiSqstatisticVariable.getLB() <= 10+precision);
              assertTrue(chiSqstatisticVariable.getUB() >= 10-precision);
              
              st.append(chiSqstatisticVariable.getLB()+" "+chiSqstatisticVariable.getUB());
           }else{
              st.append("No solution!");
              assertTrue(false);
           }
        //}while(solution = solver.nextSolution());
        System.out.println(st.toString());
      }
      
      @Override
      public void prettyOut() {
          
      }
   }
   
   class ChiSquareIndependenceReal extends AbstractProblem {
      public RealVar[] seriesA;
      public RealVar[] seriesB;
      public IntVar[][] binVariables;
      IntVar[] marginalsH; 
      IntVar[] marginalsV; 
      public RealVar chiSqstatisticVariable;
      
      double[][] valuesA;
      double[][] valuesB;
      double[][] binBounds;
      
      double[] chiSqStatistic;
      
      double precision = 1.e-4;
      
      public ChiSquareIndependenceReal(double[][] valuesA,
                                       double[][] valuesB, 
                                       double[][] binBounds,
                                       double[] chiSqStatistic){
         this.valuesA = valuesA.clone();
         this.valuesB = valuesB.clone();
         this.binBounds = binBounds.clone();
         this.chiSqStatistic = chiSqStatistic.clone();
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("ChiSquareReal");
      }
      
      @Override
      public void buildModel() {
         seriesA = new RealVar[this.valuesA.length];
         for(int i = 0; i < this.valuesA.length; i++)
            seriesA[i] = VariableFactory.real("Value A"+(i+1), valuesA[i][0], valuesA[i][1], precision, solver);
         
         seriesB = new RealVar[this.valuesB.length];
         for(int i = 0; i < this.valuesB.length; i++)
            seriesB[i] = VariableFactory.real("Value B"+(i+1), valuesB[i][0], valuesB[i][1], precision, solver);
         
         chiSqstatisticVariable = VF.real("chiSqStatistics", chiSqStatistic[0], chiSqStatistic[1], precision, solver);
         
         ChiSquareIndependence.decomposition("chiSqConstraint", seriesA, seriesB, this.binBounds, chiSqstatisticVariable, precision, false);
      }
      
      @Override
      public void configureSearch() {    
         /*solver.set(
               RealStrategyFactory.cyclic_middle(seriesA),
               RealStrategyFactory.cyclic_middle(seriesB),
               RealStrategyFactory.cyclic_middle(new RealVar[]{chiSqstatisticVariable})
               );*/
      }
      
      @Override
      public void solve() {
        StringBuilder st = new StringBuilder();
        boolean solution = solver.findSolution();
        //do{
           st.append("---\n");
           if(solution) {

              assertTrue(chiSqstatisticVariable.getLB() <= 10+precision);
              assertTrue(chiSqstatisticVariable.getUB() >= 10-precision);
              
              st.append(chiSqstatisticVariable.getLB()+" "+chiSqstatisticVariable.getUB());
           }else{
              st.append("No solution!");
              assertTrue(false);
           }
        //}while(solution = solver.nextSolution());
        System.out.println(st.toString());
      }
      
      @Override
      public void prettyOut() {
          
      }
   }
}

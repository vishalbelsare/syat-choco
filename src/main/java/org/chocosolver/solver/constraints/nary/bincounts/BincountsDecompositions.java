package org.chocosolver.solver.constraints.nary.bincounts;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactorySt;
import org.chocosolver.solver.constraints.LogicalConstraintFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

public class BincountsDecompositions {
   /**
    * Bincounts decomposition (Rossi, 2016)
    * 
    * BEWARE : it is automatically posted (it cannot be reified)
    * 
    * @param valueVariables
    * @param binVariables
    * @param binBounds
    */
   public static void bincountsDecomposition1(IntVar[] valueVariables, IntVar[] binVariables, int[] binBounds){
      Solver solver = valueVariables[0].getSolver();
      
      IntVar[] valueOccurrenceVariables = new IntVar[binBounds[binBounds.length-1]-binBounds[0]];
      int[] valuesArray = new int[valueOccurrenceVariables.length];
      for(int i = 0; i < valueOccurrenceVariables.length; i++){
         valueOccurrenceVariables[i] = VariableFactory.bounded("Value Occurrence "+i, 0, valueVariables.length, solver);
         valuesArray[i] = i + binBounds[0];
      }
      
      for(int i = 0; i < binBounds.length - 1; i++){
         IntVar[] binOccurrences = new IntVar[binBounds[i+1]-binBounds[i]];
         System.arraycopy(valueOccurrenceVariables, binBounds[i]-binBounds[0], binOccurrences, 0, binBounds[i+1]-binBounds[i]);
         solver.post(IntConstraintFactorySt.sum(binOccurrences, binVariables[i]));
      }
      
      solver.post(IntConstraintFactorySt.sum(binVariables, VariableFactory.fixed(valueVariables.length, solver)));
      
      solver.post(IntConstraintFactorySt.global_cardinality(valueVariables, valuesArray, valueOccurrenceVariables, true));
   }
   
   /**
    * Bincounts decomposition (Rossi, 2016) GCC replaced by a bincounts for sanity check
    * 
    * BEWARE : it is automatically posted (it cannot be reified)
    * 
    * @param valueVariables
    * @param binVariables
    * @param binBounds
    */
   public static void bincountsDecomposition1a(IntVar[] valueVariables, IntVar[] binVariables, int[] binBounds){
      Solver solver = valueVariables[0].getSolver();
      
      IntVar[] valueOccurrenceVariables = new IntVar[binBounds[binBounds.length-1]-binBounds[0]];
      int[] valuesArray = new int[binBounds[binBounds.length-1]-binBounds[0]+1];
      for(int i = 0; i < valueOccurrenceVariables.length; i++){
         valueOccurrenceVariables[i] = VariableFactory.bounded("Value Occurrence "+i, 0, valueVariables.length, solver);
         valuesArray[i] = i + binBounds[0];
      }
      valuesArray[valuesArray.length-1] = binBounds[binBounds.length-1];
      
      for(int i = 0; i < binBounds.length - 1; i++){
         IntVar[] binOccurrences = new IntVar[binBounds[i+1]-binBounds[i]];
         System.arraycopy(valueOccurrenceVariables, binBounds[i]-binBounds[0], binOccurrences, 0, binBounds[i+1]-binBounds[i]);
         solver.post(IntConstraintFactorySt.sum(binOccurrences, binVariables[i]));
      }
      
      solver.post(IntConstraintFactorySt.sum(binVariables, VariableFactory.fixed(valueVariables.length, solver)));
      
      solver.post(IntConstraintFactorySt.bincounts(valueVariables, valueOccurrenceVariables, valuesArray, BincountsPropagatorType.EQFast));
   }
   
   /**
    * Bincounts decomposition (Agkun, 2016) first decomposition 
    * 
    * BEWARE : it is automatically posted (it cannot be reified)
    * 
    * @param valueVariables
    * @param binVariables
    * @param binBounds
    */
   public static void bincountsDecomposition2(IntVar[] valueVariables, IntVar[] binVariables, int[] binBounds){
      Solver solver = valueVariables[0].getSolver();
      
      IntVar[] valueOccurrenceVariables = new IntVar[valueVariables.length];
      for(int i = 0; i < valueOccurrenceVariables.length; i++){
         valueOccurrenceVariables[i] = VariableFactory.bounded("Value-Bin "+i, 0, binBounds.length - 2, solver);
         for(int j = 0; j < binBounds.length - 1; j++){
            solver.post(LogicalConstraintFactory.ifThen_reifiable(
                  IntConstraintFactorySt.arithm(valueOccurrenceVariables[i], "=", j), 
                  LogicalConstraintFactory.and(
                        IntConstraintFactorySt.arithm(valueVariables[i], ">=", binBounds[j]),
                        IntConstraintFactorySt.arithm(valueVariables[i], "<", binBounds[j+1])
                        )));
            
            solver.post(LogicalConstraintFactory.ifThen_reifiable( 
                  LogicalConstraintFactory.and(
                        IntConstraintFactorySt.arithm(valueVariables[i], ">=", binBounds[j]),
                        IntConstraintFactorySt.arithm(valueVariables[i], "<", binBounds[j+1])
                        ),
                        IntConstraintFactorySt.arithm(valueOccurrenceVariables[i], "=", j)
                  ));
         }
      }
      
      int[] bins = new int[binBounds.length-1];
      for(int k = 0; k < binBounds.length - 1; k++) bins[k] = k;
      
      solver.post(IntConstraintFactorySt.global_cardinality(valueOccurrenceVariables, bins, binVariables, true));
   }
   
   /**
    * Bincounts decomposition (Agkun, 2016) second decomposition 
    * 
    * BEWARE : it is automatically posted (it cannot be reified)
    * 
    * @param valueVariables
    * @param binVariables
    * @param binBounds
    */
   public static void bincountsDecomposition3(IntVar[] valueVariables, IntVar[] binVariables, int[] binBounds){
      Solver solver = valueVariables[0].getSolver();
      
      for(int j = 0; j < binBounds.length - 1; j++){
         BoolVar[] valueOccurrenceVariables = new BoolVar[valueVariables.length];
         for(int i = 0; i < valueOccurrenceVariables.length; i++){
            valueOccurrenceVariables[i] = VariableFactory.bool("Value-Bin "+i+" "+j, solver);
            
            solver.post(LogicalConstraintFactory.reification_reifiable(
                  valueOccurrenceVariables[i], 
                  LogicalConstraintFactory.and(
                        IntConstraintFactorySt.arithm(valueVariables[i], ">=", binBounds[j]),
                        IntConstraintFactorySt.arithm(valueVariables[i], "<", binBounds[j+1])
                        )));
            
         }
         solver.post(IntConstraintFactorySt.sum(valueOccurrenceVariables, binVariables[j]));
      }
   }
}

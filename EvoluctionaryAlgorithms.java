package br.unicamp.ft.mra.reordering;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Random;
import br.unicamp.ft.mra.ReorderableMatrix;
import br.unicamp.ft.mra.evaluation.StressNeighborhoodMoore;

public class EvoluctionaryAlgorithms extends AbstractMatrixReorderingAlgorithm {
    
    private double mutationRate; 
    private Integer numIndividuals = 20;
    private double originalValue;
    private List<ReorderableMatrix> individuals = new ArrayList<ReorderableMatrix>();

    public EvoluctionaryAlgorithms(ReorderableMatrix originalMatrix) {
        super(originalMatrix);
    }

    public EvoluctionaryAlgorithms() {
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public ReorderableMatrix getIndividuals() {

        ReorderableMatrix matrix = new ReorderableMatrix(originalMatrix);
        ReorderableMatrix matrix_dois = new ReorderableMatrix(originalMatrix);

        // Permutation;
       matrix.shuffle();
       
       return matrix;
    }

    /* Metodos para teste [Imprime a Ordem das Colunas] */
    public void ShowOrderColumn(ReorderableMatrix matriz){
       
        int i, j, k;
        
        for(i = 0; i < matriz.getNumberOfColumns(); i++){
            System.out.print(matriz.getColumnLabels(i)+", ");
        }
    }
    
    public List<ReorderableMatrix> mutation(List<ReorderableMatrix> individuals) {
       
        mutationRate=0.5;
        
        Random chooseMutation = new Random(System.currentTimeMillis());
        int mutationRange = (int) Math.round(mutationRate * numIndividuals);
        int i, j; // cont
        int b1, b2, max, min; // points for flip in vector.
        int half; // auxiliary for vector points.
                
        Collections.shuffle(individuals);
        
        for (i = 0; i < mutationRange; i++) {
           
            if (chooseMutation.nextInt(2) == 1) {
                b1 = chooseMutation.nextInt(individuals.get(i).getNumberOfRows());
                b2 = chooseMutation.nextInt(individuals.get(i).getNumberOfRows());
                
                if(b1>b2){
                    max = b1;
                    min = b2;
                } else {
                    max = b2;
                    min = b1;
                }
                
                half = (int) (max - (min - 1))/2;
                
                for(j=0; j< half; j++){
                    b1 = max - j;
                    b2 = min + j;
                                      
                    individuals.get(i).permuteRow(b1, b2);
                }
                
            } else {
                b1 = chooseMutation.nextInt(individuals.get(i).getNumberOfColumns());
                b2 = chooseMutation.nextInt(individuals.get(i).getNumberOfColumns());
                
                if(b1>b2){
                    max = b1;
                    min = b2;
                } else {
                    max = b2;
                    min = b1;
                }
                
                 half = (int) (max - (min - 1))/2;
                
                for(j=0; j< half; j++){
                    b1 = max - j;
                    b2 = min + j;
                    
                    individuals.get(i).permuteColumn(b1, b2);
                }
            }
        }
         return individuals;
    }
    
    public List<ReorderableMatrix> crossOver(List<ReorderableMatrix> individuals){
        
        int pai1, pai2;
        int auxiliar[] = null;   
        
        Random parents = new Random(System.currentTimeMillis());
        
        do{
            pai1 = parents.nextInt(numIndividuals);
            pai2 = parents.nextInt(numIndividuals);
        }while(pai1 == pai2);
        
        
        /* Copy index values to auxiliar */
        auxiliar = individuals.get(pai1).getColumnOrder();
         
        /* Set the  columns values of the father 1 to columns values of the father 2 */
        individuals.get(pai1).setColumnOrder(individuals.get(pai2).getColumnOrder());
        individuals.get(pai2).setColumnOrder(auxiliar);
               
        return individuals;   
    }
    
    public ReorderableMatrix getBestIndividual(List<ReorderableMatrix> individuals){
                
        int i;
        double bestValue = Double.POSITIVE_INFINITY, value;
        ReorderableMatrix bestMatrix = null;
        StressNeighborhoodMoore stress = new StressNeighborhoodMoore();
             
        for(i=0; i< individuals.size(); i++){
            // Calculate the stress value of all the matrixs
            value = stress.evaluate(individuals.get(i));
              
            if(bestValue > value){
                bestValue = value;
                bestMatrix = individuals.get(i);
            }
        }
        return bestMatrix;
    }
    
    public ReorderableMatrix tournaments(List<ReorderableMatrix> individuals){
        
        int i;
        int indOne, indTwo;
        double valueOne, valueTwo;
        ReorderableMatrix bestMatrix = null;
        Random chooseIndividual = new Random(System.currentTimeMillis());
        StressNeighborhoodMoore stress = new StressNeighborhoodMoore();
        
        for(i = 0; i < numIndividuals; i++){
            
            do{
                indOne = chooseIndividual.nextInt(numIndividuals);
                indTwo = chooseIndividual.nextInt(numIndividuals);
            }while(indOne == indTwo);
            
            valueOne = stress.evaluate(individuals.get(indOne));
            valueTwo = stress.evaluate(individuals.get(indTwo));
            
            if(valueOne > valueTwo){
                bestMatrix = individuals.get(indTwo);
            }else{
                bestMatrix = individuals.get(indOne);
            }
        }
        
        return bestMatrix;
    }
        
    private int getBinomial(int n, double p) {
        int x=0;
        for (int i=0; i<n; i++) {
            if (Math.random() < p) x++;
        }
        return x;
    }
    
    @Override
    public ReorderableMatrix sort(){
        
        int geracao;
        int i, j, k;
        ReorderableMatrix bestMatrix, finalMatrix, goodMatrix = null, auxMatrix = null;
        List<ReorderableMatrix> geracaoList = new ArrayList<ReorderableMatrix>();
        List<ReorderableMatrix> goodList = new ArrayList<ReorderableMatrix>();

        double value = Double.POSITIVE_INFINITY;
        double goodValue = Double.POSITIVE_INFINITY;
        double auxValue = Double.POSITIVE_INFINITY;
        double finalValue = Double.POSITIVE_INFINITY;
        StressNeighborhoodMoore stress = new StressNeighborhoodMoore();

        originalValue = stress.evaluate(originalMatrix);

        for (i = 0; i < this.numIndividuals; i++) {
            individuals.add(i, getIndividuals());
        }

        geracao = 0;
        do {
            int qtde = getBinomial(20, .5);

            for (k = 0; k < qtde; k++) {
                individuals = crossOver(individuals);
            }

            individuals = mutation(individuals);

            auxMatrix = getBestIndividual(individuals);
            auxValue = stress.evaluate(auxMatrix);// Para Testes

            goodList.add(geracao, auxMatrix);
            geracaoList.clear();

            for (j = 0; j < numIndividuals; j++) {
                geracaoList.add(j, tournaments(individuals));
            }

            individuals.clear();
            individuals.addAll(geracaoList);

            /* Teste para terminal */
            System.out.println("Geração: " + geracao + ", Stress: " + auxValue);
            geracao++;

            finalMatrix = getBestIndividual(geracaoList);
            goodMatrix = getBestIndividual(goodList);

            finalValue = stress.evaluate(finalMatrix);
            goodValue = stress.evaluate(goodMatrix);

            if (goodValue < finalValue) {
                bestMatrix = goodMatrix;
                value = goodValue;
            } else {
                bestMatrix = finalMatrix;
                value = finalValue;
            }
        } while (value > (originalValue / 2));

        System.out.println("valor de estresse:\n Boa: " + goodValue + "; Final: " + finalValue + "; Original: " + originalValue);
        return bestMatrix;

    }

    @Override
    public String getAlgorithmName() {
        return "Evoluctionary Algorithms";
    }

    public String getShortAlgorithmName() {
        return "Evolution";
    }
}

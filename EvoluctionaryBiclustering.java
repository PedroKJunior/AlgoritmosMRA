package br.unicamp.ft.mra.reordering;

import br.unicamp.ft.mra.ReorderableMatrix;
import br.unicamp.ft.mra.evaluation.StressNeighborhoodMoore;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EvoluctionaryBiclustering extends AbstractMatrixReorderingAlgorithm {    
        
    private final Integer population = 300;
    private List<ReorderableMatrix> individuals = new ArrayList<ReorderableMatrix>();
    private StressNeighborhoodMoore stress = new StressNeighborhoodMoore();
    private double[] valueOffSpring = new double[population]; //Crossover range value;
    private int[] generation = new int[population]; // List dos Pais;
    
               
    public EvoluctionaryBiclustering(ReorderableMatrix originalMatrix){
        super(originalMatrix);
    }

    public EvoluctionaryBiclustering() {
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
    public ReorderableMatrix getIndividuals() {

       ReorderableMatrix matrix = new ReorderableMatrix(originalMatrix);
        
       // Permutation;
       matrix.shuffle();
       
       return matrix;
    }
    
    // Gera a range que cada matriz pode ser escolhida para mutação
    public void rateMutation(List<ReorderableMatrix> individuals){
        
        double stressRange; // valor invertido do stress
        double sumStress = 0; // Soma de todos os stress
                
        for(int i = 0; i < population; i++){
            
            stressRange = stress.evaluate(individuals.get(i));
            sumStress += stressRange;
            valueOffSpring[i] = stressRange;
        }
               
        for(int j = 0; j<population; j++){
           
            valueOffSpring[j] = valueOffSpring[j]/sumStress;                 
            
            if(j == 0){
                valueOffSpring[j] += 0; 
            } else {
                valueOffSpring[j] += valueOffSpring[j-1];
            }
        }       
    }
 
    // Insere o ponto b2 antes do ponto b1
    public ReorderableMatrix insertionMutation(int b1, int b2, boolean permutRow, ReorderableMatrix matrix){
                
        if(permutRow){    
            for(int i = b2; i>b1; i--){
                matrix.permuteRow(i, i-1);
            }
        }else{
            for(int i = b2; i>b1; i--){
                matrix.permuteColumn(i, i-1);
            }
        }
        
        return matrix;
    }
    
    // Permutação simples
    public ReorderableMatrix exchangedMutation(int b1, int b2, boolean permutRow, ReorderableMatrix matrix){
                
        if(permutRow){    
            matrix.permuteRow(b1, b2);
        }else{
            matrix.permuteColumn(b1, b2);
        }
        
        return matrix;
    }
   
    // Permutação por flip
    public ReorderableMatrix crossExchangeMutation(int b1, int b2, boolean permutRow, ReorderableMatrix matrix){
        
        int max = b1, min = b2;
        
        int half = (int) (b1 - (b2 - 1))/2;
        if(permutRow){
            for(int i=0; i< half; i++){
                b1 = max - i;
                b2 = min + i;
                                      
                matrix.permuteRow(b1, b2);
                }
        }else{
            for(int i=0; i< half; i++){
                b1 = max - i;
                b2 = min + i;
                                   
                matrix.permuteColumn(b1, b2);
            }
        }

        return matrix;
    }
    
    // Escolhe os indices e o tipo de mutação e se usará linha ou coluna
    public List<ReorderableMatrix> mutation(List<ReorderableMatrix> individuals){
        
        Random random = new Random(System.currentTimeMillis());
        boolean permutRow;
        double range;
        int b1, b2, max, min; // points for flip in vector.
        int roleta;
                        
        for(int i=0; i<population; i++){
           
            range = random.nextDouble();
            
            for(int j=0; j<population; j++){
                if(range <= valueOffSpring[j]){
                    if(range > valueOffSpring[Math.max(0, j-1)]){
                        
                        roleta = random.nextInt(3);
                        if(random.nextInt(2) == 1){
                            permutRow = true;

                            b1 = random.nextInt(individuals.get(j).getNumberOfRows());
                            b2 = random.nextInt(individuals.get(j).getNumberOfRows());

                            if(b1>b2){
                                max = b1;
                                min = b2;
                            } else {
                                max = b2;
                                min = b1;
                            }            

                            if( roleta == 1)
                                insertionMutation(max, min, permutRow, individuals.get(j));
                            else if(roleta == 2)
                                exchangedMutation(max, min, permutRow, individuals.get(j));
                            else
                                crossExchangeMutation(max, min, permutRow, individuals.get(j));
                        } else {
                            permutRow = false;

                            b1 = random.nextInt(individuals.get(j).getNumberOfColumns());
                            b2 = random.nextInt(individuals.get(j).getNumberOfColumns());

                            if(b1>b2){
                                max = b1;
                                min = b2;
                            } else {
                                max = b2;
                                min = b1;
                            }

                            if(roleta == 1)
                                insertionMutation(max, min, permutRow, individuals.get(j));
                            else if(roleta == 2)
                                exchangedMutation(max, min, permutRow, individuals.get(j));
                            else
                                crossExchangeMutation(max, min, permutRow, individuals.get(j));
                        }
                    }
                }
            }
        }
        return individuals;
    }   
    
    public ReorderableMatrix tournaments(List<ReorderableMatrix> individuals){
        
        ReorderableMatrix bestMatrix = individuals.get(0);
        double stressValue[] = new double[individuals.size()];
        
        for(int i=0; i<individuals.size(); i++){
            stressValue[i] = stress.evaluate(individuals.get(i));
        }
        
        double min = stressValue[0];
        
        for(int j=0; j<individuals.size(); j++){
            if(min > stressValue[j]){
                min = stressValue[j];
                bestMatrix = individuals.get(j);
            }
        }
        return bestMatrix;
    }
       
    public int[][] suppressionRow(List<ReorderableMatrix> individuals){
        
        int x;
        int y;
        int nOperations;
        int result[][] = new int[individuals.size()][individuals.size()];
       
        boolean finished;
        
        for(x=0; x<individuals.size()-1; x++){
            
            boolean stop = false;
            int numOfGenes = individuals.get(x).getNumberOfRows();
            ReorderableMatrix matrixOne = individuals.get(x); 
            
            for(y=x+1; y<individuals.size(); y++){
                
                ReorderableMatrix matrixTwo = individuals.get(y);
                
                // Step 1
                int k = 0;
                int currentElement = matrixOne.getRowIndex(k);
                nOperations = 0;
                finished=false;
                
                // Step 2        
                int j=-1;
                do{
                    j++;
                }while(matrixTwo.getRowIndex(j) != currentElement && j<numOfGenes);
                
                
                //Step 3 for Rows
                while(!finished) {
                    // Step 3.1
                    k++;
                    currentElement = matrixOne.getRowIndex(k);

                    // Step 3.2
                    int n=-1;
                    do{
                        n++;
                    }while(matrixTwo.getRowIndex(n) != currentElement && n<numOfGenes);

                    //Step 3.3
                    int index1, index2;
                    index1=j+1;
                    if (index1==numOfGenes) {
                        index1=0;
                    }
                    index2=j-1;
                    if (index2==-1) {
                        index2=numOfGenes-1;
                    }
                    //Step 3.4
                    if (n!=index1 && n!=index2){
                        nOperations++;
                    }
                    //Step 3.5
                    j=n;
                    //Step 3.6
                    if (k==numOfGenes-1) {
                        finished = true;
                    }
                };
               
               
                //Step 3 for Columns
                result[x][y] = nOperations;
                nOperations = 0;
            }
        }
                
        return result;
    }
    
    public int[][] suppressionColumn(List<ReorderableMatrix> individuals){
        
        int x;
        int y;
        int nOperations;
        int result[][] = new int[individuals.size()][individuals.size()];
       
        boolean finished;
        
        for(x=0; x<individuals.size()-1; x++){
            
            boolean stop = false;
            int numOfGenes = individuals.get(x).getNumberOfColumns();
            ReorderableMatrix matrixOne = individuals.get(x); 
            
            for(y=x+1; y<individuals.size(); y++){
                
                ReorderableMatrix matrixTwo = individuals.get(y);
                
                // Step 1
                int k = 0;
                int currentElement = matrixOne.getColumnIndex(k);
                nOperations = 0;
                finished=false;
                
                // Step 2        
                int j=-1;
                do{
                    j++;
                }while(matrixTwo.getColumnIndex(j) != currentElement && j<numOfGenes);
                
                
                //Step 3 for Column
                while(!finished) {
                    // Step 3.1
                    k++;
                    currentElement = matrixOne.getColumnIndex(k);

                    // Step 3.2
                    int n=-1;
                    do{
                        n++;
                    }while(matrixTwo.getColumnIndex(n) != currentElement && n<numOfGenes);

                    //Step 3.3
                    int index1, index2;
                    index1=j+1;
                    if (index1==numOfGenes) {
                        index1=0;
                    }
                    index2=j-1;
                    if (index2==-1) {
                        index2=numOfGenes-1;
                    }
                    //Step 3.4
                    if (n!=index1 && n!=index2){
                        nOperations++;
                    }
                    //Step 3.5
                    j=n;
                    //Step 3.6
                    if (k==numOfGenes-1) {
                        finished = true;
                    }
                };
               
               
                //Step 3 for Columns
                result[x][y] = nOperations;
                nOperations = 0;
            }
        }
        
        return result;
    }
    
    public List<ReorderableMatrix> suppression(List<ReorderableMatrix> individuals){
        
        int row[][] = new int[individuals.size()][individuals.size()];
        int column[][] = new int[individuals.size()][individuals.size()];
        int result[][] = new int[individuals.size()][individuals.size()];
        Integer value = Integer.MAX_VALUE;
        int matrix = 0;
        
        row = suppressionRow(individuals);
        column = suppressionColumn(individuals);
        
        for(int i =0; i<individuals.size(); i++){
            for(int j=0; j<individuals.size(); j++){
                result[i][j] = row[i][j] + column[i][j];
            }
        }
        
        
        // IMPRESSÃO PARA TESTE
                System.out.print("Linhas\n\n");
                for(int i =0; i<individuals.size(); i++){
                    for(int j=0; j<individuals.size(); j++){
                        System.out.print(row[i][j]+", ");
                    }
                    System.out.print("\n");
                } 

                System.out.print("\n\nColunas\n\n");
                for(int i =0; i<individuals.size(); i++){
                    for(int j=0; j<individuals.size(); j++){
                        System.out.print(column[i][j]+", ");
                    }
                    System.out.print("\n");
                } 

                System.out.print("\n\nSoma\n\n");
                for(int i =0; i<individuals.size(); i++){
                    for(int j=0; j<individuals.size(); j++){
                        System.out.print(result[i][j]+", ");
                    }
                    System.out.print("\n");
                }                            
        
        for(int i =0; i<individuals.size(); i++){
            for(int j=0; j<individuals.size(); j++){
                if(result[i][j] < value && result[i][j] != 0)
                    value = result[i][j];
                    matrix = i;
            }
        }
        
        individuals.remove(matrix);
        individuals.add(matrix, getIndividuals());
        
        System.out.println("\n\nResultado: "+value+"\n");
        
        return individuals;
    }
    
   @Override
    public ReorderableMatrix sort() {
        
        System.out.println("Stress original "+stress.evaluate(originalMatrix)+"\n\n\n");
        
        ThreeOpt opt = new ThreeOpt();
        ReorderableMatrix bestMatrix = originalMatrix;
        List<ReorderableMatrix> geracaoList, bestList = new ArrayList<ReorderableMatrix>();
        int geracao = 0;
                
        for(int i=0; i<population; i++){
            individuals.add(i,getIndividuals());
        }
                
        do{
            rateMutation(individuals);
            geracaoList = mutation(individuals);
            
            for(int i=0; i<individuals.size(); i++){    
                bestMatrix = opt.heuristic(geracaoList.get(i), true);
                bestMatrix = opt.heuristic(bestMatrix, false);
                
                geracaoList.remove(i);
                geracaoList.add(i, bestMatrix);
            }          
            
            geracaoList = suppression(geracaoList);
            
            for(int j=0; j<individuals.size(); j++){
                if(stress.evaluate(individuals.get(j))>stress.evaluate(geracaoList.get(j))){
                    individuals.remove(j);
                    individuals.add(j, geracaoList.get(j));
                }
            }
            
            bestMatrix = tournaments(geracaoList);    
            
            bestList.add(geracao, bestMatrix);
            
            geracao++;
            
        }while(geracao < 50);   
        
        bestMatrix = tournaments(bestList);
     
        return bestMatrix;
    }    
    
    @Override
    public String getAlgorithmName() {
        return "Evoluctionary Biclustering";
    }

    @Override
    public String getShortAlgorithmName() {
        return "Biclustering";
    }  
}
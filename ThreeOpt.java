package br.unicamp.ft.mra.reordering;

import br.unicamp.ft.mra.ReorderableMatrix;
import br.unicamp.ft.mra.SimilarityMatrix;
import br.unicamp.ft.mra.coefficient.EuclideanDistance;
import br.unicamp.ft.mra.coefficient.ICoefficient;
import br.unicamp.ft.mra.evaluation.MinimalSpanLossFunction;
import br.unicamp.ft.mra.evaluation.StressNeighborhoodMoore;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Pedro Junior
 */
public class ThreeOpt extends AbstractMatrixReorderingAlgorithm{
    
    private ReorderableMatrix matrizOriginal;    
    
    public ThreeOpt(ReorderableMatrix originalMatrix){
        super(originalMatrix);
    }
    
    public ThreeOpt() {}
    
    public ReorderableMatrix heuristic(ReorderableMatrix m, boolean order){
        
        
        // If the matrix greather than 5 return the matrix reorderable, else return original matrix; 
        if(m.getNumberOfColumns() > 5 && m.getNumberOfRows() > 5){
            int indexA, indexA1;
            int indexB, indexB1;
            int indexC, indexC1;

            ReorderableMatrix matrixOne;
            ReorderableMatrix matrixTwo;
            ReorderableMatrix matrixTree;
            ReorderableMatrix matrixFour;

            List<ReorderableMatrix> list = new ArrayList<ReorderableMatrix>();

            Random random = new Random(System.currentTimeMillis());

            do{
                if(order){    
                    indexA = random.nextInt(m.getNumberOfRows());
                    indexA1 = (indexA==m.getNumberOfRows()-1?0:indexA+1); //indexA+1 no vetor circular
                    indexB = random.nextInt(m.getNumberOfRows());
                    indexB1 = (indexB==m.getNumberOfRows()-1?0:indexB+1); //indexB+1 no vetor circular
                    indexC = random.nextInt(m.getNumberOfRows());
                    indexC1 = (indexC==m.getNumberOfRows()-1?0:indexC+1); //indexC+1 no vetor circular
                } else {
                    indexA = random.nextInt(m.getNumberOfColumns());
                    indexA1 = (indexA==m.getNumberOfColumns()-1?0:indexA+1); //indexA+1 no vetor circular
                    indexB = random.nextInt(m.getNumberOfColumns());
                    indexB1 = (indexB==m.getNumberOfColumns()-1?0:indexB+1); //indexB+1 no vetor circular
                    indexC = random.nextInt(m.getNumberOfColumns());
                    indexC1 = (indexC==m.getNumberOfColumns()-1?0:indexC+1); //indexC+1 no vetor circular
                }
            }while(indexA == indexB || indexA == indexC || indexB == indexC

                    || indexB == indexA1 || indexC == indexB1 || indexA == indexC1 

                    ||(indexB < indexA && indexA < indexC)

                    ||(indexA < indexC && indexC < indexB)

                    ||(indexC < indexB && indexB < indexA));

            // reordenação 1
            matrixOne = move(new ReorderableMatrix(m), indexA1, indexB, indexC, order);

            // reordenação 2
            matrixTwo = flip(new ReorderableMatrix(m), indexA1, indexB, order);
            matrixTwo = move(matrixTwo, indexA1, indexB, indexC, order);

            // reordenação 3
            matrixTree = flip(new ReorderableMatrix(m), indexB1, indexC, order);        
            matrixTree = move(matrixTree, indexA1, indexB, indexC, order);

            // reordenação 4
            matrixFour = flip(new ReorderableMatrix(m), indexB1, indexC, order);
            matrixFour = flip(matrixFour, indexA1, indexB, order);



            list.add(0, matrixOne);
            list.add(1, matrixTwo);
            list.add(2, matrixTree);
            list.add(3, matrixFour);

            m = calculate(list, order);

            return m;
        } else {
            return m;
        }
    }
    
    // Imprime a ordem dos rótulos dos índices. Método apenas para teste;
    public void teste(ReorderableMatrix m1,ReorderableMatrix m2,ReorderableMatrix m3,ReorderableMatrix m4, boolean order){
        
        if(order){
            System.out.print("\n Matriz Linhas 1:\n");
            for(int i=0; i < m1.getNumberOfRows(); i++){
                System.out.print(m1.getRowLabels(i)+", ");
            }
            System.out.print("\n Matriz Linhas 2:\n");
            for(int i=0; i < m2.getNumberOfRows(); i++){
                System.out.print(m2.getRowLabels(i)+", ");
            }
            System.out.print("\n Matriz Linhas 3:\n");
            for(int i=0; i < m3.getNumberOfRows(); i++){
                System.out.print(m3.getRowLabels(i)+", ");
            }

            System.out.print("\n Matriz Linhas 4:\n");
            for(int i=0; i < m4.getNumberOfRows(); i++){
                System.out.print(m4.getRowLabels(i)+", ");
            }
            System.out.print("\n");
        } else {
            System.out.print("\n Matriz Colunas 1:\n");
            for(int i=0; i < m1.getNumberOfColumns(); i++){
                System.out.print(m1.getColumnLabels(i)+", ");
            }
            System.out.print("\n Matriz Colunas 2:\n");
            for(int i=0; i < m2.getNumberOfColumns(); i++){
                System.out.print(m2.getColumnLabels(i)+", ");
            }
            System.out.print("\n Matriz Colunas 3:\n");
            for(int i=0; i < m3.getNumberOfColumns(); i++){
                System.out.print(m3.getColumnLabels(i)+", ");
            }

            System.out.print("\n Matriz Colunas 4:\n");
            for(int i=0; i < m4.getNumberOfColumns(); i++){
                System.out.print(m4.getColumnLabels(i)+", ");
            }
            System.out.print("\n");
        }
    }
    
    public ReorderableMatrix calculate(List<ReorderableMatrix> list, boolean order){
        
        int best = 0;
        double compare = Double.POSITIVE_INFINITY;
        double valor[] = new double[4];
        ICoefficient coef = new EuclideanDistance();
        SimilarityMatrix matrix;
        MinimalSpanLossFunction mslf;
                        
        for(int i=0; i<4; i++){
            matrix = new SimilarityMatrix(list.get(i), order, coef);
            mslf = new MinimalSpanLossFunction();
            valor[i] = mslf.evaluate(matrix, false);
        }
        
        for(int j=0; j<4; j++){
            if(valor[j] < compare){
                compare = valor[j];
                best = j;
            }
        }
                
        return list.get(best);
    }
    
    public ReorderableMatrix flip(ReorderableMatrix matrix, int p1, int p2, boolean order){
        
        if(order){
            if(p1<p2){
                do{
                    matrix.permuteRow(p1, p2);
                    p1++;
                    p2--; 
                }while(p1<p2);
            } else if(p1>p2) {
                do{                 

                    matrix.permuteRow(p1, p2);

                    p1++;
                    p2--; 

                    if(p1== matrix.getNumberOfRows())
                        p1=0;
                    if(p2< 0)
                        p2=matrix.getNumberOfRows()-1;

                }while(p1 != p2 && p1 != p2+1 && 
                       !(p2==matrix.getNumberOfRows()-1 && p1 == 0) );   
            }
        /* ---------- CÓDIGO REPLICADO PARA COLUNAS ---------- */         
        } else {
            if(p1<p2){
                do{
                    matrix.permuteColumn(p1, p2);
                    p1++;
                    p2--; 
                }while(p1<p2);
            } else if(p1>p2) {
                do{                 
      
                    matrix.permuteColumn(p1, p2);

                    p1++;
                    p2--; 

                    if(p1== matrix.getNumberOfColumns())
                        p1=0;
                    if(p2< 0)
                        p2=matrix.getNumberOfColumns()-1;

                }while(p1 != p2 && p1 != p2+1 && 
                       !(p2==matrix.getNumberOfColumns()-1 && p1 == 0) );   
             }
        }
          
        return matrix;
    }
    
    public ReorderableMatrix move(ReorderableMatrix matrix, int pi, int pf, int bf, boolean order){
        
        if(order){
            if(pi<pf){  
                if(pi>bf){
                    for(int i = pf; i>=pi; i--){
                        for(int j= pf; j>bf+1; j--){
                            matrix.permuteRow(j, j-1);
                        }
                    }
                }else{
                    for(int i = pi; i<pf+1  ; i++){
                        for(int j = pi; j<bf; j++){
                            matrix.permuteRow(j, j+1);
                        }
                    }
                }
            } else {
                int aux = bf+1;
                int aux1= 0;
                for(int i = matrix.getNumberOfRows()-1; i>= pi; i--){
                    for(int j = matrix.getNumberOfRows()-1; j>aux; j--){
                        matrix.permuteRow(j, j-1);   
                    }
                    aux1++;
                }
                aux1 += bf;
                for(int i = 0; i<pf+1  ; i++){
                    for(int j = 0; j<aux1; j++){
                        matrix.permuteRow(j, j+1);   
                    }
                    aux++;
                }
            }    
       /* ---------- CÓDIGO REPLICADO PARA COLUNAS ---------- */     
        } else {
            if(pi<pf){  
                if(pi>bf){
                    for(int i = pf; i>=pi; i--){
                        for(int j= pf; j>bf+1; j--){
                            matrix.permuteColumn(j, j-1);
                        }
                    }
                }else{
                    for(int i = pi; i<pf+1  ; i++){
                        for(int j = pi; j<bf; j++){
                            matrix.permuteColumn(j, j+1);
                        }
                    }
                }
            } else {
                int aux = bf+1;
                int aux1= 0;
                for(int i = matrix.getNumberOfColumns()-1; i>= pi; i--){
                    for(int j = matrix.getNumberOfColumns()-1; j>aux; j--){
                        matrix.permuteColumn(j, j-1);   
                    }
                    aux1++;
                }
                aux1 += bf;
                for(int i = 0; i<pf+1  ; i++){
                    for(int j = 0; j<aux1; j++){
                        matrix.permuteColumn(j, j+1);   
                    }
                    aux++;
                }
            }
        }
        
        return matrix;
    }
    
    @Override
    public ReorderableMatrix sort() {        
        
        ReorderableMatrix matrix;
        StressNeighborhoodMoore stress = new StressNeighborhoodMoore();
        double valorTotal = stress.evaluate(originalMatrix);
        double valor;
        int i =0;
        
        matrix = heuristic(originalMatrix, true);
        matrix = heuristic(matrix, false);
       
        System.out.println("Total: "+valorTotal);
        
        do{
            matrix = heuristic(matrix, true);
            matrix = heuristic(matrix, false);
            
            valor = stress.evaluate(matrix);
            
            System.out.println(i+": "+valor);
            i++;
        }while(valor > valorTotal/2);
        
        return  matrix; 
    }

    @Override
    public String getAlgorithmName() {
        return "3-opt";
    }

    @Override
    public String getShortAlgorithmName() {
        return "3-opt";
    } 
}
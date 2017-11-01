package br.unicamp.ft.mra.reordering;

import br.unicamp.ft.mra.IMatrix;
import br.unicamp.ft.mra.ReorderableMatrix;
import br.unicamp.ft.mra.SimilarityMatrix;
import br.unicamp.ft.mra.coefficient.EuclideanDistance;
import br.unicamp.ft.mra.coefficient.ICoefficient;
import br.unicamp.ft.mra.coefficient.PearsonCorrelation;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EllipticalSeriation extends AbstractMatrixReorderingAlgorithm {

    private final int PRECISAO = 10000;
    
    private IMatrix matrix;

    public EllipticalSeriation(ReorderableMatrix originalMatrix) {
        super(originalMatrix);
    }

    public EllipticalSeriation() {
    }

    public IMatrix calculateMatrixRInfinite(ReorderableMatrix m, boolean isRowOrder) {

        matrix = new ReorderableMatrix(m);
        ICoefficient coef = new EuclideanDistance();
        matrix = new SimilarityMatrix(matrix, isRowOrder, coef);
        ICoefficient coefCorr = new PearsonCorrelation();
        
        int i;
        int j;
       
        double valor;
        boolean pronto;
        
        SimilarityMatrix oldMatrix;
        do {
            System.out.println("Calculando R infinito\n\n");
            oldMatrix = new SimilarityMatrix((SimilarityMatrix)matrix);
            matrix = new SimilarityMatrix(matrix, true, coefCorr);
            

            for (i = 0; i < matrix.getNumberOfRows(); i++) {
                for (j = 0; j < matrix.getNumberOfColumns(); j++) {

                    valor = matrix.getValue(i, j);
                    
                    valor=Math.rint(valor*PRECISAO)/PRECISAO; // cuida de casos de arrendondamento e de NaN.
                    
                    matrix.setValue(i, j, valor);
                    

                    System.out.print(matrix.getValue(i, j) + "   "); // DEBUG ONLY.
                }

                System.out.print("\n");// DEBUG ONLY.
                
            }
            System.out.print("\n\n");// DEBUG ONLY.
            
            // Obs. Na convergência nem sempre a matriz converge para -1 ou 1 ... veja artigo do Chen pag. 16-17
            pronto = sameContent(oldMatrix,matrix); // Verifica convergência.

        } while (!pronto);
        System.out.println("OK!");
        return matrix;
    }

    private boolean sameContent(IMatrix a, IMatrix b) {
        if (a.getNumberOfColumns() != b.getNumberOfColumns() || a.getNumberOfRows() != b.getNumberOfRows()) {
            return false;
            // Não precisa verificar linhas, pois matriz de similaridade é simétrica.
        }
        for (int i=0; i<a.getNumberOfRows(); i++) {
            for (int j=0; j<a.getNumberOfColumns(); j++) {
                if (a.getValue(i, j) != b.getValue(i,j)) {
//                    System.out.print("a["+i+","+j+"]="+a.getValue(i,j)+"    ");
//                    System.out.println("b["+i+","+j+"]="+b.getValue(i,j)+"    ");
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean compareDimension(IMatrix matrix, int indexOne, int indexTwo){
        
        boolean equalDimension = true;
        int i;
        
        i=0;
        while(i<matrix.getNumberOfRows() && equalDimension){
            if( matrix.getValue(i, indexOne) != matrix.getValue(i, indexTwo)){
                equalDimension = false;
            } else {
                i++;
            }
        }
                
        return equalDimension;
    }
    
    

    private void createSubmatrix(ReorderableMatrix oldMatrix,
            ReorderableMatrix newMatrix, List<Integer> rowIndexList, List<Integer> columnIndexList, 
            Map<Integer,Integer> newToOldRowIndexes, Map<Integer,Integer> newToOldColumnIndexes) {

        if (rowIndexList==null) {
            rowIndexList=new ArrayList<Integer>();
            for (int i=0; i<oldMatrix.getNumberOfRows(); i++) {
                rowIndexList.add(oldMatrix.getRowIndex(i));
            }
        }
        
        if (columnIndexList==null) {
            columnIndexList=new ArrayList<Integer>();
            for (int i=0; i<oldMatrix.getNumberOfColumns(); i++) {
                columnIndexList.add(oldMatrix.getColumnIndex(i));
            }
        }
        
        Integer newRowIndex=0;
        for (Integer oldRowIndex : rowIndexList) {
            Integer newColumnIndex=0;    
            for (Integer oldColumnIndex : columnIndexList) {
                double value = oldMatrix.getValue(oldRowIndex,oldColumnIndex);
                newMatrix.setValue(newRowIndex, newColumnIndex, value);
                newToOldRowIndexes.put(newRowIndex, oldRowIndex);
                newToOldColumnIndexes.put(newColumnIndex, oldColumnIndex);
                newColumnIndex++;
            }
            newRowIndex++;
        }
    }
    
    // Calcula recursivamente a nova ordem de linhas ou de colunas da matriz m.
    private List<Integer> getNewOrderList(ReorderableMatrix m, boolean isRowOrder) {
        System.out.println("Matriz");
        System.out.println("  Linhas:"+m.getNumberOfRows());
        System.out.println("  Colunas:"+m.getNumberOfColumns());
        
        
        // Caso base
        if (isRowOrder && m.getNumberOfRows()<=2) {
            List<Integer> rowIndexList=new ArrayList<Integer>();
            for (int i=0; i<m.getNumberOfRows(); i++) {
                rowIndexList.add(m.getRowIndex(i));
            }
            return rowIndexList; 
        }
        if (!isRowOrder && m.getNumberOfColumns()<=2) {
            List<Integer> columnIndexList=new ArrayList<Integer>();
            for (int i=0; i<m.getNumberOfColumns(); i++) {
                columnIndexList.add(m.getColumnIndex(i));
            }
            return columnIndexList; 
        }

        
        
        IMatrix rInfiniteMatrix = calculateMatrixRInfinite(m,isRowOrder);
        
        List<Integer> A = new ArrayList<Integer>();
        List<Integer> B = new ArrayList<Integer>();
        
        int i;
        
        A.add(0);
        
        for(i=1; i<rInfiniteMatrix.getNumberOfColumns(); i++){
            if(compareDimension(rInfiniteMatrix, 0, i)){
                A.add(i);
            } else {
                B.add(i);
            }
        }
        
        if (B.isEmpty()) {
            return A;
        } else if (A.isEmpty()) {
            return B;
        }
        
        // Passo recursivo
        
        // Cria 2 submatrizes, com base em A e B
        
        // TODO CRIAR OBJETOS AQUI FORA DO MÉTODO CREATESUBMATRIX, SENÃO DA NULL POINTER EXCEPTION.
        
        Map<Integer,Integer> newToOldRowIndexesA = new HashMap<Integer,Integer>();;
        Map<Integer,Integer> newToOldColumnIndexesA = new HashMap<Integer,Integer>();;
        Map<Integer,Integer> newToOldRowIndexesB = new HashMap<Integer,Integer>();;
        Map<Integer,Integer> newToOldColumnIndexesB = new HashMap<Integer,Integer>();;
        ReorderableMatrix mA=null,mB=null;
        
        if (isRowOrder) {
            mA = new ReorderableMatrix(A.size(), m.getNumberOfColumns());
            mB = new ReorderableMatrix(B.size(), m.getNumberOfColumns());
            createSubmatrix(m,mA,A,null,newToOldRowIndexesA, newToOldColumnIndexesA);
            createSubmatrix(m,mB,B,null,newToOldRowIndexesB, newToOldColumnIndexesB);
        } else {
            mA = new ReorderableMatrix(m.getNumberOfRows(),A.size());
            mB = new ReorderableMatrix(m.getNumberOfRows(),B.size());
            createSubmatrix(m,mA,null,A,newToOldRowIndexesA, newToOldColumnIndexesA);
            createSubmatrix(m,mB,null,B,newToOldRowIndexesB, newToOldColumnIndexesB);
        }

        
        // Resolve o problema recursivamente para duas submatrizes
        List<Integer> newA = getNewOrderList(mA,isRowOrder);
        List<Integer> newB = getNewOrderList(mB,isRowOrder);
        
       
        
        
        List<Integer> latestA = new ArrayList<Integer>();
        List<Integer> latestB = new ArrayList<Integer>();
        if (isRowOrder) {
            for (Integer elem : newA) {
                latestA.add(newToOldRowIndexesA.get(elem));
            }
            for (Integer elem : newB) {
                latestB.add(newToOldRowIndexesB.get(elem));
            }
        } else {
            for (Integer elem : newA) {
                latestA.add(newToOldColumnIndexesA.get(elem));
            }
            for (Integer elem : newB) {
                latestB.add(newToOldColumnIndexesB.get(elem));
            }
        }

        System.out.println(Arrays.toString(latestA.toArray())+" + "+Arrays.toString(latestB.toArray()));
        
        
        // Melhorias (Celmar): flipping de acordo com semelhança
//        
//        int aIni=0;
//        int aFim=latestA.size()-1;
//        int bIni=aFim+1;
//        int bFim=matrix.getNumberOfColumns()-1;
//        
//        ICoefficient coef = new PearsonCorrelation();
//        double[] comparison = new double[4];
//        comparison[0] = 1-coef.compareColumns(m, aIni, bIni);
//        comparison[1] = 1-coef.compareColumns(m, aIni, bFim);
//        comparison[2] = 1-coef.compareColumns(m, aFim, bIni);
//        comparison[3] = 1-coef.compareColumns(m, aFim, bFim);
//        
//        double max = comparison[0];
//        int maxIndex=0;
//        for (int s=1; s<4; s++) {
//            if (comparison[s] > max) {
//                maxIndex=s;
//                max=comparison[s];
//            }
//        }
//        
//        switch (maxIndex) {
//            case 0:
//                Collections.reverse(latestA);
//                break;
//            case 1:
//                Collections.reverse(latestA);
//                Collections.reverse(latestB);
//                break;
//            case 2:
//                // nada a fazer
//                break;
//            case 3:
//                Collections.reverse(latestB);
//                break;
//                
//        }
//        
//        System.out.println("Após flipping:");
//        System.out.println(Arrays.toString(latestA.toArray())+" + "+Arrays.toString(latestB.toArray()));
        
        List<Integer> finalOrder = new ArrayList<Integer>();
        finalOrder.addAll(latestA);
        finalOrder.addAll(latestB);
        
        
        System.out.println("Ordem final de lista: "+ Arrays.toString(finalOrder.toArray()));
        
        
//        int[] newOrder = new int[m.getNumberOfColumns()];
//        for (i=0; i<newOrder.length; i++) {
//            newOrder[i]=newA.get(i);
//        }
        
        return finalOrder;

    }
    
    private int[] getNewOrder(ReorderableMatrix m, boolean isRowOrder) {
        List<Integer> newOrderList = getNewOrderList(m, isRowOrder);
        int[] newOrder = new int[newOrderList.size()];
        int i;
        for (i=0; i<newOrder.length; i++) {
            if (isRowOrder) {
                newOrder[i]=m.getRowIndex(newOrderList.get(i));
            } else {
                newOrder[i]=m.getColumnIndex(newOrderList.get(i));
            }
        }
        return newOrder;
    }
    
    @Override
    public ReorderableMatrix sort() {

        int[] newRowOrder=getNewOrder(originalMatrix,true);
        int[] newColumnOrder=getNewOrder(originalMatrix,false);
        ReorderableMatrix result = new ReorderableMatrix(originalMatrix);
        result.setRowOrder(newRowOrder);
        result.setColumnOrder(newColumnOrder);
        return result;        
        
//        matrix = calculateMatrixRInfinite();     
//        
//        orderMatrix(matrix);
//        
//        ((SimilarityMatrix) matrix).printCSV("teste.csv");
//        
//        return originalMatrix ;// temporario
    }

    @Override
    public String getAlgorithmName() {
        return "Serialization Elliptical";
    }

    @Override
    public String getShortAlgorithmName() {
        return "Serialization";
    }

}

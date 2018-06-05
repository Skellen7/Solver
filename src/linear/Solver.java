package linear;

import javafx.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;

public class Solver {
    List<Double> firstLimit = new ArrayList<>();
    List<Double> secondLimit = new ArrayList<>();
    List<Double> target = new ArrayList<>();

    List<Pair<Double, Double>> potentialSolutions = new ArrayList<>();
    double solution;
    List<Double> solutionTarget = new ArrayList<>();

    public Solver(String filepath, String delimiter){
        File file = new File(filepath);
        Scanner sc = null;
        try {
            sc = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line[];
        int lineNr=0;
        while (sc.hasNextLine()){
            line = sc.nextLine().split(delimiter);
            for(String factor : line){
                switch(lineNr){
                    case 0 :
                        this.firstLimit.add(Double.parseDouble(factor));
                        break;
                    case 1 :
                        this.secondLimit.add(Double.parseDouble(factor));
                        break;
                    case 2 :
                        this.target.add(Double.parseDouble(factor));
                        break;
                }
            }
            lineNr++;
        }
    }

    public void solve() {
        List<Double> dualTarget = new ArrayList<>();

        List<List<Double>> dualLimits = new ArrayList<>();

        for (int i = 0; i < firstLimit.size() - 1; i++) {
            dualLimits.add(new ArrayList<>()); //initialize y1+y2 >= b
        }

        for (int i = 0; i < firstLimit.size() - 1; i++) {
            dualLimits.get(i).add(firstLimit.get(i));
            dualLimits.get(i).add(secondLimit.get(i));
            dualLimits.get(i).add(this.target.get(i));

        }
        //x=0 and y=0
        List<Double> xAxis = new ArrayList<>();
        xAxis.add(1.0);
        xAxis.add(0.0);
        xAxis.add(0.0);
        List<Double> yAxis = new ArrayList<>();
        yAxis.add(0.0);
        yAxis.add(1.0);
        yAxis.add(0.0);
        dualLimits.add(xAxis);
        dualLimits.add(yAxis);


        dualTarget.add(this.firstLimit.get(this.firstLimit.size() - 1));
        dualTarget.add(this.secondLimit.get(this.secondLimit.size() - 1));

        //dual done

        //solve system of equations

        for (int i = 0; i < dualLimits.size(); i++) {
            for (int j = i + 1; j < dualLimits.size(); j++) {

                double a1 = dualLimits.get(i).get(0);
                double b1 = dualLimits.get(i).get(1);
                double c1 = dualLimits.get(i).get(2);
                double a2 = dualLimits.get(j).get(0);
                double b2 = dualLimits.get(j).get(1);
                double c2 = dualLimits.get(j).get(2);

                double w = a1 * b2 - b1 * a2;
                double wx = c1 * b2 - b1 * c2;
                double wy = a1 * c2 - c1 * a2;


                if (w != 0) {//inaczej mamy nieskonczenie wiele rozwiazan lub sprzeczne
                    double x = wx / w;
                    double y = wy / w;
                    if (x == -0) x = Math.abs(x);
                    if (y == -0) y = Math.abs(y);
                    Pair<Double, Double> potentialSolution = new Pair<>(x, y);
                    if (!potentialSolutions.contains(potentialSolution)) {
                        if (x >= 0 && y >= 0) {
                            potentialSolutions.add(potentialSolution);
                        }
                    }
                }
            }
        }


        //filtering
        Boolean isValid;
        for (int i = 0; i < potentialSolutions.size(); i++) {
            isValid = true;
            double x = potentialSolutions.get(i).getKey();
            double y = potentialSolutions.get(i).getValue();

            for (int j = 0; j < dualLimits.size(); j++) {
                double a = dualLimits.get(j).get(0);
                double b = dualLimits.get(j).get(1);
                double c = dualLimits.get(j).get(2);

                if (a * x + b * y < c) {
                    //System.out.println(potentialSolutions.get(i));
                    //System.out.println(a + "*"+ x+" "+b+"*"+y+"< "+c);
                    isValid = false;
                    break;
                }
            }
            if (!isValid) {
                potentialSolutions.remove(i);
                if (i != potentialSolutions.size()) i--;
            }
        }

        System.out.println(potentialSolutions);
        double a = dualTarget.get(0);
        double b = dualTarget.get(1);
        System.out.println(dualTarget);
        double minValue = potentialSolutions.get(0).getKey() * a + potentialSolutions.get(0).getValue() * b;
        Pair minPoint = potentialSolutions.get(0);
        for (int i = 1; i < potentialSolutions.size(); i++) {
            double x = potentialSolutions.get(i).getKey();
            double y = potentialSolutions.get(i).getValue();
            if (a * x + b * y < minValue) {
                minValue = a * x + b * y;
                minPoint = potentialSolutions.get(i);
            }
        }

        double x = (double) minPoint.getKey();
        double y = (double) minPoint.getValue();
        List<Integer> nonZeroDeterminants = new ArrayList<>();

        for(int i=0; i<dualLimits.size(); i++){
            a = dualLimits.get(i).get(0);
            b = dualLimits.get(i).get(1);
            double c = dualLimits.get(i).get(2);
            if(a*x+b*y == c){
                nonZeroDeterminants.add(i);
            }
        }

        List<Double> firstEquation = new ArrayList<>();
        firstEquation.add(firstLimit.get(nonZeroDeterminants.get(0)));
        firstEquation.add(firstLimit.get(nonZeroDeterminants.get(1)));
        firstEquation.add(firstLimit.get(firstLimit.size()-1));

        List<Double> secondEquation = new ArrayList<>();
        secondEquation.add(secondLimit.get(nonZeroDeterminants.get(0)));
        secondEquation.add(secondLimit.get(nonZeroDeterminants.get(1)));
        secondEquation.add(secondLimit.get(secondLimit.size()-1));

        double a1 = firstEquation.get(0);
        double b1 = firstEquation.get(1);
        double c1 = firstEquation.get(2);
        double a2 = secondEquation.get(0);
        double b2 = secondEquation.get(1);
        double c2 = secondEquation.get(2);

        double w = a1 * b2 - b1 * a2;
        double wx = c1 * b2 - b1 * c2;
        double wy = a1 * c2 - c1 * a2;
        System.out.println(w);

        if(w!=0){
            x = wx/w;
            y = wy/w;
            if (x == -0) x = Math.abs(x);
            if (y == -0) y = Math.abs(y);
            System.out.println(x + " " + y);
        }

        solution += x * this.target.get(nonZeroDeterminants.get(0));
        solution += y * this.target.get(nonZeroDeterminants.get(1));

        for(int i=0; i<this.target.size(); i++){
            if(i == nonZeroDeterminants.get(0)){
                solutionTarget.add(x);
            }
            else if (i == nonZeroDeterminants.get(1)){
                solutionTarget.add(y);
            }
            else{
                solutionTarget.add(0.0);
            }
        }

        System.out.println("Points limiting dual program: " + potentialSolutions.toString());
        System.out.println("Optimal coefficients " + solutionTarget.toString());
        System.out.println("Optimal solution: " + solution);







    }
}
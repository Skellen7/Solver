package linear;

public class Main {
    public static void main(String[] args){
        Solver solver = new Solver("task.txt",";");
        //System.out.println(solver);
        solver.solve();
    }
}

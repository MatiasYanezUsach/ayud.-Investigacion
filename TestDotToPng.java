import model.FileIO;

public class TestDotToPng {
    public static void main(String[] args) {
        try {
            String path = "out/results/evolution0/job.0.";
            String dotPath = "graphviz-2.38/bin/dot.exe";
            String file = "BestIndividual.dot";
            
            System.out.println("Probando conversión de .dot a .png...");
            FileIO.dot_a_png(path, dotPath, file);
            System.out.println("Prueba completada.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package cse.buet;

public class Main {
    public static void main(String[] args) {
        for (int i = 0; i < 100 ; i++) {
            try {
                Client.main(null);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

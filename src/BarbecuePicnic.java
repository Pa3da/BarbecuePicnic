

class Base {
    public static int[] NumberOfSkewers = new int[3];

}

class GrillMaster extends Base {
    public GrillMaster() {
        NumberOfSkewers[1] = 2;
    }
}

class Customer extends Base {
    public Customer() {
        NumberOfSkewers[1] = 1;
    }
}


public class BarbecuePicnic {
    public static void main(String[] args) {
        printFormat(Base.NumberOfSkewers);
        printFormat(Customer.NumberOfSkewers);
        printFormat(GrillMaster.NumberOfSkewers);
        System.out.println("new Customer");
        new Customer();
        printFormat(Base.NumberOfSkewers);
        printFormat(Customer.NumberOfSkewers);
        printFormat(GrillMaster.NumberOfSkewers);
        System.out.println("new GrillMaster");
        new GrillMaster();
        printFormat(Base.NumberOfSkewers);
        printFormat(Customer.NumberOfSkewers);
        printFormat(GrillMaster.NumberOfSkewers);
    }


    public static void printFormat(int[] a) {
        for (int num : a) {
            System.out.println(num);
        }
    }
}

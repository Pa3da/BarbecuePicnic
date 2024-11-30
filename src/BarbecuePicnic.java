

class Base {
	public static int NumberOfSkewers[]= new int[3];
	
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
		System.out.println(Base.NumberOfSkewers);
		System.out.println(Customer.NumberOfSkewers);
		System.out.println(GrillMaster.NumberOfSkewers);
		new Customer();
		System.out.println(Base.NumberOfSkewers);
		System.out.println(Customer.NumberOfSkewers);
		System.out.println(GrillMaster.NumberOfSkewers);
		new GrillMaster();
		System.out.println(Base.NumberOfSkewers);
		System.out.println(Customer.NumberOfSkewers);
		System.out.println(GrillMaster.NumberOfSkewers);
	}
	
	
}

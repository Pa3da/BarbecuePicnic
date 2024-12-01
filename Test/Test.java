
public class Test {
	public static void main(String[] args) {
		int[] exist = Base.calculateOfExist(0);
		if (exist.length == 0){
			System.out.println("hello");
		}
		for (int i : exist) {
			System.out.print(i + " ");
		}
		Thread grillMaster1 = new Thread(new GrillMaster("Pa3da", 1));
		grillMaster1.start();
		
		
	}
}

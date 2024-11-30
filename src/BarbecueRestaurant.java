/**
 * 父类
 */
class Base {
	//餐厅桌子数量
	public static int[] NumberOfTables = new int[Constants.NUMBER_OF_TABLES];
	
	
}

/**
 * 烧烤师傅
 */
class GrillMaster extends Base {
	public GrillMaster() {
		
	}
}

/**
 * 顾客
 */
class Customer extends Base {
	public Customer() {
		
	}
}

/**
 * 常量类
 */
class Constants {
	//餐厅桌子数量
	public static int NUMBER_OF_TABLES = 3;
	//正在占用的状态
	public static int USING_STATE = 0;
	//空闲状态
	public static int NO_USING_STATE = 1;
}


/**
 * 程序入口类
 */
public class BarbecueRestaurant {
	/**
	 * 入口
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		
	}
	
	
	public static void printFormat(int[] a) {
		for (int num : a) {
			System.out.println(num);
		}
	}
	
	/**
	 * 初始化
	 */
	public void init() {
		
	}
}

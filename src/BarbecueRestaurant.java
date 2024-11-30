import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 父类
 */
abstract class Base implements Runnable {
	protected static final Lock lock = new ReentrantLock();
	public int id;
	public String name;
	//餐厅桌子数量
	public static int[] numberOfTables = new int[Constants.NUMBER_OF_TABLES];
	//数组
	public static int[][] arrayOfPlates = new int[Constants.NUMBER_OF_TABLES][Constants.CAPACITY_OF_PLATE];
	public static int[] countOfPlates = new int[Constants.NUMBER_OF_TABLES];

//	public Base() {
//
//	}
}

/**
 * 烧烤师傅
 */
class GrillMaster extends Base {
	public GrillMaster(String name, int id) {
		this.name = name;
		this.id = id;
	}
	
	@Override
	public void run() {
		try {
			makingBBQ();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void makingBBQ() throws InterruptedException {
		System.out.println("GrillMaster " + this.id + "号 " + this.name + "进入makingBBQ()了");
		long endTime = System.currentTimeMillis() + 10000; // 10秒的结束时间
		while (System.currentTimeMillis() < endTime) {
			if (lock.tryLock(5, TimeUnit.SECONDS))
				System.out.println("GrillMaster " + this.id + "号 " + this.name + "进入锁了");
			//TODO 制作就是简单的制作，从第一个开始填充，如果遇到已经填充的就跳过，填充下一个，直到填充完毕
			int n = 0;
			for (int i = 0; i < arrayOfPlates.length; i++) {
				for (int j = 0; j < arrayOfPlates[i].length; j++) {
					if (arrayOfPlates[i][j] == 0 && n < 20) {
						arrayOfPlates[i][j] = 1;
						n++;
					} else if (n >= 10) {
						break;
					}
				}
				if (n >= 10) {
					break;
				}
			}
			BarbecueRestaurant.printStateOfPlates();
			Thread.sleep(Constants.TIME_OF_MAKING_BBQ);
			BarbecueRestaurant.printStateOfPlates();
			System.out.println("GrillMaster " + this.id + "号 " + this.name + "出锁了");
			lock.unlock();
			return;
		}
		System.out.println(this.name + "等待太久了，进不去");
		System.exit(0);
	}
}

/**
 * 顾客
 */
class Customer extends Base {
	public Customer(String name, int id) {
		this.name = name;
		this.id = id;
	}
	
	@Override
	public void run() {
		try {
			take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void take() throws InterruptedException {
		Thread.sleep(500);
		System.out.println("Customer " + this.id + "号 " + this.name + "进入take()了");
		int i = 1;
		long endTime = System.currentTimeMillis() + 10000; // 10秒的结束时间
		while (System.currentTimeMillis() < endTime) {
			if (lock.tryLock(5, TimeUnit.SECONDS)) {
				System.out.println("Customer " + this.id + "号 " + this.name + "进入锁了");
				while (i <= 10) {
					int j = (int) (Math.random() * Constants.CAPACITY_OF_PLATE);
					if (Base.arrayOfPlates[0][j] == 1) {
						Base.arrayOfPlates[0][j] = 0;
						i++;
					}
				}
				System.out.println("Customer " + this.id + "号 " + this.name + "出锁了");
				lock.unlock();
				BarbecueRestaurant.printStateOfPlates();
				return;
			}
		}
		System.out.println(this.name + "等待太久了，进不去");
		System.exit(0);
	}
	
}

/**
 * 常量类
 */
class Constants {
	//餐厅桌子数量
	public static int NUMBER_OF_TABLES = 3;
	//每个盘子的容量
	public static int CAPACITY_OF_PLATE = 50;
	//正在占用的状态
	public static int USING_STATE = 0;
	//空闲状态
	public static int NO_USING_STATE = 1;
	//厨师制作烧烤时间
	public static int TIME_OF_MAKING_BBQ = 6000;
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
		init();
	}
	
	
	public static void printFormat(int[] a) {
		for (int num : a) {
			System.out.println(num);
		}
	}
	
	/**
	 * 初始化
	 */
	public static void init() {
		Thread grillMaster1 = new Thread(new GrillMaster("Pa3da", 1));
		Thread customer1 = new Thread(new Customer("Matinal", 2));
		grillMaster1.start();
		customer1.start();
	}
	
	public static void printStateOfPlates() {
		Base.countOfPlates = new int[]{0,0,0};
		for (int i = 0; i < Base.arrayOfPlates.length; i++) {
			for (int j = 0; j < Base.arrayOfPlates[i].length; j++) {
				System.out.print(Base.arrayOfPlates[i][j] + " ");
				if (Base.arrayOfPlates[i][j] == 1) {
					Base.countOfPlates[i]++;
				}
			}
			System.out.println();
		}
		for (int i = 0; i < Base.countOfPlates.length; i++) {
			System.out.print(Base.countOfPlates[i] + " ");
		}
		System.out.println();
	}
}

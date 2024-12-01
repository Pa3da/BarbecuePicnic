import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 父类
 */
abstract class Base implements Runnable {
	
	public int id;
	public String name;
	//数组
	public static int[][] arrayOfPlates = new int[Constants.NUMBER_OF_TABLES][Constants.CAPACITY_OF_PLATE];

//	public static boolean[][] booleanOfPlates = new boolean[Constants.NUMBER_OF_TABLES][Constants.CAPACITY_OF_PLATE];
	
	
	//已经存在的空位
	public static int[] existingOfVacant = new int[Constants.CAPACITY_OF_PLATE];
	
	public Base() {

//		for (int i = 0; i < booleanOfPlates.length; i++) {
//			for (int j = 0; j < booleanOfPlates[i].length; j++) {
//				booleanOfPlates[i][j] = true;
//			}
//		}
	}
	
	/**
	 * 将存在的序列递增存储到existingOfVacant
	 *
	 * @param tableNumber 桌子的序号
	 * @return
	 */
	public static int[] calculateOfExist(int tableNumber) {
		//临时变量，用于递增存储到existingOfVacant里面。
		int count = 0;
		for (int i = 0; i < arrayOfPlates[tableNumber].length; i++) {
			if (arrayOfPlates[tableNumber][i] == 1) {
				existingOfVacant[count++] = i;
			}
		}
		//使得数组空间利用率达到100%，长度即等于剩余存在烧烤数量
		return Arrays.copyOf(existingOfVacant, count);
	}
	
	public static int[] calculate() {
		int[] countOfPlates = new int[Constants.NUMBER_OF_TABLES];
		for (int i = 0; i < arrayOfPlates.length; i++) {
			for (int j = 0; j < arrayOfPlates[i].length; j++) {
				if (arrayOfPlates[i][j] == 1) {
					countOfPlates[i]++;
				}
			}
		}
		return countOfPlates;
	}
	
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
			if (BarbecueRestaurant.lockOfTable.tryLock(5, TimeUnit.SECONDS))
				System.out.println("GrillMaster " + this.id + "号 " + this.name + "进入锁了");
			//TODO 制作就是简单的制作，从第一个开始填充，如果遇到已经填充的就跳过，填充下一个，直到填充完毕
			int n = 0;
			for (int i = this.id; i < arrayOfPlates.length; i++) {
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
			BarbecueRestaurant.lockOfTable.unlock();
			int[] exist = Base.calculateOfExist(0);
			for (int i : exist) {
				System.out.print(i + " ");
			}
			System.out.println();
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
	private boolean flag = false;
	
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
	
	//顾客拿烧烤的过程，保证厨师对桌子互斥，保证顾客对自己拿取的单个空位的互斥
	public void take() throws InterruptedException {
		Thread.sleep(500);
		System.out.println("Customer " + this.id + "号 " + this.name + "进入take()了");
		long endTime = System.currentTimeMillis() + 10000; // 10秒的结束时间
		while (System.currentTimeMillis() < endTime) {
			if (calculateOfExist(this.id).length != 0) {
				if (BarbecueRestaurant.lockOfTable.tryLock(5, TimeUnit.SECONDS)) {
					this.flag = true;
				}
				System.out.println("Customer " + this.id + "号 " + this.name + "进入锁了");
				// 2秒的拿取时间，如果两秒内反复获取都获取不到，直接结束程序即可
				long endTimeOfTaking = System.currentTimeMillis() + 2000;
				while (System.currentTimeMillis() < endTimeOfTaking) {
					int[] exist = calculateOfExist(this.id);
					if (exist.length == 0) {
						continue;
					}
					int j = exist[(int) (Math.random() * exist.length)];
					if (Base.arrayOfPlates[this.id][j] == 1 && BarbecueRestaurant.locksOfEachSlot[this.id][j].tryLock(1, TimeUnit.SECONDS)) {
						Base.arrayOfPlates[this.id][j] = 0;
						System.out.println("Customer " + this.id + "号 " + this.name + "拿走了" + j + "个烧烤");
						BarbecueRestaurant.locksOfEachSlot[this.id][j].unlock();
					}
					if (this.flag) {
						System.out.println("Customer " + this.id + "号 " + this.name + "出桌子的锁了");
						this.flag = false;
						BarbecueRestaurant.lockOfTable.unlock();
					}
					//顾客拿完了烤串，得吃一会，并且还需要呵呵酒，说说话
					//所以这时候应该放开对桌子和对空位的锁，并且进入睡眠一段时间，等恢复好了再拿新的烤串
					Thread.sleep(3000);
					BarbecueRestaurant.printStateOfPlates();
					return;
				}
				System.out.println("Customer " + this.id + "号 " + this.name + "等待太久了，半天拿不到烧烤");
				System.exit(0);
			}
		}
		System.out.println("Customer " + this.id + "号 " + this.name + "等待太久了，进不去桌子了");
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
	
	//对于桌子的锁，用于厨子和顾客
	public static final Lock lockOfTable = new ReentrantLock();
	//给每个桌子的空位都上把锁，用于顾客与顾客的互斥
	public static final Lock[][] locksOfEachSlot = new ReentrantLock[Constants.NUMBER_OF_TABLES][Constants.CAPACITY_OF_PLATE];
	
	
	/**
	 * 入口
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		init();
	}
	
	
	/**
	 * 格式化输出 TODO 目前用不上，等到了完整程序大体出来了再根据具体情况格式化展示
	 *
	 * @param a 需要格式化展示的数组，TODO 目前不确定，可能以后也要改
	 */
//	public static void printFormat(int[] a) {
//		for (int num : a) {
//			System.out.println(num);
//		}
//	}
	
	/**
	 * 初始化
	 */
	public static void init() {
		for (int i = 0; i < locksOfEachSlot.length; i++) {
			for (int j = 0; j < locksOfEachSlot[i].length; j++) {
				locksOfEachSlot[i][j] = new ReentrantLock();
			}
		}
		Thread grillMaster1 = new Thread(new GrillMaster("Pa3da", 1));
		Thread customer1 = new Thread(new Customer("Matinal", 1));
		Thread customer2 = new Thread(new Customer("zhy2", 1));
		Thread customer3 = new Thread(new Customer("zhy3", 1));
		Thread customer4 = new Thread(new Customer("zhy4", 1));
		Thread customer5 = new Thread(new Customer("zhy5", 1));
		Thread customer6 = new Thread(new Customer("zhy6", 1));
		Thread customer7 = new Thread(new Customer("zhy7", 1));
		Thread customer8 = new Thread(new Customer("zhy8", 1));
		Thread customer9 = new Thread(new Customer("zhy9", 1));
		Thread customer10 = new Thread(new Customer("zhy10", 1));
		Thread customer11 = new Thread(new Customer("zhy11", 1));
		Thread customer12 = new Thread(new Customer("zhy12", 1));
		
		grillMaster1.start();
		customer1.start();
		customer2.start();
		customer3.start();
		customer4.start();
		customer5.start();
		customer6.start();
		customer7.start();
		customer8 .start();
		customer9 .start();
		customer10.start();
		customer11.start();
		customer12.start();
	}
	
	/**
	 * 展示基本情况，包括数据具体情况和每个数组包含数量情况
	 */
	public static void printStateOfPlates() {
		System.out.println("每个数组具体情况展示：");
		for (int i = 0; i < Base.arrayOfPlates.length; i++) {
			for (int j = 0; j < Base.arrayOfPlates[i].length; j++) {
				System.out.print(Base.arrayOfPlates[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println("每个数组包含的烧烤数量：");
		for (int i = 0; i < Constants.NUMBER_OF_TABLES; i++) {
			System.out.print(Base.calculate()[i] + " ");
		}
		System.out.println();
	}
}

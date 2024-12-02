import java.util.Arrays;
import java.util.concurrent.*;

/**
 * 父类
 */
abstract class Base implements Runnable {
	
	public int id;
	public String name;
	
	
	/**
	 * 将存在的序列递增存储到existingOfVacant
	 *
	 * @param tableNumber 桌子的序号
	 * @return 返回100%利用率的existingOfVacant
	 */
	public static int[] calculateOfExist(int tableNumber) {
		//临时变量，用于递增存储到existingOfVacant里面。
		int count = 0;
		for (int i = 0; i < BarbecueRestaurant.arrayOfPlates[tableNumber].length; i++) {
			if (BarbecueRestaurant.arrayOfPlates[tableNumber][i] == Constants.EXISTENCE_BBQ) {
				BarbecueRestaurant.existingOfVacant[count++] = i;
			}
		}
		//使得数组空间利用率达到100%，长度即等于剩余存在烧烤数量
		return Arrays.copyOf(BarbecueRestaurant.existingOfVacant, count);
	}
	
	/**
	 * 用于计算目前所有桌子的所有空位情况
	 *
	 * @return 返回每张桌子的烧烤数
	 */
	public static int[] calculate() {
		int[] countOfPlates = new int[Constants.NUMBER_OF_TABLES];
		for (int i = 0; i < BarbecueRestaurant.arrayOfPlates.length; i++) {
			for (int j = 0; j < BarbecueRestaurant.arrayOfPlates[i].length; j++) {
				if (BarbecueRestaurant.arrayOfPlates[i][j] == Constants.EXISTENCE_BBQ) {
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
			for (int i = 0; i < 2; i++) {
				makingBBQ();
				Thread.sleep(2000 + (int) (Math.random() * Constants.TIME_OF_MAKING_BBQ));
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void makingBBQ() throws InterruptedException {
		long endTime = System.currentTimeMillis() + 10000; // 10秒的结束时间
		while (System.currentTimeMillis() < endTime) {
			if (!BarbecueRestaurant.hasCustomer && BarbecueRestaurant.tableSemaphores[this.id].tryAcquire(1, TimeUnit.SECONDS)) {
				PrintMessage.messageQueue.add(PrintMessage.toBeStringMessage("GrillMaster " + this.id + "号桌 " + this.name + "进入缓冲区了"));
				//TODO 制作就是简单的制作，从第一个开始填充，如果遇到已经填充的就跳过，填充下一个，直到填充完毕
				int n = 0;
				for (int i = this.id; i < BarbecueRestaurant.arrayOfPlates.length; i++) {
					for (int j = 0; j < BarbecueRestaurant.arrayOfPlates[i].length; j++) {
						if (BarbecueRestaurant.arrayOfPlates[i][j] == Constants.NO_EXISTENCE_BBQ && n < 20) {
							BarbecueRestaurant.arrayOfPlates[i][j] = Constants.EXISTENCE_BBQ;
							n++;
						} else if (n >= 10) {
							break;
						}
					}
					if (n >= 10) {
						break;
					}
				}
//				Thread.sleep(Constants.TIME_OF_MAKING_BBQ);
//				BarbecueRestaurant.printStateOfPlates();
				PrintMessage.messageQueue.add(PrintMessage.toBeStringMessage("GrillMaster " + this.id + "号桌 " + this.name + "离开缓冲区了"));
				int[] exist = Base.calculateOfExist(this.id);
				StringBuilder sb = new StringBuilder();
				for (int i : exist) {
					sb.append(i + " ");
				}
				PrintMessage.messageQueue.add(PrintMessage.toBeStringMessage(sb.toString()));
				BarbecueRestaurant.tableSemaphores[this.id].release();
				return;
			}
			//等待顾客全部让开
			Thread.sleep(1000 + (int) (Math.random() * Constants.TIME_OF_GRILLMASTER_SLEEP));
//			BarbecueRestaurant.printStateOfPlates();
		}
		PrintMessage.messageQueue.add(PrintMessage.toBeStringMessage(this.name + "等待太久了，进不去"));
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
		
		//1~6次
		int randomEatNumber = (int) (Math.random() * Constants.BASE_NUMBER_of_CUSTOMER_EAT_BBQ);
		try {
			//第一次先让厨师先做
			Thread.sleep(500 + (int) (Math.random() * Constants.TIME_OF_CUSTOMER_SLEEP));
			for (int i = 0; i < randomEatNumber; i++) {
				take();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	//顾客拿烧烤的过程，保证厨师对桌子互斥，保证顾客对自己拿取的单个空位的互斥
	public void take() throws InterruptedException {
		long endTime = System.currentTimeMillis() + 30000; // 30秒的结束时间
		while (System.currentTimeMillis() < endTime) {
			//所在的桌上存在烧烤
			//且确认没有厨师
			if (calculateOfExist(this.id).length > 0 &&
					BarbecueRestaurant.tableSemaphores[this.id].tryAcquire(10, TimeUnit.MILLISECONDS)) {
				BarbecueRestaurant.tableSemaphores[this.id].release();
				BarbecueRestaurant.hasCustomer = true;
				PrintMessage.messageQueue.add(PrintMessage.toBeStringMessage("Customer " + this.id + "号桌 " + this.name + "进入缓冲区了"));
				// 2秒的拿取时间，如果两秒内反复获取都获取不到，直接结束程序即可
				long endTimeOfTaking = System.currentTimeMillis() + 2000;
				while (System.currentTimeMillis() < endTimeOfTaking) {
					int[] exist = calculateOfExist(this.id);
					if (exist.length == 0) {
						continue;
					}
					int j = exist[(int) (Math.random() * exist.length)];
					if (BarbecueRestaurant.arrayOfPlates[this.id][j] == Constants.EXISTENCE_BBQ && !BarbecueRestaurant.hasCustomerInSlot[this.id][j]) {
						BarbecueRestaurant.hasCustomerInSlot[this.id][j] = true;
						BarbecueRestaurant.arrayOfPlates[this.id][j] = Constants.NO_EXISTENCE_BBQ;
						PrintMessage.messageQueue.add(PrintMessage.toBeStringMessage("Customer " + this.id + "号桌 " + this.name + "拿走了" + j + "号位置的烧烤"));
						BarbecueRestaurant.hasCustomerInSlot[this.id][j] = false;
						BarbecueRestaurant.hasCustomer = false;
						PrintMessage.messageQueue.add(PrintMessage.toBeStringMessage("Customer " + this.id + "号桌 " + this.name + "离开缓冲区了"));
						//顾客拿完了烤串，得吃一会，并且还需要呵呵酒，说说话
						//所以这时候应该放开对桌子和对空位的锁，并且进入睡眠一段时间，等恢复好了再拿新的烤串
						Thread.sleep(3000 + (int) (Math.random() * Constants.TIME_OF_CUSTOMER_SLEEP));
						return;
					}
				}
				PrintMessage.messageQueue.add(PrintMessage.toBeStringMessage("Customer " + this.id + "号桌 " + this.name + "等待太久了，半天拿不到烧烤"));
				System.exit(0);
			}
		}
		PrintMessage.messageQueue.add(PrintMessage.toBeStringMessage("Customer " + this.id + "号桌 " + this.name + "等待太久了，进不去桌子了"));
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
	//不存在烧烤
	public static int NO_EXISTENCE_BBQ = 0;
	//烧烤存在
	public static int EXISTENCE_BBQ = 1;
	
	//厨师制作烧烤时间
	public static int TIME_OF_MAKING_BBQ = 6000;
	//顾客随机等待时间基数
	public static int TIME_OF_CUSTOMER_SLEEP = 3000;
	//厨师随机等待时间基数
	public static int TIME_OF_GRILLMASTER_SLEEP = 3000;
	//固定间隔时间输出当前状况
	public static int TIME_OF_PRINTMESSAGE = 1000;
	//顾客吃烧烤的次数的基数 初步定为 1~6次，由于方法特性会导致变成0~5，所以得+1
	public static int BASE_NUMBER_of_CUSTOMER_EAT_BBQ = 5;
	
}


class PrintMessage {
	// 创建一个线程安全的队列用于存储完整的消息
	public static final ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<>();
	
	/**
	 * 用于专门负责打印的线程执行的函数
	 */
	public static void processAndPrintMessages() {
//		long endTimeOfPrint = System.currentTimeMillis() + 20000; // 20秒的结束时间
		do {
			String message = messageQueue.poll(); // 从队列中获取并移除消息
			if (message != null) {
				System.out.print(message); // 输出消息
			} else {
				try {
					Thread.sleep(100); // 如果队列为空，稍作等待
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		} while (((ThreadPoolExecutor) BarbecueRestaurant.executorService).getActiveCount() > 0);
	}
	
	/**
	 * 固定时间输出当前状况，并且在厨师和顾客线程都结束后结束
	 */
	public static void displayCondition() {
		do {
			BarbecueRestaurant.printStateOfPlates();
			try {
				Thread.sleep(Constants.TIME_OF_PRINTMESSAGE);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		} while (((ThreadPoolExecutor) BarbecueRestaurant.executorService).getActiveCount() > 0);
	}
	
	
	/**
	 * 格式化需要打印的信息
	 *
	 * @param strings
	 * @return
	 */
	public static String toBeStringMessage(String... strings) {
		StringBuilder sb = new StringBuilder();
		for (String s : strings) {
			sb.append(s);
		}
		sb.append("\n");
		return sb.toString();
	}
	
	
}

/**
 * 程序入口类
 */
public class BarbecueRestaurant {
	
	//对于桌子的锁，用于厨子和顾客
//	public static final Lock lockOfTable = new ReentrantLock();
	//给每个桌子的空位都上把锁，用于顾客与顾客的互斥
//	public static final Lock[][] locksOfEachSlot = new ReentrantLock[Constants.NUMBER_OF_TABLES][Constants.CAPACITY_OF_PLATE];
	//空位的总体数组
	public static int[][] arrayOfPlates = new int[Constants.NUMBER_OF_TABLES][Constants.CAPACITY_OF_PLATE];
	//已经存在的空位
	public static int[] existingOfVacant = new int[Constants.CAPACITY_OF_PLATE];
	//保证厨师和顾客们对于每个桌子的互斥
	public static final Semaphore[] tableSemaphores = new Semaphore[Constants.NUMBER_OF_TABLES];
	//标志位，指示是否有顾客
	public static volatile boolean hasCustomer = false;
	//保证顾客和顾客之间对于每个烧烤的互斥
	public static volatile boolean[][] hasCustomerInSlot = new boolean[Constants.NUMBER_OF_TABLES][Constants.CAPACITY_OF_PLATE];
	//线程池
	public static ExecutorService executorService;
	
	/**
	 * 入口
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		init();
	}
	
	
	/**
	 * 初始化
	 */
	public static void init() {
		for (int i = 0; i < Constants.NUMBER_OF_TABLES; i++) {
			tableSemaphores[i] = new Semaphore(1); // 每个桌子只有一个许可
		}
		
		// 创建一个线程池
		executorService = Executors.newFixedThreadPool(8); // 线程池大小为工作线程数量之和
		
		
		// 定义 GrillMaster 和 Customer 的名字和 ID
		String[] grillMasterNames = {"Pa3da0", "Pa3da1", "Pa3da2"};
		int[] grillMasterIds = {0, 1, 2};
		
		String[] customerNames = {"Matinal0", "zhy0", "zhy1", "zhy11", "zhy2", "zhy22"};
		int[] customerIds = {0, 0, 1, 1, 2, 2};
		
		// 提交 GrillMaster 任务
		for (int i = 0; i < grillMasterNames.length; i++) {
			executorService.submit(new GrillMaster(grillMasterNames[i], grillMasterIds[i]));
		}
		
		// 提交 Customer 任务
		for (int i = 0; i < customerNames.length; i++) {
			executorService.submit(new Customer(customerNames[i], customerIds[i]));
		}
		
		//保证所有线程输出的顺序性
		new Thread(PrintMessage::processAndPrintMessages).start();
		//保证每1秒钟输出一次当前状况，并且在线程都结束后自己结束
		new Thread(PrintMessage::displayCondition).start();
		
		// 停止接收新的任务并关闭线程池，在这里为了确保应用程序的资源能够得以释放，并正确地退出。
		// 即使在简单的程序中，也建议在不再需要线程池时调用以确保程序的健壮性。
		executorService.shutdown();
	}
	
	/**
	 * 展示基本情况，包括数据具体情况和每个数组包含数量情况
	 */
	public static void printStateOfPlates() {
		StringBuilder sb = new StringBuilder();
		sb.append("每个数组具体情况展示：\n");
		for (int i = 0; i < BarbecueRestaurant.arrayOfPlates.length; i++) {
			for (int j = 0; j < BarbecueRestaurant.arrayOfPlates[i].length; j++) {
				sb.append(BarbecueRestaurant.arrayOfPlates[i][j] + " ");
			}
			sb.append("\n");
		}
		sb.append("每个数组包含的烧烤数量：\n");
		for (int i = 0; i < Constants.NUMBER_OF_TABLES; i++) {
			sb.append(Base.calculate()[i] + " ");
		}
		sb.append("\n");
		PrintMessage.messageQueue.add(sb.toString());
	}
}

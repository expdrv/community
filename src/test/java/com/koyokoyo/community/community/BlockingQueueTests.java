package com.koyokoyo.community.community;

import org.aspectj.lang.ProceedingJoinPoint;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueTests {
    public static void main(String[]args)
    {
        BlockingQueue<Integer> queue=new ArrayBlockingQueue<>(10);
        new Thread(new Producer(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();
    }
}
interface PCConst
{
    public static final int EOQ=-1;
}
class Producer implements Runnable,PCConst
{
    private BlockingQueue<Integer> queue;

    public Producer(BlockingQueue<Integer> queue)
    {
        this.queue=queue;
    }
    @Override
    public void run() {
        try
        {
                for(int i=0;i<100;i++)
                {
                    Thread.sleep(40);
                    queue.put(i);
                    System.out.println(Thread.currentThread().getName()+"生产"+queue.size());
                }
                queue.put(EOQ);
                queue.put(EOQ);
                queue.put(EOQ);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

class Consumer implements Runnable,PCConst
{
    private BlockingQueue<Integer> queue;

    public Consumer(BlockingQueue<Integer> queue)
    {
        this.queue=queue;
    }
    @Override
    public void run() {
        try
        {
            while(true)
            {
                Thread.sleep(new Random().nextInt(1000));
                if(queue.take()==EOQ) break;
                System.out.println(Thread.currentThread().getName()+"消费"+queue.size());
            }
            System.out.println(Thread.currentThread().getName()+"停止消费");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
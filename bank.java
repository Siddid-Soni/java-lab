import java.util.Scanner;
import java.io.*;
import java.nio.file.*;

class BankAccount {
    private int balance;
    private final String balanceFilePath = "balance.txt";

    public BankAccount(int initialBalance) {
        if (Files.exists(Paths.get(balanceFilePath))) {
            this.balance = readBalanceFromFile();
            System.out.println("Loaded existing balance: " + this.balance);
        } else {
            this.balance = initialBalance;
            saveBalanceToFile();
            System.out.println("Created new account with balance: " + this.balance);
        }
    }

    private void saveBalanceToFile() {
        try {
            FileWriter Writer = new FileWriter("myfile.txt");
            Writer.write(String.valueOf(balance));
            Writer.close();
        }
        catch (IOException e) {
            System.out.println("An error has occurred.");
            e.printStackTrace();
        }
    }

    private int readBalanceFromFile() {
        int data = 0;
        try {
            File Obj = new File("myfile.txt");
            Scanner Reader = new Scanner(Obj);
            data = Integer.parseInt(Reader.nextLine());
            Reader.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("An error has occurred.");
            e.printStackTrace();
        }
        return data;
    }

    public synchronized void deposit(int amount) {
        System.out.println(Thread.currentThread().getName() + " trying to deposit " + amount);

        balance += amount;

        saveBalanceToFile();
        System.out.println("Deposit successful. New balance: " + balance);
    
        notify();
    }

    public synchronized void withdraw(int amount) {
        System.out.println(Thread.currentThread().getName() + " trying to withdraw " + amount);

        while (balance < amount) {
            try {
                System.out.println("Insufficient balance. Waiting for deposit...");
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        balance -= amount;
        
        saveBalanceToFile();
        System.out.println("Withdrawal successful. New balance: " + balance);
        
    }

    public int getBalance() {
        return readBalanceFromFile();
        
    }
}

class DepositThread extends Thread {
    private BankAccount account;
    private int amount;

    public DepositThread(BankAccount account, int amount, String name) {
        super(name);
        this.account = account;
        this.amount = amount;
    }

    @Override
    public void run() {
        account.deposit(amount);
    }
}

class WithdrawThread extends Thread {
    private BankAccount account;
    private int amount;

    public WithdrawThread(BankAccount account, int amount, String name) {
        super(name);
        this.account = account;
        this.amount = amount;
    }

    @Override
    public void run() {
        account.withdraw(amount);
    }
}

public class bank {
    public static void main(String[] args) {
        // Create account with initial balance of 1000 and withdraw limit of 5000
        BankAccount account = new BankAccount(1000);

        // Create multiple deposit and withdraw threads
        Thread d1 = new DepositThread(account, 2000, "Depositor-1");
        Thread d2 = new DepositThread(account, 3000, "Depositor-2");
        Thread w1 = new WithdrawThread(account, 4000, "Withdrawer-1");
        Thread w2 = new WithdrawThread(account, 1500, "Withdrawer-2");

        // Start the threads
        w1.start();  // This will wait initially as balance is insufficient
        d1.start();  // This will deposit and notify waiting threads
        w2.start();  // This will proceed as balance is sufficient
        d2.start();  // This will deposit more money

        // Wait for all threads to complete
        try {
            d1.join();
            d2.join();
            w1.join();
            w2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Final balance: " + account.getBalance());
    }
}
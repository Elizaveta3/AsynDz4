import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class task2 {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        // 1. Асинхронний початок із паузою
        CompletableFuture<Void> startAsync = CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
                printWithTime("Starting computations after pause...", start);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 2. Генерація одновимірного масиву з 20 випадкових натуральних чисел
        CompletableFuture<int[]> generateArray = startAsync.thenCompose(v -> CompletableFuture.supplyAsync(() -> {
            int[] array = ThreadLocalRandom.current().ints(20, 1, 101).toArray(); // Генеруємо числа від 1 до 100 включно
            printWithTime("Generated array: " + Arrays.toString(array), start);
            return array;
        }));

        // 3. Обчислення мінімуму серед (a1 + a2, a2 + a3, ...)
        CompletableFuture<Integer> minPairSum = generateArray.thenApplyAsync(array -> {
            int minSum = Integer.MAX_VALUE;
            for (int i = 0; i < array.length - 1; i++) {
                int pairSum = array[i] + array[i + 1];
                minSum = Math.min(minSum, pairSum);
            }
            printWithTime("Minimum pair sum: " + minSum, start);
            return minSum;
        });

        // 4. Виведення результату
        CompletableFuture<Void> resultOutput = minPairSum.thenAcceptAsync(minSum -> {
            printWithTime("Final result: Minimum pair sum is " + minSum, start);
        });

        // 5. Асинхронне очищення ресурсів
        resultOutput.thenRunAsync(() -> {
            printWithTime("Cleaning up resources...", start);
        });

        // Очікування завершення всіх асинхронних завдань
        resultOutput.join();
    }

    private static void printWithTime(String message, long start) {
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("[" + elapsed + " ms] " + message);
    }
}


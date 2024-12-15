import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class task1 {

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

        // 2. Генерація одновимірного масиву з 10 випадкових цілих чисел
        CompletableFuture<int[]> generateArray = startAsync.thenCompose(v -> CompletableFuture.supplyAsync(() -> {
            int[] array = ThreadLocalRandom.current().ints(10, 1, 11).toArray(); // Генеруємо числа від 1 до 10 включно
            printWithTime("Generated array: " + Arrays.toString(array), start);
            return array;
        }));

        // 3. Збільшення кожного елемента масиву на 5
        CompletableFuture<int[]> incrementedArray = generateArray.thenApplyAsync(array -> {
            int[] incremented = Arrays.stream(array).map(n -> n + 5).toArray();
            printWithTime("Incremented array: " + Arrays.toString(incremented), start);
            return incremented;
        });

        // 4. Знаходження факторіалу від суми елементів масивів
        CompletableFuture<Void> factorialResult = generateArray.thenCombineAsync(incrementedArray, (original, incremented) -> {
            int originalSum = Arrays.stream(original).sum();
            int incrementedSum = Arrays.stream(incremented).sum();
            BigInteger totalSum = BigInteger.valueOf(originalSum + incrementedSum);
            BigInteger factorial = factorial(totalSum);
            printWithTime("Factorial of total sum (" + totalSum + "): " + factorial, start);
            return factorial;
        }).thenAcceptAsync(result -> {
            printWithTime("Computation finished", start);
        });

        // 5. Асинхронне очищення ресурсів
        factorialResult.thenRunAsync(() -> {
            printWithTime("Cleaning up resources...", start);
        });

        // Очікування завершення всіх асинхронних завдань
        factorialResult.join();
    }

    private static BigInteger factorial(BigInteger n) {
        BigInteger result = BigInteger.ONE;
        for (BigInteger i = BigInteger.TWO; i.compareTo(n) <= 0; i = i.add(BigInteger.ONE)) {
            result = result.multiply(i);
        }
        return result;
    }

    private static void printWithTime(String message, long start) {
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("[" + elapsed + " ms] " + message);
    }
}

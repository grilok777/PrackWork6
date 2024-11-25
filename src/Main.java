import java.util.Random;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Програма аналізу даних сенсорів запущена.");

        // Створюємо ExecutorService з фіксованим пулом потоків
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        // runAsync: Повідомлення про початок збору даних
        CompletableFuture<Void> startTask = CompletableFuture.runAsync(() -> {
            System.out.println("runAsync: Починаємо збір даних із сенсорів...");
        }, executorService);

        // supplyAsync: Генерація даних із сенсорів
        CompletableFuture<int[]> sensorDataTask = CompletableFuture.supplyAsync(() -> {
            System.out.println("supplyAsync: Генеруємо дані сенсорів...");
            Random random = new Random();
            int[] data = new int[10]; // 10 сенсорів
            for (int i = 0; i < data.length; i++) {
                data[i] = random.nextInt(35, 101); // Температура від 35°C до 100°C
            }
            return data;
        }, executorService);

        // thenApplyAsync: Аналіз даних (обчислення середньої температури)
        CompletableFuture<Double> analysisTask = sensorDataTask.thenApplyAsync(data -> {
            System.out.println("thenApplyAsync: Аналізуємо отримані дані...");
            double sum = 0;
            for (int temp : data) {
                sum += temp;
            }
            return sum / data.length;
        }, executorService);

        // thenApplyAsync: Обчислення стандартного відхилення
        CompletableFuture<Double> deviationTask = analysisTask.thenApplyAsync(avgTemperature -> {
            System.out.println("thenApplyAsync: Обчислюємо стандартне відхилення...");
            double sumOfSquares = 0;
            for (int temp : sensorDataTask.join()) {
                sumOfSquares += Math.pow(temp - avgTemperature, 2);
            }
            return Math.sqrt(sumOfSquares / sensorDataTask.join().length);
        }, executorService);

        // thenAcceptAsync: Виведення результатів
        CompletableFuture<Void> printResultTask = deviationTask.thenAcceptAsync(deviation -> {
            System.out.println("thenAcceptAsync: Середня температура за даними сенсорів:");
            System.out.printf("Середнє: %.2f°C, Стандартне відхилення: %.2f°C\n", analysisTask.join(), deviation);
        }, executorService);

        // thenRunAsync: Повідомлення про завершення
        CompletableFuture<Void> finishTask = printResultTask.thenRunAsync(() -> {
            System.out.println("thenRunAsync: Обробка даних завершена.");
        }, executorService);

        // Очікування завершення всіх задач
        try {
            CompletableFuture.allOf(startTask, finishTask).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("Програма завершена.");

        // Завершуємо роботу ExecutorService
        executorService.shutdown();
    }
}
package com.example.app10;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import java.util.concurrent.Callable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CounterFragment extends Fragment {

    private ExecutorService executor;
    private TextView statusTextView;
    private TextView secondsPassedTextView;
    private TextView taskTimeTextView;
    private int secondsPassed = 0;
    private boolean isCounterRunning = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_counter, container, false);

        // Инициализируем ExecutorService с фиксированным количеством потоков
        executor = Executors.newFixedThreadPool(5);

        Button startButton = rootView.findViewById(R.id.startNumberButton);
        Button stopButton = rootView.findViewById(R.id.stopNumberButton);
        statusTextView = rootView.findViewById(R.id.numberStatusTextView);
        secondsPassedTextView = rootView.findViewById(R.id.numberCounterTextView);
        taskTimeTextView = rootView.findViewById(R.id.taskTimeTextView);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTasks();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTasks();
            }
        });

        startCounterThread();

        Button nextScreenButton = rootView.findViewById(R.id.button_to_second_screen);
        nextScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageBlurFragment imageBlurFragment = new ImageBlurFragment();
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, imageBlurFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        Runnable runnableTask = () -> {
            try {
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Callable<String> callableTask = () -> {
            try {
                TimeUnit.MILLISECONDS.sleep(300);
                return "Task's execution";
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        };

        List<Callable<String>> callableTasks = new ArrayList<>();
        callableTasks.add(callableTask);
        callableTasks.add(callableTask);
        callableTasks.add(callableTask);

        // Примеры использования submit(), invokeAny(), invokeAll(), execute() и shutdown()
        executor.submit(runnableTask);
        executor.execute(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        try {
            String result = executor.invokeAny(callableTasks);
            System.out.println("Result of any task: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            List<Future<String>> futures = executor.invokeAll(callableTasks);
            List<String> results = new ArrayList<>();
            for (Future<String> future : futures) {
                results.add(future.get());
            }
            for (String result : results) {
                System.out.println("Result of task: " + result);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


        return rootView;
    }

    private void startCounterThread() {
        isCounterRunning = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isCounterRunning) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    secondsPassed++;
                    // Обновляем UI через главный поток активности
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            secondsPassedTextView.setText("Приложение запущено: " + secondsPassed + " секунд");
                        }
                    });
                }
            }
        }).start();
    }

    private void startTasks() {
        if (executor != null && !executor.isShutdown()) {
            final long startTimeMillis = System.currentTimeMillis();
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    updateStatus("Задача начала выполнение");
                    try {
                        Thread.sleep(10000); // Ваша задача
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    updateStatus("Задача выполнена");
                    long endTimeMillis = System.currentTimeMillis();
                    final long executionTimeSeconds = (endTimeMillis - startTimeMillis) / 1000;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            taskTimeTextView.setText("Задача выполнялась: " + executionTimeSeconds + " секунд");
                        }
                    });
                }
            });
        } else {
            updateStatus("ExecutorService уже завершен");
        }
    }


    private void stopTasks() {
        if (executor != null) {
            executor.shutdownNow();
            updateStatus("Задача остановлена");
        }
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        if (executor != null) {
//            executor.shutdownNow();
//        }
//        isCounterRunning = false;
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isCounterRunning = false; // Остановка счетчика времени
        if (executor != null) {
            executor.shutdown(); // Остановка ExecutorService
            try {
                if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }

    private void updateStatus(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusTextView.setText(message);
            }
        });
    }
}

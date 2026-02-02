package com.example.piggybank;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // Элементы интерфейса
    private TextView statusText;
    private TextView totalAmountText;
    private TextView totalCoinsText;
    private TextView coin50Text, coin1Text, coin2Text, coin5Text, coin10Text;
    private TextView logText;
    private Button connectButton;
    private Button resetButton;
    private Button calibrateButton;

    // Данные копилки (соответствуют вашему Arduino коду)
    private boolean isConnected = false;
    private float totalAmount = 0.0f;
    private int totalCoins = 0;
    private int[] coinCounts = {0, 0, 0, 0, 0}; // 50коп, 1руб, 2руб, 5руб, 10руб
    private float[] coinValues = {0.5f, 1.0f, 2.0f, 5.0f, 10.0f};

    // Имитация работы с Arduino
    private Handler handler = new Handler();
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupClickListeners();
        updateUI();
    }

    private void initViews() {
        statusText = findViewById(R.id.statusText);
        totalAmountText = findViewById(R.id.totalAmountText);
        totalCoinsText = findViewById(R.id.totalCoinsText);
        coin50Text = findViewById(R.id.coin50Text);
        coin1Text = findViewById(R.id.coin1Text);
        coin2Text = findViewById(R.id.coin2Text);
        coin5Text = findViewById(R.id.coin5Text);
        coin10Text = findViewById(R.id.coin10Text);
        logText = findViewById(R.id.logText);
        connectButton = findViewById(R.id.connectButton);
        resetButton = findViewById(R.id.resetButton);
        calibrateButton = findViewById(R.id.calibrateButton);
    }

    private void setupClickListeners() {
        // Кнопка подключения
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    disconnectFromPiggyBank();
                } else {
                    connectToPiggyBank();
                }
            }
        });

        // Кнопка сброса
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResetConfirmationDialog();
            }
        });

        // Кнопка калибровки
        calibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalibrationDialog();
            }
        });

        // Имитация добавления монет (долгое нажатие на общую сумму)
        totalAmountText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (isConnected) {
                    simulateCoinInsertion();
                }
                return true;
            }
        });
    }

    private void connectToPiggyBank() {
        addToLog("Попытка подключения к копилке...");

        // Имитация процесса подключения
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isConnected = true;
                statusText.setText("Подключено");
                statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                connectButton.setText("Отключить");
                addToLog("Успешно подключено к копилке");

                // Запускаем имитацию получения данных
                startDataSimulation();
            }
        }, 2000);
    }

    private void disconnectFromPiggyBank() {
        isConnected = false;
        statusText.setText("Не подключено");
        statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        connectButton.setText("Подключить");
        addToLog("Отключено от копилки");

        // Останавливаем имитацию
        handler.removeCallbacksAndMessages(null);
    }

    private void startDataSimulation() {
        // Имитация периодического обновления данных
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isConnected) {
                    // С вероятностью 10% добавляем монету
                    if (random.nextInt(100) < 10) {
                        simulateCoinInsertion();
                    }
                    handler.postDelayed(this, 5000); // Проверка каждые 5 секунд
                }
            }
        }, 5000);
    }

    private void simulateCoinInsertion() {
        int coinIndex = random.nextInt(5); // Случайная монета
        coinCounts[coinIndex]++;
        totalCoins++;
        totalAmount += coinValues[coinIndex];

        updateUI();

        String coinName = getCoinName(coinIndex);
        addToLog("Добавлена монета: " + coinName);
    }

    private String getCoinName(int index) {
        switch (index) {
            case 0: return "50 копеек";
            case 1: return "1 рубль";
            case 2: return "2 рубля";
            case 3: return "5 рублей";
            case 4: return "10 рублей";
            default: return "Неизвестная монета";
        }
    }

    private void showResetConfirmationDialog() {
        if (!isConnected) {
            addToLog("Ошибка: нет подключения к копилке");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Сброс статистики")
                .setMessage("Вы уверены, что хотите сбросить всю статистику? Это действие нельзя отменить.")
                .setPositiveButton("Сбросить", (dialog, which) -> {
                    resetStatistics();
                    addToLog("Статистика сброшена");
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void resetStatistics() {
        totalAmount = 0.0f;
        totalCoins = 0;
        for (int i = 0; i < coinCounts.length; i++) {
            coinCounts[i] = 0;
        }
        updateUI();
    }

    private void showCalibrationDialog() {
        if (!isConnected) {
            addToLog("Ошибка: нет подключения к копилке");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Калибровка копилки")
                .setMessage("Для калибровки последовательно вставьте монеты каждого номинала:\n\n" +
                        "1. 50 копеек\n" +
                        "2. 1 рубль\n" +
                        "3. 2 рубля\n" +
                        "4. 5 рублей\n" +
                        "5. 10 рублей\n\n" +
                        "Система автоматически запомнит параметры каждой монеты.")
                .setPositiveButton("Начать калибровку", (dialog, which) -> {
                    startCalibrationProcess();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void startCalibrationProcess() {
        addToLog("Запуск процесса калибровки...");

        // Имитация процесса калибровки
        for (int i = 0; i < 5; i++) {
            final int coinIndex = i;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String coinName = getCoinName(coinIndex);
                    addToLog("Калибровка монеты: " + coinName);

                    if (coinIndex == 4) { // Последняя монета
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                addToLog("Калибровка завершена успешно!");
                            }
                        }, 1000);
                    }
                }
            }, i * 2000);
        }
    }

    private void updateUI() {
        // Обновляем общую сумму
        totalAmountText.setText(String.format(Locale.getDefault(), "%.2f ₽", totalAmount));

        // Обновляем общее количество монет
        totalCoinsText.setText(String.valueOf(totalCoins));

        // Обновляем статистику по монетам
        coin50Text.setText(coinCounts[0] + " шт");
        coin1Text.setText(coinCounts[1] + " шт");
        coin2Text.setText(coinCounts[2] + " шт");
        coin5Text.setText(coinCounts[3] + " шт");
        coin10Text.setText(coinCounts[4] + " шт");
    }

    private void addToLog(String message) {
        String timeStamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String logEntry = timeStamp + ": " + message + "\n";

        // Добавляем новое сообщение в начало
        String currentLog = logText.getText().toString();
        logText.setText(logEntry + currentLog);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Очищаем все pending callbacks
        handler.removeCallbacksAndMessages(null);
    }
}
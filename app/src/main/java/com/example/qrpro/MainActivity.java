package com.example.qrpro;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    ImageView imageQR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        imageQR = findViewById(R.id.imageQR);

        Button btnGenerate = findViewById(R.id.btnGenerate);
        Button btnScan = findViewById(R.id.btnScan);
        Button btnShare = findViewById(R.id.btnShare);

        btnGenerate.setOnClickListener(v -> generateQR());

        btnScan.setOnClickListener(v -> {
            IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
            integrator.setPrompt("Наведите камеру на QR-код");
            integrator.setOrientationLocked(false);
            integrator.setBeepEnabled(true);
            integrator.initiateScan();
        });

        btnShare.setOnClickListener(v -> shareQR());
    }

    private void generateQR() {
        String text = editText.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Введите текст или ссылку!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 500, 500);
            BarcodeEncoder encoder = new BarcodeEncoder();

            Bitmap bitmap = encoder.createBitmap(bitMatrix);
            imageQR.setImageBitmap(bitmap);

            Toast.makeText(this, "QR-код готов", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка генерации: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                String scanned = result.getContents();
                editText.setText(scanned);
                Toast.makeText(this, "Распознано: " + scanned, Toast.LENGTH_LONG).show();
                generateQR();
            } else {
                Toast.makeText(this, "Сканирование отменено", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void shareQR() {
        if (imageQR.getDrawable() == null) {
            Toast.makeText(this, "Сначала создайте QR-код!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Bitmap bitmap = ((android.graphics.drawable.BitmapDrawable) imageQR.getDrawable()).getBitmap();

            File cacheDir = new File(getCacheDir(), "qr_images");
            if (!cacheDir.exists()) cacheDir.mkdirs();

            File file = new File(cacheDir, "qrcode_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    file
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, editText.getText().toString());
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Отправить через..."));

        } catch (Exception e) {
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
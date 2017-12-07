package com.example.adamfousek.tickitoprojekt;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission_group.CAMERA;

public class BarcodeReaderActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    // ScannerView - pracuje s kamerou
    private ZXingScannerView scannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nastavení layoutu pro kameru
        setContentView(R.layout.activity_barcode_reader);
        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    // @TODO Naskenovaný kód se uloží lokálně a odešle se na api a zjistí jestli již byl použitý
    @Override
    public void handleResult(Result result) {
        final String myResult = result.getText();

        // Vypsání kódu a jestli byl použitý nebo ne
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("OK/Not OK");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                scannerView.resumeCameraPreview(BarcodeReaderActivity.this);
            }
        });
        builder.setNeutralButton("Again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                scannerView.resumeCameraPreview(BarcodeReaderActivity.this);
            }
        });
        builder.setMessage(myResult);
        AlertDialog alert1 = builder.create();
        alert1.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Menu pro rozsvicení světla a zadání kódu ručně
        getMenuInflater().inflate(R.menu.barcode_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.flashlight){
            // Rozsvícení světla
            // @TODO Je třeba zapracovat na fixu kdy to nefunguje hezky - když se autofocusuje nejde zapnout světlo
            scannerView.toggleFlash();
            return true;
        }
        if(id == R.id.manulaScan){
            // Zobrazí se Activita na zadání kódu ručně
            /*Intent intent = new Intent(this, Settings.class);
            startActivityForResult(intent, 333);
            //Toast.makeText(getApplicationContext(), "Setting", Toast.LENGTH_SHORT).show();*/
        }
        return super.onOptionsItemSelected(item);

    }

}

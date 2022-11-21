package com.example.ejercicio2_1;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.ejercicio2_1.configuraciones.BDTransacciones;
import com.example.ejercicio2_1.configuraciones.SQLiteConexion;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity{

    static final int REQUEST_VIDEO_CAPTURE=104;
    VideoView video;
    Button tomarvideo;
    Button guardar;
    String grabar;
    byte[] bArrayVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        video = (VideoView) findViewById(R.id.videoView);
        tomarvideo = (Button) findViewById(R.id.btnrecord);
        guardar = (Button) findViewById(R.id.btnguardar);

        guardar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(bArrayVideo != null){
                    guardarVideo();
                }else{
                    if(bArrayVideo == null)
                    {
                        Toast.makeText(getApplicationContext(),"Generar Video!", Toast.LENGTH_LONG).show();
                        video.requestFocus();}
                }
            }
        });

        tomarvideo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                permisos();
            }
        });


    }
    private void permisos(){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},REQUEST_VIDEO_CAPTURE);
        }
        else{
            dispatchTakeVideoIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_VIDEO_CAPTURE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                tomarvideo();
            }
        }
        else{
            Toast.makeText(getApplicationContext(),"Brindar Permiso Camara",Toast.LENGTH_LONG).show();
        }
    }

    private void tomarvideo() {
        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (videoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(videoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    private void guardarVideo() {
        SQLiteConexion conexion = new SQLiteConexion(this, BDTransacciones.NameDataBase,null,1);
        SQLiteDatabase db = conexion.getWritableDatabase();
        ContentValues atributos = new ContentValues();
        atributos.put(BDTransacciones.video, bArrayVideo);
        String sql = "INSERT INTO tblGrabVideo(id,video) VALUES (0,'"+bArrayVideo+"')";
        try{
//          db.execSQL(sql);
            Long total = db.insert(BDTransacciones.tablaGVideo,BDTransacciones.id,atributos);
            Toast.makeText(getApplicationContext(),"Video Guardado! Nç"+total.toString(), Toast.LENGTH_LONG).show();
            db.close();
        }catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Se produjo un problema"+e, Toast.LENGTH_LONG).show();
        }
        limpiarPantalla();
        bArrayVideo=null;
    }

    private void limpiarPantalla() {
        video.setVideoURI(null);
    }

    private File createVideoFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String videoFileName = "MP4_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        File vid = File.createTempFile(
                videoFileName,
                ".mp4",
                storageDir
        );
        grabar = vid.getAbsolutePath();
        return vid;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK){
            File vidFile = new File(grabar);
            Uri urlvideo = Uri.fromFile(vidFile);
            video.setVideoURI(urlvideo);
            video.setMediaController(new MediaController(this));
            video.requestFocus();
            video.start();
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    bArrayVideo = Files.readAllBytes(vidFile.toPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void dispatchTakeVideoIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null){
            File videofile = null;
            try {
                videofile = createVideoFile();
                Toast.makeText(getApplicationContext(),"Video Creado",Toast.LENGTH_LONG).show();
                AlertDialog.Builder adb=new AlertDialog.Builder(MainActivity.this);
                adb.setTitle("Video Procesado");
                adb.setMessage("Su video se procesado correctamente");
                adb.setPositiveButton("Aceptar", null);
                adb.show();
            } catch (IOException ex) {
                Toast.makeText(getApplicationContext(),"Proceso incorrecto",Toast.LENGTH_LONG).show();
            }
            SystemClock.sleep(1000);
            Uri vidURI = null;
            if (videofile != null) {
                try {
                    vidURI = FileProvider.getUriForFile(this,"com.example.ejercicio2_1.provider",videofile);
                    Toast.makeText(getApplicationContext(),"Ruta exitosa", Toast.LENGTH_LONG ).show();
                }catch (Exception ex){
                    Toast.makeText(getApplicationContext(),"Error en ruta "+ex,Toast.LENGTH_LONG).show();
                    System.out.println(ex);
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, vidURI);
                startActivityForResult(takePictureIntent, REQUEST_VIDEO_CAPTURE);
            }
        }
    }
}
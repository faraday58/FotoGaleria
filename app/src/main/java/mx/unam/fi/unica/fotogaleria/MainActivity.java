package mx.unam.fi.unica.fotogaleria;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.media.audiofx.BassBoost;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    private String DIRECTORIO_APP = "MisImagenesApp";
    private String DIRECTORIO_MEDIOS = DIRECTORIO_APP + "ImagenesApp";
    private final int MIS_PERMISOS = 10;
    private final int CODIGO_FOTO = 20;
    private final int IMAGEN_SELECCIONADA = 30;
    private String mRuta;


    ImageView img_foto;
    Button btn_seleccion;
    RelativeLayout activity_main;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img_foto = (ImageView) findViewById(R.id.img_foto);
        btn_seleccion = (Button) findViewById(R.id.btn_seleccion);
        activity_main =(RelativeLayout)findViewById(R.id.activity_main);

        if (myRequestStoragePermission()) {
            btn_seleccion.setEnabled(true);
        } else {
            btn_seleccion.setEnabled(false);
        }

        btn_seleccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptions();
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void showOptions() {
        final CharSequence[] opcion = {"Cámara", "Elegir de la galería", "Cancelar"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Elige una Opción");
        builder.setItems(opcion, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (opcion[which] == "Cámara") {
                    AbrirCamara();
                } else if (opcion[which] == "Elejir de la galería") {
                    Intent intent_camara = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent_camara.setType("image/*");
                    startActivityForResult(intent_camara.createChooser(intent_camara, "Selecciona App para imagenes"), IMAGEN_SELECCIONADA);
                } else {
                    dialog.dismiss();
                }

            }
        });

        builder.show();
    }

    private void AbrirCamara() {
        File archivo = new File(Environment.getExternalStorageDirectory(), DIRECTORIO_MEDIOS);
        boolean DirectorioCreado = archivo.exists();
        if (!DirectorioCreado) {
            DirectorioCreado = archivo.mkdirs();
        }
        if (DirectorioCreado) {
            Long timeStamp = System.currentTimeMillis() / 1000;
            String nombreImagen = timeStamp.toString() + ".jpg";
            mRuta = Environment.getExternalStorageDirectory() + archivo.separator + DIRECTORIO_MEDIOS + archivo.separator + nombreImagen;
            File ArchivoNuevo = new File(mRuta);
            Intent intent_archivo = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent_archivo.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(ArchivoNuevo));
            startActivityForResult(intent_archivo, CODIGO_FOTO);

        }

    }

    private boolean myRequestStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if ((checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED)) {
            return true;
        }

        if ((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) || (shouldShowRequestPermissionRationale(CAMERA))) {

            Snackbar.make(activity_main,"Los permisos son necesarios para usar la aplicación",Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {

                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onClick(View v)
                {

                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA},MIS_PERMISOS);

                }
            }).show();
        }
        else
        {
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA},MIS_PERMISOS);
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("file_path",mRuta);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mRuta = savedInstanceState.getString("file_path");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CODIGO_FOTO:
                    MediaScannerConnection.scanFile(this, new String[]{mRuta}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned" + path + ":");
                            Log.i("ExternalStorage", "->Uri =" + uri);

                        }
                    });

                    Bitmap bitmap = BitmapFactory.decodeFile(mRuta);
                    img_foto.setImageBitmap(bitmap);
                    break;
                case IMAGEN_SELECCIONADA:
                    Uri path = data.getData();
                    img_foto.setImageURI(path);
                    break;

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == MIS_PERMISOS)
        {
            if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
            {
                Snackbar.make(activity_main,"Permisos aceptados",Snackbar.LENGTH_LONG).show();
                btn_seleccion.setEnabled(true);
            }
        }else
        {
            muestraExplicacion();
        }
    }

    private void muestraExplicacion() {
        AlertDialog.Builder  builder= new AlertDialog.Builder(this);
        builder.setTitle("Permisos denegados");
        builder.setMessage("Para usar las funciones de la app necesitas aceptar los permisos");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS );
                Uri uri = Uri.fromParts("package",getPackageName(),null);
                intent.setData(uri);
                startActivity(intent);

            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.show();
    }
}

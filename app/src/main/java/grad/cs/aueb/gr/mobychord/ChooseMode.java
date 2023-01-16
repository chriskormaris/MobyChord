package grad.cs.aueb.gr.mobychord;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;

import java.io.File;

//********************************* Polyvios Liosis ************************************//
//********************************* Christos Kormaris **********************************//
//********************************* Dimitris Botonakis *********************************//


public class ChooseMode extends Activity {

    //**********GUI variables**********
    private Button makeRequestButton;
    private Button makeNodeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_mode);

        makeRequestButton = this.findViewById(R.id.makeRequestButton);
        makeNodeButton = this.findViewById(R.id.makeNodeButton);

        makeRequestButton.setOnClickListener(v -> makeNewRequest());

        makeNodeButton.setOnClickListener(v -> newChordNodeRequest());

        createAppDirectories();
    }


    private void makeNewRequest() {
        Intent intent = new Intent(this, RequestClient.class);
        startActivity(intent);
    }

    private void newChordNodeRequest() {
        Intent intent = new Intent(this, NewNodeInfoActivity.class);
        startActivity(intent);
    }

    private void createAppDirectories() {
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "mobyChord");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "mobyChord/Node");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        /*
        folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "mobyChord/Client");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        */

        folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "mobyChord/Node/Net Architecture");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "mobyChord/Node/Keys");
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

}
